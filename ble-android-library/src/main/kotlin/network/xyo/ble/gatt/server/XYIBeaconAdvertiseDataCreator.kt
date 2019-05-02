package network.xyo.ble.gatt.server

import android.bluetooth.le.AdvertiseData
import java.nio.ByteBuffer
import java.util.*

object XYIBeaconAdvertiseDataCreator {

    fun create(major: ByteArray, minor: ByteArray, serviceUuid: UUID, manufacturerId: Int, includeDeviceName: Boolean): AdvertiseData.Builder {
        if (major.size != 2) throw Exception("IBeacon major must be 2 bytes!")
        if (minor.size != 2) throw Exception("IBeacon major must be 2 bytes!")

        val buffer = ByteBuffer.allocate(23)
        buffer.put(byteArrayOf(0x02.toByte(), 0x15.toByte()))
        buffer.putLong(serviceUuid.mostSignificantBits)
        buffer.putLong(serviceUuid.leastSignificantBits)
        buffer.put(major)
        buffer.put(minor)

        buffer.put(-51) // todo fid distance at one meter, for now this is an average of all android devices

        val builder = AdvertiseData.Builder()
        builder.addManufacturerData(manufacturerId, buffer.array())
        return builder.setIncludeDeviceName(includeDeviceName)

    }

}