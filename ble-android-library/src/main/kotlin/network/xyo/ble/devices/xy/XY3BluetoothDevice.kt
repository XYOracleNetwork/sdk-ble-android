package network.xyo.ble.devices.xy

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.*
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.reporters.XYFinderBluetoothDeviceReporter
import network.xyo.ble.services.xy.*

/**
 * Listener for XY3 Devices.
 *
 * Brings in a renamed Finder Listener.
 * .listener is now camel cased into the name.
 */
open class XY3BluetoothDeviceListener : XYFinderBluetoothDeviceListener()

@Suppress("unused")
open class XY3BluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val alertNotification by lazy { AlertNotificationService(this) }
    val batteryService by lazy { BatteryService(this) }
    val currentTimeService by lazy { CurrentTimeService(this) }
    val deviceInformationService by lazy { DeviceInformationService(this) }
    val genericAccessService by lazy { GenericAccessService(this) }
    val genericAttributeService by lazy { GenericAttributeService(this) }
    val linkLossService by lazy { LinkLossService(this) }
    val txPowerService by lazy { TxPowerService(this) }

    val basicConfigService by lazy { BasicConfigService(this) }
    val controlService by lazy { ControlService(this) }
    val extendedConfigService by lazy { ExtendedConfigService(this) }
    val sensorService by lazy { SensorService(this) }

    private var lastButtonPressTime = 0L

    private val buttonListener = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic?.uuid == controlService.button.uuid) {
                reporter.buttonPressed(this@XY3BluetoothDevice, buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
                globalReporter.buttonPressed(this@XY3BluetoothDevice, buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
            }
        }
    }

    init {
        centralCallback.addListener("xy3", buttonListener)
    }

    // we only allow mac addresses that end in 4 to be updated since those are the connectible ones
    override fun updateBluetoothDevice(device: BluetoothDevice?) {
        if (device?.address?.endsWith("4") == true) {
            this.device = device
        }
        lastAdTime = now
    }

    override val minor: UShort
        get() {
            // we have to mask the low nibble for the power level
            return minorValue.and(0xfff0.toUShort()).or(0x0004.toUShort())
        }

    override val prefix = "xy:ibeacon"

    override suspend fun find(song: UByte?) = connection {
        return@connection controlService.buzzerSelect.set(song ?: 0x02U)
    }

    override suspend fun stopFind() = connection {
        return@connection controlService.buzzerSelect.set(0xffU)
    }

    override suspend fun lock() = connection {
        return@connection basicConfigService.lock.set(DEFAULT_LOCK_CODE)
    }

    override suspend fun unlock() = connection {
        return@connection basicConfigService.unlock.set(DEFAULT_LOCK_CODE)
    }

    override suspend fun stayAwake() = connection {
        return@connection extendedConfigService.stayAwake.set(XYFinderBluetoothDeviceStayAwake.On.state)
    }

    override suspend fun fallAsleep() = connection {
        return@connection extendedConfigService.stayAwake.set(XYFinderBluetoothDeviceStayAwake.Off.state)
    }

    override fun onDetect(scanResult: XYScanResult?) {
        super.onDetect(scanResult)
        if (scanResult != null) {
            if (pressFromScanResult(scanResult)) {
                if (now - lastButtonPressTime > BUTTON_ADVERTISEMENT_LENGTH) {
                    reporter.buttonPressed(this, XYFinderBluetoothDeviceButtonPress.Single)
                    globalReporter.buttonPressed(this, XYFinderBluetoothDeviceButtonPress.Single)
                    lastButtonPressTime = now
                }
            }
        }
    }

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("08885dd0-111b-11e4-9191-0800200c9a66")!!

        // this is how long the xy4 will broadcast ads with power level 8 when a button is pressed once
        private const val BUTTON_ADVERTISEMENT_LENGTH = 30 * 1000

        private val DEFAULT_LOCK_CODE = byteArrayOf(0x2f.toByte(), 0xbe.toByte(), 0xa2.toByte(), 0x07.toByte(), 0x52.toByte(), 0xfe.toByte(), 0xbf.toByte(), 0x31.toByte(), 0x1d.toByte(), 0xac.toByte(), 0x5d.toByte(), 0xfa.toByte(), 0x7d.toByte(), 0x77.toByte(), 0x76.toByte(), 0x80.toByte())

        protected val globalListeners = HashMap<String, XY3BluetoothDeviceListener>()

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

        @kotlin.ExperimentalUnsignedTypes
        internal fun pressFromScanResult(scanResult: XYScanResult): Boolean {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                val minor = buffer.getShort(20).toUShort()
                val buttonBit = minor.and(0x0008.toUShort())
                buttonBit == 0x0008.toUShort()
            } else {
                false
            }
        }

        val globalReporter = XYFinderBluetoothDeviceReporter<XY3BluetoothDevice, XY3BluetoothDeviceListener>()

        @Deprecated("Deprecated", ReplaceWith("globalReporter.addListener(key, listener)"))
        fun addGlobalListener(key: String, listener: XY3BluetoothDeviceListener) {
            globalReporter.addListener(key, listener)
        }

        @Deprecated("Deprecated", ReplaceWith("globalReporter.removeListener(key)"))
        fun removeGlobalListener(key: String) {
            globalReporter.removeListener(key)
        }

        private val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {
                val hash = hashFromScanResult(scanResult)
                foundDevices[hash] = globalDevices[hash]
                        ?: XY3BluetoothDevice(context, scanResult, hash)
            }
        }

        fun hashFromScanResult(scanResult: XYScanResult): String {
            val uuid = iBeaconUuidFromScanResult(scanResult)
            val major = majorFromScanResult(scanResult)
            val minor = minorFromScanResult(scanResult)
            return "$uuid:$major:$minor"
        }
    }
}
