package network.xyo.ble.generic.scanner

import android.annotation.TargetApi
import android.bluetooth.le.ScanRecord
import android.os.ParcelUuid
import android.util.SparseArray

@TargetApi(21)
internal class XYScanRecordModern(private val scanRecord: ScanRecord) : XYScanRecord() {
    override val advertiseFlags: Int
        get() {
            return scanRecord.advertiseFlags
        }

    override val bytes: ByteArray
        get() {
            return scanRecord.bytes
        }

    override val deviceName: String?
        get() {
            return scanRecord.deviceName
        }

    override val txPowerLevel: Int
        get() {
            return scanRecord.txPowerLevel
        }

    override val serviceUuids: List<ParcelUuid>?
        get() {
            return scanRecord.serviceUuids
        }

    override val manufacturerSpecificData: SparseArray<ByteArray>
        get() {
            return scanRecord.manufacturerSpecificData
        }

    override val serviceData: Map<ParcelUuid, ByteArray>
        get() {
            return scanRecord.serviceData
        }

    override fun getManufacturerSpecificData(manufacturerId: Int): ByteArray? {
        return scanRecord.getManufacturerSpecificData(manufacturerId)
    }

    override fun getServiceData(serviceDataUuid: ParcelUuid?): ByteArray? {
        return scanRecord.getServiceData(serviceDataUuid)
    }

    override fun toString(): String {
        return scanRecord.toString()
    }
}
