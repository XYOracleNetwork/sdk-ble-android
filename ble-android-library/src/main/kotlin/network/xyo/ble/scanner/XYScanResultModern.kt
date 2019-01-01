package network.xyo.ble.scanner

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcel

@TargetApi(21)
class XYScanResultModern(private val scanResult:ScanResult): XYScanResult() {

    override val timestampNanos : Long
        get() {
            return scanResult.timestampNanos
        }

    override val rssi : Int
        get() {
            return scanResult.rssi
        }

    override val scanRecord : XYScanRecord?
        get() {
            val scanRecord = scanResult.scanRecord
            if (scanRecord != null) {
                return XYScanRecordModern(scanRecord)
            }
            return null
        }

    override val device : BluetoothDevice
        get() {
            return scanResult.device
        }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        scanResult.writeToParcel(dest, flags)
    }

    override fun describeContents() : Int {
        return scanResult.describeContents()
    }

    override fun hashCode() : Int {
        return scanResult.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return scanResult.equals(other)
    }

    override fun toString(): String {
        return scanResult.toString()
    }

}