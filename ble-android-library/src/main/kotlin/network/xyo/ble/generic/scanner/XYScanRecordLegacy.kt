package network.xyo.ble.generic.scanner

import android.annotation.TargetApi
import android.os.ParcelUuid
import android.util.SparseArray
import network.xyo.ble.generic.bluetooth.ScanRecordLegacy

@kotlin.ExperimentalUnsignedTypes
@kotlin.ExperimentalStdlibApi
@TargetApi(18)
internal class XYScanRecordLegacy(private val scanRecord: ScanRecordLegacy) : XYScanRecord() {

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

    override val serviceUuids : List<ParcelUuid>?
        get() {
            return scanRecord.serviceUuids
        }

    override val manufacturerSpecificData : SparseArray<ByteArray>
        get() {
            return scanRecord.manufacturerSpecificData
        }

    override val serviceData : Map<ParcelUuid, ByteArray>
        get() {
            return scanRecord.serviceData
        }

    override fun getManufacturerSpecificData(manufacturerId: Int): ByteArray? {
        return scanRecord.getManufacturerSpecificData(manufacturerId)
    }

    override fun getServiceData(serviceDataUuid: ParcelUuid?): ByteArray? {
        return getServiceData(serviceDataUuid)
    }

    override fun toString() : String {
        return scanRecord.toString()
    }

    companion object {
        fun parseFromBytes(bytes: ByteArray) : XYScanRecord? {
            val scanRecord = ScanRecordLegacy.parseFromBytes(bytes)
            if (scanRecord != null) {
                return XYScanRecordLegacy(scanRecord)
            }
            return null
        }
    }
}