package network.xyo.ble.gatt.server

import android.bluetooth.le.AdvertiseData
import android.content.Context
import java.nio.ByteBuffer

open class XYIBeaconAdvertiser (context: Context) : XYBluetoothAdvertiser(context) {

    var major : ByteArray = DEFAULT_MAJOR
        set(value) {
            if (major.size == 2) {
                field = value
            }

            throw Exception("IBeacon major must be 2 bytes!")
        }

    var minor : ByteArray = DEFAULT_MINOR
        set(value) {
            if (major.size == 2) {
                field = value
            }

            throw Exception("IBeacon major must be 2 bytes!")
        }

    var identifier : ByteArray = DEFAULT_IDENTIFIER
        set(value) {
            if (major.size == 2) {
                field = value
            }

            throw Exception("IBeacon major must be 2 bytes!")
        }

    private fun makeManufacturerData (txLevel : Byte) : ByteArray {
        val buffer = ByteBuffer.allocate(24)
        buffer.put(identifier)                                                                      // 2 bytes
        buffer.putLong(primaryService?.uuid?.mostSignificantBits ?: DEFAULT_SERVICE_TOP)      // 8 bytes
        buffer.putLong(primaryService?.uuid?.leastSignificantBits ?: DEFAULT_SERVICE_BOTTOM)  // 8 bytes
        buffer.put(major)                                                                           // 2 bytes
        buffer.put(minor)                                                                           // 2 bytes
        buffer.put(txLevel)                                                                         // 1 byte
        return buffer.array()
    }


    override fun makeAdvertisingData(): AdvertiseData {
        val builder = AdvertiseData.Builder()
        builder.addManufacturerData(manufacturerId ?: DEFAULT_MANUFACTURER_ID, makeManufacturerData(-51))
        return builder.build()
    }


    companion object {
        private const val DEFAULT_SERVICE_TOP = 0L
        private const val DEFAULT_SERVICE_BOTTOM = 0L
        private const val DEFAULT_MANUFACTURER_ID = 0x004c // apple
        private val DEFAULT_MAJOR = byteArrayOf(0x00, 0x00)
        private val DEFAULT_MINOR = byteArrayOf(0x00, 0x00)
        private val DEFAULT_IDENTIFIER = byteArrayOf(0x00, 0x00)
    }

}