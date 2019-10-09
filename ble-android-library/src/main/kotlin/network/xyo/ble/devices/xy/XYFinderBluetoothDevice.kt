package network.xyo.ble.devices.xy

import android.content.Context
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.firmware.XYOtaUpdate
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.scanner.XYScanResult

@kotlin.ExperimentalUnsignedTypes
open class XYFinderBluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYIBeaconBluetoothDevice(context, scanResult, hash) {

    enum class Family {
        Unknown,
        XY1,
        XY2,
        XY3,
        Mobile,
        Gps,
        Near,
        XY4,
        Webble
    }

    enum class Proximity {
        None,
        OutOfRange,
        VeryFar,
        Far,
        Medium,
        Near,
        VeryNear,
        Touching
    }

    enum class ButtonPress(val state: Int) {
        None(0),
        Single(1),
        Double(2),
        Long(3)
    }

    enum class StayAwake(val state: UByte) {
        Off(0U),
        On(1U)
    }

    override val id: String
        get() {
            return "$prefix:$uuid.${major.toInt()}.${minor.and(0xfff0.toUShort()).or(0x0004.toUShort()).toInt()}"
        }

    internal open val prefix = "xy:finder"

    val family: Family
        get() {
            return when (this@XYFinderBluetoothDevice) {
                is XYMobileBluetoothDevice -> {
                    Family.Mobile
                }
                is XYGpsBluetoothDevice -> {
                    Family.Gps
                }
                is XY4BluetoothDevice -> {
                    Family.XY4
                }
                is XY3BluetoothDevice -> {
                    Family.XY3
                }
                else -> {
                    Family.Unknown
                }
            }
        }

    // the distance is in meters, so these are what we subjectively think are the fuzzy proximity values
    val proximity: Proximity
        get() {

            val distance = distance ?: return Proximity.OutOfRange

            if (distance < 0.0) {
                return Proximity.None
            }

            if (distance < 0.5) {
                return Proximity.Touching
            }

            if (distance < 15) {
                return Proximity.VeryNear
            }

            if (distance < 30) {
                return Proximity.Near
            }

            if (distance < 60) {
                return Proximity.Medium
            }

            if (distance < 120) {
                return Proximity.Far
            }

            return Proximity.VeryFar
        }

    // signal the user to where it is, usually make it beep
    open suspend fun find() = connection {
        log.error(UnsupportedOperationException().toString(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    // turn off finding, if supported
    open suspend fun stopFind() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun lock() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<ByteArray>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun unlock() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<ByteArray>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun stayAwake() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun fallAsleep() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun restart() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open suspend fun batteryLevel() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResult.ErrorCode.Unsupported)
    }

    open fun updateFirmware(stream: InputStream, listener: XYOtaUpdate.Listener) {
    }

    open fun updateFirmware(folderName: String, filename: String, listener: XYOtaUpdate.Listener) {
    }

    open fun cancelUpdateFirmware() {
    }

    internal open fun reportButtonPressed(state: ButtonPress) {
        log.info("reportButtonPressed")
        GlobalScope.launch {
            synchronized(listeners) {
                for (listener in listeners) {
                    val xyFinderListener = listener.value as? Listener
                    if (xyFinderListener != null) {
                        log.info("reportButtonPressed: $xyFinderListener")
                        GlobalScope.launch {
                            when (state) {
                                ButtonPress.Single -> xyFinderListener.buttonSinglePressed(this@XYFinderBluetoothDevice)
                                ButtonPress.Double -> xyFinderListener.buttonDoublePressed(this@XYFinderBluetoothDevice)
                                ButtonPress.Long -> xyFinderListener.buttonLongPressed(this@XYFinderBluetoothDevice)
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    open class Listener : XYIBeaconBluetoothDevice.Listener() {
        open fun buttonSinglePressed(device: XYFinderBluetoothDevice) {}

        open fun buttonDoublePressed(device: XYFinderBluetoothDevice) {}

        open fun buttonLongPressed(device: XYFinderBluetoothDevice) {}
    }

    companion object : XYBase() {

        fun enable(enable: Boolean, canCreate: Boolean? = null) {
            this.canCreate = canCreate ?: this.canCreate
            if (enable) {
                XYIBeaconBluetoothDevice.enable(true)
            }
        }

        fun buttonPressFromInt(index: Int): ButtonPress {
            return when (index) {
                1 -> ButtonPress.Single
                2 -> ButtonPress.Double
                3 -> ButtonPress.Long
                else -> {
                    ButtonPress.None
                }
            }
        }

        var canCreate = false

        internal fun addCreator(uuid: UUID, creator: XYCreator) {
            XYIBeaconBluetoothDevice.uuidToCreator[uuid] = XYFinderBluetoothDevice.creator
            uuidToCreator[uuid] = creator
        }

        internal fun removeCreator(uuid: UUID) {
            uuidToCreator.remove(uuid)
        }

        private val uuidToCreator = HashMap<UUID, XYCreator>()

        private val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {

                val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
                if (bytes != null) {
                    val buffer = ByteBuffer.wrap(bytes)
                    buffer.position(2) // skip the type and size

                    // get uuid
                    val high = buffer.long
                    val low = buffer.long
                    val uuidFromScan = UUID(high, low)

                    for ((uuid, creator) in uuidToCreator) {
                        if (uuid == uuidFromScan) {
                            creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)
                            return
                        }
                    }
                }

                val hash = hashFromScanResult(scanResult)

                if (canCreate) {
                    foundDevices[hash] = globalDevices[hash] ?: XYFinderBluetoothDevice(context, scanResult, hash)
                }
            }
        }

        internal fun hashFromScanResult(scanResult: XYScanResult): String {
            return XYIBeaconBluetoothDevice.hashFromScanResult(scanResult)
        }
    }
}
