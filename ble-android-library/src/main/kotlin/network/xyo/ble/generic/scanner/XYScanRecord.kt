package network.xyo.ble.generic.scanner

import android.os.ParcelUuid
import android.util.SparseArray
import network.xyo.base.XYBase

abstract class XYScanRecord: XYBase() {

    abstract val advertiseFlags : Int

    abstract val bytes : ByteArray

    abstract val deviceName : String?

    abstract val txPowerLevel : Int

    abstract val serviceUuids : List<ParcelUuid>?

    abstract val manufacturerSpecificData : SparseArray<ByteArray>

    abstract val serviceData : Map<ParcelUuid, ByteArray>

    abstract fun getManufacturerSpecificData(manufacturerId: Int): ByteArray?

    abstract fun getServiceData(serviceDataUuid: ParcelUuid?): ByteArray?
}