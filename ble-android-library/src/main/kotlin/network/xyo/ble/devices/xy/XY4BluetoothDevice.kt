package network.xyo.ble.devices.xy

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.firmware.XYBluetoothDeviceUpdate
import network.xyo.ble.firmware.XYOtaFile
import network.xyo.ble.firmware.XYOtaUpdate
import network.xyo.ble.firmware.XYOtaUpdateListener
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.*
import network.xyo.ble.services.dialog.SpotaService
import network.xyo.ble.services.xy.PrimaryService

open class XY4BluetoothDeviceListener : XYFinderBluetoothDeviceListener()

@kotlin.ExperimentalUnsignedTypes
open class XY4BluetoothDevice(
    context: Context,
    scanResult: XYScanResult,
    hash: String
) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val alertNotification by lazy { AlertNotificationService(this) }
    val batteryService by lazy { BatteryService(this) }
    val currentTimeService by lazy { CurrentTimeService(this) }
    val deviceInformationService by lazy { DeviceInformationService(this) }
    val genericAccessService by lazy { GenericAccessService(this) }
    val genericAttributeService by lazy { GenericAttributeService(this) }
    val linkLossService by lazy { LinkLossService(this) }
    val txPowerService by lazy { TxPowerService(this) }
    val primary by lazy { PrimaryService(this) }
    val spotaService by lazy { SpotaService(this) }

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

    override suspend fun find() = connection<UByte> {
        log.info("find")
        val unlockResult = unlock()
        if (unlockResult.error == XYBluetoothResultErrorCode.None) {
            val writeResult = primary.buzzer.set(0xbU)
            if (writeResult.error == XYBluetoothResultErrorCode.None) {
                return@connection XYBluetoothResult(writeResult.value)
            } else {
                return@connection XYBluetoothResult(null, writeResult.error)
            }
        } else {
            return@connection XYBluetoothResult(null, unlockResult.error)
        }
    }

    override suspend fun stopFind() = connection {
        return@connection primary.buzzer.set(0xffU)
    }

    override suspend fun lock() = connection {
        return@connection primary.lock.set(DefaultLockCode)
    }

    override suspend fun unlock() = connection {
        return@connection primary.unlock.set(DefaultLockCode)
    }

    override suspend fun stayAwake() = connection {
        return@connection primary.stayAwake.set(0x01U)
    }

    override suspend fun fallAsleep() = connection {
        return@connection primary.stayAwake.set(0x00U)
    }

    override suspend fun batteryLevel() = connection {
        return@connection batteryService.level.get()
    }

    override fun onDetect(scanResult: XYScanResult?) {
        super.onDetect(scanResult)
        if (scanResult != null) {
            if (pressFromScanResult(scanResult)) {
                if (now - lastButtonPressTime > BUTTON_ADVERTISEMENT_LENGTH) {
                    reportButtonPressed(XYFinderBluetoothDeviceButtonPress.Single)
                    lastButtonPressTime = now
                }
            }
        }
    }

    override fun updateFirmware(folderName: String, filename: String, listener: XYOtaUpdateListener) {
        val otaFile = XYOtaFile.getByName(folderName, filename)
        val updater = XYBluetoothDeviceUpdate(spotaService, this, otaFile)

        updater.addListener("XY4BluetoothDevice", listener)
        updater.start()
    }

    override fun updateFirmware(stream: InputStream, listener: XYOtaUpdateListener) {

        val otaFile = XYOtaFile.getByStream(stream)
        updater = XYBluetoothDeviceUpdate(spotaService, this, otaFile)

        updater?.addListener("XY4BluetoothDevice", listener)
        updater?.start()
    }

    override fun cancelUpdateFirmware() {
        updater?.cancel()
    }

    private fun enableButtonNotifyIfConnected() {
        if (connection?.state == BluetoothGatt.STATE_CONNECTED) {
            GlobalScope.launch {
                primary.buttonState.enableNotify(true)
            }
        }
    }

    override fun onConnectionStateChange(newState: Int) {
        super.onConnectionStateChange(newState)
        enableButtonNotifyIfConnected()
    }

    override fun reportButtonPressed(state: XYFinderBluetoothDeviceButtonPress) {
        super.reportButtonPressed(state)
        // every time a notify fires, we have to re-enable it
        enableButtonNotifyIfConnected()
        reportGlobalButtonPressed(this, state)
    }

    override val minor: UShort
        get() {
            // we have to mask the low nibble for the power level
            return minorValue.and(0xfff0.toUShort()).or(0x0004.toUShort())
        }

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("a44eacf4-0104-0000-0000-5f784c9977b5")!!

        protected val globalListeners = HashMap<String, XY4BluetoothDeviceListener>()

        val DefaultLockCode = byteArrayOf(0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(), 0x08.toByte(), 0x09.toByte(), 0x0a.toByte(), 0x0b.toByte(), 0x0c.toByte(), 0x0d.toByte(), 0x0e.toByte(), 0x0f.toByte())

        // this is how long the xy4 will broadcast ads with power level 8 when a button is pressed once
        private const val BUTTON_ADVERTISEMENT_LENGTH = 30 * 1000

        fun enable(enable: Boolean) {
            if (enable) {
                XYFinderBluetoothDevice.enable(true)
                addCreator(FAMILY_UUID, creator)
            } else {
                removeCreator(FAMILY_UUID)
            }
        }

        fun addGlobalListener(key: String, listener: XY4BluetoothDeviceListener) {
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

        fun reportGlobalButtonPressed(device: XY4BluetoothDevice, state: XYFinderBluetoothDeviceButtonPress) {
            GlobalScope.launch {
                synchronized(globalListeners) {
                    for (listener in globalListeners) {
                        val xyFinderListener = listener.value as? XYFinderBluetoothDeviceListener
                        if (xyFinderListener != null) {
                            log.info("reportButtonPressed: $xyFinderListener")
                            GlobalScope.launch {
                                when (state) {
                                    XYFinderBluetoothDeviceButtonPress.Single -> xyFinderListener.buttonSinglePressed(device)
                                    XYFinderBluetoothDeviceButtonPress.Double -> xyFinderListener.buttonDoublePressed(device)
                                    XYFinderBluetoothDeviceButtonPress.Long -> xyFinderListener.buttonLongPressed(device)
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private val creator = object : XYCreator() {
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
                val buttonBit = 0x0008.toUShort()
                val minor = buffer.getShort(20).toUShort() and buttonBit
                minor == buttonBit
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
