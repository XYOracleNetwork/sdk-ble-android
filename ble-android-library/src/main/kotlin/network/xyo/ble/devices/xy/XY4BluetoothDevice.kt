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
import network.xyo.ble.firmware.XYOtaUpdateListener
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.*
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.reporters.XYFinderBluetoothDeviceReporter
import network.xyo.ble.services.dialog.SpotaService
import network.xyo.ble.services.xy.PrimaryService

/**
 * Listener for XY4 Devices.
 *
 * Brings in a renamed Finder Listener.
 * .listener is now camel cased into the name.
 */

@kotlin.ExperimentalUnsignedTypes
open class XY4BluetoothDevice(
    context: Context,
    scanResult: XYScanResult,
    hash: String
) : XYFinderBluetoothDevice(context, scanResult, hash) {

    open val alertNotification by lazy { AlertNotificationService(this) }
    open val batteryService by lazy { BatteryService(this) }
    open val currentTimeService by lazy { CurrentTimeService(this) }
    open val linkLossService by lazy { LinkLossService(this) }
    open val txPowerService by lazy { TxPowerService(this) }
    open val primary by lazy { PrimaryService(this) }
    open val spotaService by lazy { SpotaService(this) }

    private var lastButtonPressTime = 0L

    private var updater: XYBluetoothDeviceUpdate? = null

    private val buttonListener = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic?.uuid == primary.buttonState.uuid) {
                // every time a notify fires, we have to re-enable it
                enableButtonNotifyIfConnected()
                reporter.buttonPressed(this@XY4BluetoothDevice,
                        buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
                globalReporter.buttonPressed(this@XY4BluetoothDevice,
                        buttonPressFromInt(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)))
            }
        }
    }

    init {
        centralCallback.addListener("xy4", buttonListener)
        enableButtonNotifyIfConnected()
    }

    override val prefix = "xy:ibeacon"

    override suspend fun find(song: UByte?) = connection {
        log.info("Arie:find:start: $song")
        val unlockResult = unlock()
        log.info("Arie:find:unlock: ${unlockResult.error}")
        if (unlockResult.error == XYBluetoothResultErrorCode.None) {
            log.info("Arie:find:setting song")
            val writeResult = primary.buzzer.set(song ?: 0xbU)
            if (writeResult.error == XYBluetoothResultErrorCode.None) {
                log.info("Arie:find:success")
                return@connection XYBluetoothResult(writeResult.value)
            } else {
                log.info("Arie:find:fail1")
                return@connection XYBluetoothResult(null, writeResult.error)
            }
        } else {
            log.info("Arie:find:fail2")
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
                    enableButtonNotifyIfConnected()
                    reporter.buttonPressed(this, XYFinderBluetoothDeviceButtonPress.Single)
                    globalReporter.buttonPressed(this, XYFinderBluetoothDeviceButtonPress.Single)
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

    override val minor: UShort
        get() {
            // we have to mask the low nibble for the power level
            return minorValue.and(0xfff0.toUShort()).or(0x0004.toUShort())
        }

    fun ensureStayAwake() {
        val functionName = "ensureStayAwake"
        log.info(functionName, "started")

        GlobalScope.launch {
            var result = false
            log.info(functionName, "async")
            connection {
                log.info(functionName, "connected")
                val unlockResult = unlock()
                if (unlockResult.hasError()) {
                    log.error(unlockResult.error.toString())
                } else {
                    log.info(functionName, "unlocked")
                    val currentStayAwakeResult = primary.stayAwake.get()
                    if (currentStayAwakeResult.hasError()) {
                        log.error(currentStayAwakeResult.error.toString())
                    } else {
                        if (currentStayAwakeResult.value?.equals(1u) == true) {
                            log.info(functionName, "complete [already set ${currentStayAwakeResult.value}]")
                        } else {
                            log.info(functionName, "current [not set ${currentStayAwakeResult.value}]")
                            val stayAwakeResult = stayAwake()
                            if (stayAwakeResult.hasError()) {
                                log.error(stayAwakeResult.error.toString())
                            } else {
                                log.info(functionName, "complete")
                                result = true
                            }
                        }
                    }
                }
                return@connection XYBluetoothResult(result)
            }
        }
    }

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("a44eacf4-0104-0000-0000-5f784c9977b5")!!

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

        val globalReporter = XYFinderBluetoothDeviceReporter<XY4BluetoothDevice>()

        @Deprecated("Deprecated", ReplaceWith("globalReporter.addListener(key, listener)"))
        fun addGlobalListener(key: String, listener: XYFinderBluetoothDeviceListener) {
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
