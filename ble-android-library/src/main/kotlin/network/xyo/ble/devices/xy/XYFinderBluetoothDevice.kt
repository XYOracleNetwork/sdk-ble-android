package network.xyo.ble.devices.xy

import android.content.Context
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.firmware.XYOtaUpdateListener
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.DeviceInformationService
import network.xyo.ble.generic.services.standard.GenericAccessService
import network.xyo.ble.generic.services.standard.GenericAttributeService
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.reporters.XYFinderBluetoothDeviceReporter

/**
 * Bluetooth device family options.
 *
 */
@Suppress("unused")
enum class XYFinderBluetoothDeviceFamily {
    Unknown,
    XY1,
    XY2,
    XY3,
    Mobile,
    Near,
    XY4,
    Webble
}

/**
 * Bluetooth device range values.
 *
 */
enum class XYFinderBluetoothDeviceProximity {
    None,
    OutOfRange,
    VeryFar,
    Far,
    Medium,
    Near,
    VeryNear,
    Touching
}

/**
 * Presses available on XY find it hardware.
 *
 * These values are assigned to start and stop the finder.
 */
enum class XYFinderBluetoothDeviceButtonPress(val state: Int) {
    None(0),
    Single(1),
    Double(2),
    Long(3)
}

/**
 * Stay awake, go to sleep toggle.
 *
 */
enum class XYFinderBluetoothDeviceStayAwake(val state: UByte) {
    Off(0U),
    On(1U)
}

@kotlin.ExperimentalUnsignedTypes
@Suppress("unused")
open class XYFinderBluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYIBeaconBluetoothDevice(context, scanResult, hash) {

    open val deviceInformationService by lazy { DeviceInformationService(this) }
    open val genericAccessService by lazy { GenericAccessService(this) }
    open val genericAttributeService by lazy { GenericAttributeService(this) }

    override val reporter = XYFinderBluetoothDeviceReporter<XYFinderBluetoothDevice>()

    override val id: String
        get() {
            return "$prefix:$uuid.${major.toInt()}.${minor.and(0xfff0.toUShort()).or(0x0004.toUShort()).toInt()}"
        }

    internal open val prefix = "xy:finder"

    val family: XYFinderBluetoothDeviceFamily
        get() {
            return when (this@XYFinderBluetoothDevice) {
                is XYMobileBluetoothDevice -> {
                    XYFinderBluetoothDeviceFamily.Mobile
                }
                is XY4BluetoothDevice -> {
                    XYFinderBluetoothDeviceFamily.XY4
                }
                is XY3BluetoothDevice -> {
                    XYFinderBluetoothDeviceFamily.XY3
                }
                else -> {
                    XYFinderBluetoothDeviceFamily.Unknown
                }
            }
        }

    // the distance is in meters, so these are what we subjectively think are the fuzzy proximity values
    val proximity: XYFinderBluetoothDeviceProximity
        get() {

            val distance = distance ?: return XYFinderBluetoothDeviceProximity.OutOfRange

            if (distance < 0.0) {
                return XYFinderBluetoothDeviceProximity.None
            }

            if (distance < 0.5) {
                return XYFinderBluetoothDeviceProximity.Touching
            }

            if (distance < 15) {
                return XYFinderBluetoothDeviceProximity.VeryNear
            }

            if (distance < 30) {
                return XYFinderBluetoothDeviceProximity.Near
            }

            if (distance < 60) {
                return XYFinderBluetoothDeviceProximity.Medium
            }

            if (distance < 120) {
                return XYFinderBluetoothDeviceProximity.Far
            }

            return XYFinderBluetoothDeviceProximity.VeryFar
        }

    // signal the user to where it is, usually make it beep
    open suspend fun find(song: UByte? = null) = connection {
        log.error(UnsupportedOperationException().toString(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    // turn off finding, if supported
    open suspend fun stopFind() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun lock() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<ByteArray>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun unlock() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<ByteArray>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun stayAwake() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun fallAsleep() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun restart() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    open suspend fun batteryLevel() = connection {
        log.error(UnsupportedOperationException(), true)
        return@connection XYBluetoothResult<UByte>(XYBluetoothResultErrorCode.Unsupported)
    }

    // update firmware at stream level
    open fun updateFirmware(stream: InputStream, listener: XYOtaUpdateListener) {
    }

    // update firmware at folder level
    open fun updateFirmware(folderName: String, filename: String, listener: XYOtaUpdateListener) {
    }

    open fun cancelUpdateFirmware() {
    }

    companion object : XYBase() {

        fun enable(enable: Boolean, canCreate: Boolean? = null) {
            this.canCreate = canCreate ?: this.canCreate
            if (enable) {
                XYIBeaconBluetoothDevice.enable(true)
            }
        }

        // grabs enum assigned to type of press from finder user. 
        fun buttonPressFromInt(index: Int): XYFinderBluetoothDeviceButtonPress {
            return when (index) {
                1 -> XYFinderBluetoothDeviceButtonPress.Single
                2 -> XYFinderBluetoothDeviceButtonPress.Double
                3 -> XYFinderBluetoothDeviceButtonPress.Long
                else -> {
                    XYFinderBluetoothDeviceButtonPress.None
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
