package network.xyo.ble.devices

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.scanner.XYScanResult
import network.xyo.ble.services.standard.*
import network.xyo.ble.services.xy3.*
import network.xyo.core.XYBase
import unsigned.Ushort
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class XY3BluetoothDevice(context: Context, scanResult: XYScanResult, hash: Int) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val alertNotification = AlertNotificationService(this)
    val batteryService = BatteryService(this)
    val currentTimeService = CurrentTimeService(this)
    val deviceInformationService = DeviceInformationService(this)
    val genericAccessService = GenericAccessService(this)
    val genericAttributeService = GenericAttributeService(this)
    val linkLossService = LinkLossService(this)
    val txPowerService = TxPowerService(this)

    val basicConfigService = BasicConfigService(this)
    val controlService = ControlService(this)
    val csrOtaService = CsrOtaService(this)
    val extendedConfigService = ExtendedConfigService(this)
    val extendedControlService = ExtendedControlService(this)
    val sensorService = SensorService(this)

    private var lastButtonPressTime = 0L

    enum class StayAwake(val state: Int) {
        Off(0),
        On(1)
    }

    private val buttonListener = object : XYBluetoothGattCallback() {
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            logInfo("onCharacteristicChanged")
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic?.uuid == controlService.button.uuid) {
                reportButtonPressed(buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
            }
        }
    }

    init {
        addGattListener("xy3", buttonListener)
    }

    override val minor: Ushort
        get() {
            //we have to mask the low nibble for the power level
            return _minor.and(0xfff0).or(0x0004)
        }

    override val prefix = "xy:ibeacon"

    override fun find(): Deferred<XYBluetoothResult<Int>> {
        logInfo("find")
        return controlService.buzzerSelect.set(2)
    }

    override fun lock(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo("lock")
        return basicConfigService.lock.set(DEFAULT_LOCK_CODE)
    }

    override fun unlock(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo("unlock")
        return basicConfigService.unlock.set(DEFAULT_LOCK_CODE)
    }

    override fun stayAwake(): Deferred<XYBluetoothResult<Int>> {
        logInfo("stayAwake")
        return extendedConfigService.registration.set(1)
    }

    override fun fallAsleep(): Deferred<XYBluetoothResult<Int>> {
        logInfo("fallAsleep")
        return extendedConfigService.registration.set(0)
    }

    override fun onDetect(scanResult: XYScanResult?) {
        super.onDetect(scanResult)
        if (scanResult != null) {
            if (pressFromScanResult(scanResult)) {
                if (now - lastButtonPressTime > BUTTON_ADVERTISEMENT_LENGTH) {
                    logInfo("onDetect: pressFromScanResult: first")
                    reportButtonPressed(ButtonPress.Single)
                    lastButtonPressTime = now
                }
            }
        }
    }

    private fun enableButtonNotifyIfConnected() {
        logInfo("enableButtonNotifyIfConnected")
        if (connectionState == ConnectionState.Connected) {
            logInfo("enableButtonNotifyIfConnected: Connected")
            controlService.button.enableNotify(true)
        }
    }

    override fun reportButtonPressed(state: ButtonPress) {
        super.reportButtonPressed(state)
        //every time a notify fires, we have to re-enable it
        enableButtonNotifyIfConnected()
        XY3BluetoothDevice.reportGlobalButtonPressed(this, state)
    }

    open class Listener : XYFinderBluetoothDevice.Listener()

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("08885dd0-111b-11e4-9191-0800200c9a66")!!

        //this is how long the xy4 will broadcast ads with power level 8 when a button is pressed once
        private const val BUTTON_ADVERTISEMENT_LENGTH = 30 * 1000

        private val DEFAULT_LOCK_CODE = byteArrayOf(0x2f.toByte(), 0xbe.toByte(), 0xa2.toByte(), 0x07.toByte(), 0x52.toByte(), 0xfe.toByte(), 0xbf.toByte(), 0x31.toByte(), 0x1d.toByte(), 0xac.toByte(), 0x5d.toByte(), 0xfa.toByte(), 0x7d.toByte(), 0x77.toByte(), 0x76.toByte(), 0x80.toByte())

        protected val globalListeners = HashMap<String, Listener>()

        fun enable(enable: Boolean) {
            if (enable) {
                XYFinderBluetoothDevice.enable(true)
                addCreator(FAMILY_UUID, creator)
            } else {
                removeCreator(FAMILY_UUID)
            }
        }

        private fun majorFromScanResult(scanResult: XYScanResult): Int? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(18).toInt()
            } else {
                null
            }
        }

        private fun minorFromScanResult(scanResult: XYScanResult): Int? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(20).toInt().and(0xfff0).or(0x0004)
            } else {
                null
            }
        }

        internal fun pressFromScanResult(scanResult: XYScanResult): Boolean {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                val minor = Ushort(buffer.getShort(20))
                val buttonBit = minor.and(0x0008)
                buttonBit == Ushort(0x0008)
            } else {
                false
            }
        }

        fun addGlobalListener(key: String, listener: Listener) {
            GlobalScope.launch {
                synchronized(globalListeners) {
                    globalListeners.put(key, listener)
                }
            }
        }

        fun removeGlobalListener(key: String) {
            GlobalScope.launch {
                synchronized(globalListeners) {
                    globalListeners.remove(key)
                }
            }
        }

        fun reportGlobalButtonPressed(device: XY3BluetoothDevice, state: ButtonPress) {
            logInfo("reportButtonPressed (Global)")
            GlobalScope.launch {
                synchronized(globalListeners) {
                    for (listener in globalListeners) {
                        val xyFinderListener = listener.value as? XYFinderBluetoothDevice.Listener
                        if (xyFinderListener != null) {
                            logInfo("reportButtonPressed: $xyFinderListener")
                            GlobalScope.launch {
                                when (state) {
                                    ButtonPress.Single -> xyFinderListener.buttonSinglePressed(device)
                                    ButtonPress.Double -> xyFinderListener.buttonDoublePressed(device)
                                    ButtonPress.Long -> xyFinderListener.buttonLongPressed(device)
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        internal val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<Int, XYBluetoothDevice>, foundDevices: HashMap<Int, XYBluetoothDevice>) {
                val hash = hashFromScanResult(scanResult)
                if (hash != null) {
                    foundDevices[hash] = globalDevices[hash] ?: XY3BluetoothDevice(context, scanResult, hash)
                }
            }
        }

        fun hashFromScanResult(scanResult: XYScanResult): Int? {
            val uuid = iBeaconUuidFromScanResult(scanResult)
            val major = majorFromScanResult(scanResult)
            val minor = minorFromScanResult(scanResult)
            return "$uuid:$major:$minor".hashCode()
        }

    }
}