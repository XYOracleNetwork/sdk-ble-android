package network.xyo.ble.devices.apple

import android.content.Context
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.scanner.XYScanResult

/**
 * Listener for IBeacon.
 *
 * Brings in a renamed Listener.
 * .listener is now camel cased into the name.
 */
open class XYIBeaconBluetoothDeviceListener : XYAppleBluetoothDeviceListener() {
    open fun onIBeaconDetect(uuid: String, major: UShort, minor: UShort) {
    }
}

@kotlin.ExperimentalUnsignedTypes
open class XYIBeaconBluetoothDevice(context: Context, val scanResult: XYScanResult?, hash: String, transport: Int? = null) :
    XYBluetoothDevice(context, scanResult?.device, hash, transport) {

    private var uuidValue: UUID
    open val uuid: UUID
        get() {
            return uuidValue
        }

    protected var majorValue: UShort
    open val major: UShort
    get() {
        return majorValue
    }

    protected var minorValue: UShort
    open val minor: UShort
    get() {
        return minorValue
    }

    val power: Byte

    override val id: String
        get() {
            return "$uuid:$major.$minor"
        }

    init {
        val bytes = scanResult?.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
        if (bytes != null && bytes.size >= 23) {
            val buffer = ByteBuffer.wrap(bytes)
            buffer.position(2) // skip the type and size

            // get uuid
            val high = buffer.long
            val low = buffer.long
            uuidValue = UUID(high, low)

            majorValue = buffer.short.toUShort()
            minorValue = buffer.short.toUShort()
            power = buffer.get()
        } else {
            uuidValue = UUID(0, 0)
            majorValue = 0.toUShort()
            minorValue = 0.toUShort()
            power = 0
        }
    }

    open val distance: Double?
        get() {
            val rssi = rssi ?: return null
            val dist: Double
            val ratio: Double = rssi * 1.0 / power
            dist = if (ratio < 1.0) {
                ratio.pow(10.0)
            } else {
                (0.89976) * ratio.pow(7.7095) + 0.111
            }
            return dist
        }

    override fun compareTo(other: XYBluetoothDevice): Int {
        if (other is XYIBeaconBluetoothDevice) {
            val d1 = distance
            val d2 = other.distance
            if (d1 == null) {
                if (d2 == null) {
                    return 0
                }
                return -1
            }
            return when {
                d2 == null -> 1
                d1 == d2 -> 0
                d1 > d2 -> 1
                else -> -1
            }
        } else {
            return super.compareTo(other)
        }
    }

    companion object : XYBase() {

        private const val APPLE_IBEACON_ID = 0x02

        var canCreate = false

        fun enable(enable: Boolean, canCreate: Boolean? = null) {
            this.canCreate = canCreate ?: this.canCreate
            if (enable) {
                XYAppleBluetoothDevice.enable(true)
                XYAppleBluetoothDevice.typeToCreator.append(APPLE_IBEACON_ID, creator)
            } else {
                XYAppleBluetoothDevice.typeToCreator.remove(APPLE_IBEACON_ID)
            }
        }

        fun iBeaconUuidFromScanResult(scanResult: XYScanResult): UUID? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.position(2) // skip the type and size

                try {
                    // get uuid
                    val high = buffer.long
                    val low = buffer.long
                    UUID(high, low)
                } catch (ex: BufferUnderflowException) {
                    // can throw a BufferUnderflowException if the beacon sends an invalid value for UUID.
                    log.error("BufferUnderflowException: $ex", true)
                    return null
                }
            } else {
                null
            }
        }

        val uuidToCreator = HashMap<UUID, XYCreator>()

        private val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(
                context: Context,
                scanResult: XYScanResult,
                globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>,
                foundDevices: HashMap<String, XYBluetoothDevice>
            ) {
                for ((uuid, creator) in uuidToCreator) {
                    val bytes =
                        scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
                    if (bytes != null) {
                        if (uuid == iBeaconUuidFromScanResult(scanResult)) {
                            creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)
                            return
                        }
                    }
                }

                val hash = hashFromScanResult(scanResult)

                if (canCreate) {
                    foundDevices[hash] = globalDevices[hash] ?: XYIBeaconBluetoothDevice(context, scanResult, hash)
                }
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

        private fun minorFromScanResult(scanResult: XYScanResult): UShort? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(20).toUShort()
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
