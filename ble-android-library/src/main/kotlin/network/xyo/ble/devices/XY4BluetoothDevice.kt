package network.xyo.ble.devices

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.firmware.XYBluetoothDeviceUpdate
import network.xyo.ble.firmware.XYOtaFile
import network.xyo.ble.firmware.XYOtaUpdate
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.scanner.XYScanResult
import network.xyo.ble.services.dialog.SpotaService
import network.xyo.ble.services.standard.*
import network.xyo.ble.services.xy4.PrimaryService
import network.xyo.base.XYBase
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class XY4BluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val alertNotification = AlertNotificationService(this)
    val batteryService = BatteryService(this)
    val currentTimeService = CurrentTimeService(this)
    val deviceInformationService = DeviceInformationService(this)
    val genericAccessService = GenericAccessService(this)
    val genericAttributeService = GenericAttributeService(this)
    val linkLossService = LinkLossService(this)
    val txPowerService = TxPowerService(this)

    val primary = PrimaryService(this)
    val spotaService = SpotaService(this)

    private var lastButtonPressTime = 0L

    private var updater: XYBluetoothDeviceUpdate? = null

    private val buttonListener = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic?.uuid == primary.buttonState.uuid) {
                enableButtonNotifyIfConnected()
                reportButtonPressed(buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
            }
        }
    }

    init {
        centralCallback.addListener("xy4", buttonListener)
        enableButtonNotifyIfConnected()
    }

    override val prefix = "xy:ibeacon"

    override fun find() = connection {
        log.info("find")
        if (unlock().await().error == null) {
            val writeResult = primary.buzzer.set(11).await()
            if (writeResult.error == null) {
                return@connection XYBluetoothResult(writeResult.value)
            } else {
                return@connection XYBluetoothResult(-1, XYBluetoothError("Failed to Write Characteristic"))
            }
        } else {
            return@connection XYBluetoothResult(-1, XYBluetoothError("Failed to Unlock"))
        }
    }

    override fun stopFind() = connection {
        return@connection primary.buzzer.set(-1).await()
    }

    override fun lock() = connection {
        return@connection primary.lock.set(DefaultLockCode).await()
    }

    override fun unlock() = connection {
        return@connection primary.unlock.set(DefaultLockCode).await()
    }

    override fun stayAwake() = connection {
        return@connection primary.stayAwake.set(1).await()
    }

    override fun fallAsleep() = connection {
        return@connection primary.stayAwake.set(0).await()
    }

    override fun batteryLevel() = connection {
        return@connection batteryService.level.get().await()
    }

    override fun onDetect(scanResult: XYScanResult?) {
        super.onDetect(scanResult)
        if (scanResult != null) {
            if (pressFromScanResult(scanResult)) {
                if (now - lastButtonPressTime > BUTTON_ADVERTISEMENT_LENGTH) {
                    reportButtonPressed(ButtonPress.Single)
                    lastButtonPressTime = now
                }
            }
        }
    }

    override fun updateFirmware(folderName:String, filename: String, listener: XYOtaUpdate.Listener) {
        val otaFile = XYOtaFile.getByName(folderName, filename)
        val updater = XYBluetoothDeviceUpdate(spotaService,this, otaFile)

        updater.addListener("XY4BluetoothDevice", listener)
        updater.start()
    }

    override fun updateFirmware(stream: InputStream, listener: XYOtaUpdate.Listener) {

        val otaFile = XYOtaFile.getByStream(stream)
        updater = XYBluetoothDeviceUpdate(spotaService,this, otaFile)

        updater?.addListener("XY4BluetoothDevice", listener)
        updater?.start()
    }

    override fun cancelUpdateFirmware() {
        updater?.cancel()
    }

    private fun enableButtonNotifyIfConnected() {
        if (connection?.state == BluetoothGatt.STATE_CONNECTED) {
            GlobalScope.launch {
                primary.buttonState.enableNotify(true).await()
            }
        }
    }

    override fun onConnectionStateChange(newState: Int) {
        super.onConnectionStateChange(newState)
        enableButtonNotifyIfConnected()
    }

    override fun reportButtonPressed(state: ButtonPress) {
        super.reportButtonPressed(state)
        //every time a notify fires, we have to re-enable it
        enableButtonNotifyIfConnected()
        reportGlobalButtonPressed(this, state)
    }

    override val minor: UShort
        get() {
            //we have to mask the low nibble for the power level
            return _minor.and(0xfff0.toUShort()).or(0x0004.toUShort())
        }

    open class Listener : XYFinderBluetoothDevice.Listener()

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("a44eacf4-0104-0000-0000-5f784c9977b5")!!

        protected val globalListeners = HashMap<String, Listener>()

        val DefaultLockCode = byteArrayOf(0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(), 0x08.toByte(), 0x09.toByte(), 0x0a.toByte(), 0x0b.toByte(), 0x0c.toByte(), 0x0d.toByte(), 0x0e.toByte(), 0x0f.toByte())

        //this is how long the xy4 will broadcast ads with power level 8 when a button is pressed once
        private const val BUTTON_ADVERTISEMENT_LENGTH = 30 * 1000

        enum class StayAwake(val state: Int) {
            Off(0),
            On(1)
        }

        fun enable(enable: Boolean) {
            if (enable) {
                XYFinderBluetoothDevice.enable(true)
                addCreator(FAMILY_UUID, creator)
            } else {
                removeCreator(FAMILY_UUID)
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

        fun reportGlobalButtonPressed(device: XY4BluetoothDevice, state: ButtonPress) {
            GlobalScope.launch {
                synchronized(globalListeners) {
                    for (listener in globalListeners) {
                        val xyFinderListener = listener.value as? XYFinderBluetoothDevice.Listener
                        if (xyFinderListener != null) {
                            log.info("reportButtonPressed: $xyFinderListener")
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
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {
                val hash = hashFromScanResult(scanResult)
                foundDevices[hash] = globalDevices[hash]
                        ?: XY4BluetoothDevice(context, scanResult, hash)
            }
        }

        private fun majorFromScanResult(scanResult: XYScanResult): UShort? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(18).toUShort()
            } else {
                null
            }
        }

        @kotlin.ExperimentalUnsignedTypes
        internal fun pressFromScanResult(scanResult: XYScanResult): Boolean {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                val minor = buffer.getShort(20).toUShort()
                val buttonBit = 0x0008.toUShort()
                buttonBit == 0x0008.toUShort()
            } else {
                false
            }
        }

        @kotlin.ExperimentalUnsignedTypes
        private fun minorFromScanResult(scanResult: XYScanResult): UShort? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(20).toUShort().and(0xfff0.toUShort()).or(0x0004.toUShort())
            } else {
                null
            }
        }

        internal fun hashFromScanResult(scanResult: XYScanResult): String {
            val uuid = iBeaconUuidFromScanResult(scanResult)
            val major = majorFromScanResult(scanResult)
            val minor = minorFromScanResult(scanResult)

            return "$uuid:$major:$minor"
        }

    }
}
