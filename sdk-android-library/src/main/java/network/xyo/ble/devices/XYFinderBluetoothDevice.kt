package network.xyo.ble.devices

import android.content.Context
import network.xyo.core.XYBase
import network.xyo.ble.gatt.XYBluetoothError
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import network.xyo.ble.scanner.XYScanResult
import kotlinx.coroutines.experimental.Deferred
import java.nio.ByteBuffer
import java.util.*

open class XYFinderBluetoothDevice(context: Context, scanResult: XYScanResult, hash: Int) : XYIBeaconBluetoothDevice(context, scanResult, hash), Comparable<XYFinderBluetoothDevice> {

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

    override fun compareTo(other: XYFinderBluetoothDevice): Int {
        val d1 = distance
        val d2 = other.distance
        if (d1 == null) {
            if (d2 == null) {
                return 0
            }
            return -1
        }
        when {
            d2== null -> return 1
            d1 == d2 -> return 0
            d1 > d2 -> return 1
            else -> return -1
        }
    }

    override val id : String
        get() {
            return "$prefix:$uuid:${major.toInt()}.${minor.and(0xfff0).or(0x0004).toInt()}"
        }

    internal open val prefix = "xy:finder"

    //the distance is in meters, so these are what we subjectively think are the fuzzy proximity values
    val proximity: Proximity
        get() {

            val distance = distance ?: return Proximity.OutOfRange

            if (distance < 0.0) {
                return Proximity.None
            }

            if (distance < 0.5) {
                return Proximity.Touching
            }

            if (distance < 2) {
                return Proximity.VeryNear
            }

            if (distance < 6) {
                return Proximity.Near
            }

            if (distance < 12) {
                return Proximity.Medium
            }

            if (distance < 24) {
                return Proximity.Far
            }

            return Proximity.VeryFar
        }

    //signal the user to where it is, usually make it beep
    open fun find() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    //turn off finding, if supported
    open fun stopFind() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun lock() : Deferred<XYBluetoothResult<ByteArray>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<ByteArray>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun unlock() : Deferred<XYBluetoothResult<ByteArray>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<ByteArray>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun stayAwake() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun fallAsleep() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun restart() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    open fun batteryLevel() : Deferred<XYBluetoothResult<Int>> {
        logException(UnsupportedOperationException(), true)
        return asyncBle {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Not Implemented"))
        }
    }

    open val distance : Double?
        get() {
            val rssi = rssi ?: return null
            val a = (power - rssi).toDouble()
            val b = a / (10.0f * 2.0f)
            return Math.pow(10.0, b)
        }

    open class Listener : XYIBeaconBluetoothDevice.Listener() {
        open fun buttonSinglePressed(device: XYFinderBluetoothDevice) {}

        open fun buttonDoublePressed(device: XYFinderBluetoothDevice) {}

        open fun buttonLongPressed(device: XYFinderBluetoothDevice) {}
    }

    companion object : XYBase() {

        fun enable(enable: Boolean) {
            if (enable) {
                XYIBeaconBluetoothDevice.enable(true)
            }
        }

        var canCreate = false

        internal fun addCreator(uuid: UUID, creator: XYCreator) {
            XYIBeaconBluetoothDevice.uuidToCreator[uuid] = this.creator
            uuidToCreator[uuid] = creator
        }

        internal fun removeCreator(uuid: UUID) {
            uuidToCreator.remove(uuid)
        }

        private val uuidToCreator = HashMap<UUID, XYCreator>()

        internal val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: HashMap<Int, XYBluetoothDevice>, foundDevices: HashMap<Int, XYBluetoothDevice>) {

                val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
                if (bytes != null) {
                    val buffer = ByteBuffer.wrap(bytes)
                    buffer.position(2) //skip the type and size

                    // get uuid
                    val high = buffer.getLong()
                    val low = buffer.getLong()
                    val uuidFromScan = UUID(high, low)

                    for ((uuid, creator) in uuidToCreator) {
                        if (uuid.equals(uuidFromScan)) {
                            creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)
                            return
                        }
                    }
                }

                val hash = hashFromScanResult(scanResult)

                if (canCreate && hash != null) {
                    foundDevices[hash] = globalDevices[hash] ?: XYFinderBluetoothDevice(context, scanResult, hash)
                }
            }
        }

        internal fun hashFromScanResult(scanResult: XYScanResult): Int? {
            return XYIBeaconBluetoothDevice.hashFromScanResult(scanResult)
        }

        val compareDistance = object : kotlin.Comparator<XYFinderBluetoothDevice> {
            override fun compare(o1: XYFinderBluetoothDevice?, o2: XYFinderBluetoothDevice?): Int {
                if (o1 == null || o2 == null) {
                    if (o1 != null && o2 == null) return -1
                    if (o2 != null && o1 == null) return 1
                    return 0
                }
                return o1.compareTo(o2)
            }
        }

        fun sortedList(devices: HashMap<Int, XYBluetoothDevice>) : List<XYFinderBluetoothDevice> {
            val result = ArrayList<XYFinderBluetoothDevice>()
            for ((_, device) in devices) {
                val deviceToAdd = device as? XYFinderBluetoothDevice
                if (deviceToAdd != null) {
                    result.add(deviceToAdd)
                }
            }
            return result.sortedWith(compareDistance)
        }
    }
}