package network.xyo.ble.devices

import android.content.Context
import network.xyo.ble.scanner.XYScanResult
import network.xyo.core.XYBase
import unsigned.Ushort
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class XYIBeaconBluetoothDevice(context: Context, scanResult: XYScanResult?, hash: String) :
    XYBluetoothDevice(context, scanResult?.device, hash) {

    protected val _uuid: UUID
    open val uuid: UUID
        get() {
            return _uuid
        }

    protected var _major: Ushort
    open val major: Ushort
        get() {
            return _major
        }

    protected var _minor: Ushort
    open val minor: Ushort
        get() {
            return _minor
        }

    protected val _power: Byte
    open val power: Byte
        get() {
            return _power
        }

    override val id: String
        get() {
            return "$uuid:$major.$minor"
        }

    init {
        val bytes = scanResult?.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
        if (bytes != null && bytes.size >= 23) {
            val buffer = ByteBuffer.wrap(bytes)
            buffer.position(2) //skip the type and size

            //get uuid
            val high = buffer.long
            val low = buffer.long
            _uuid = UUID(high, low)

            _major = Ushort(buffer.short)
            _minor = Ushort(buffer.short)
            _power = buffer.get()
        } else {
            _uuid = UUID(0, 0)
            _major = Ushort(0)
            _minor = Ushort(0)
            _power = 0
        }
    }

    open class Listener : XYAppleBluetoothDevice.Listener() {
        open fun onIBeaconDetect(uuid: String, major: Ushort, minor: Ushort) {

        }
    }

    companion object : XYBase() {

        const val APPLE_IBEACON_ID = 0x02.toByte()

        var canCreate = false

        fun enable(enable: Boolean) {
            if (enable) {
                XYAppleBluetoothDevice.enable(true)
                XYAppleBluetoothDevice.typeToCreator[APPLE_IBEACON_ID] = creator
            } else {
                XYAppleBluetoothDevice.typeToCreator.remove(APPLE_IBEACON_ID)
            }
        }

        fun iBeaconUuidFromScanResult(scanResult: XYScanResult): UUID? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.position(2) //skip the type and size

                try {
                    //get uuid
                    val high = buffer.long
                    val low = buffer.long
                    UUID(high, low)
                } catch (ex: BufferUnderflowException) {
                    // can throw a BufferUnderflowException if the beacon sends an invalid value for UUID.
                    log.error("refreshGatt catch $ex", true)
                    return null
                }
            } else {
                null
            }
        }

        val uuidToCreator = HashMap<UUID, XYCreator>()

        internal val creator = object : XYCreator() {
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

        private fun majorFromScanResult(scanResult: XYScanResult): Ushort? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                Ushort(buffer.getShort(18).toInt())
            } else {
                null
            }
        }

        private fun minorFromScanResult(scanResult: XYScanResult): Ushort? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                Ushort(buffer.getShort(20).toInt())
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