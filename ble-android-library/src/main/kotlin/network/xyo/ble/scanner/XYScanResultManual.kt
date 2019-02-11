package network.xyo.ble.scanner

import android.bluetooth.BluetoothDevice
import android.os.Parcel

internal class XYScanResultManual (
        val _device: BluetoothDevice?,
        val _rssi: Int,
        val _scanRecord: XYScanRecord?,
        val _timestampNanos: Long
) : XYScanResult() {
    override val device: BluetoothDevice?
        get() = _device

    override val rssi: Int
        get() = _rssi

    override val scanRecord: XYScanRecord?
        get() = _scanRecord

    override val timestampNanos: Long
        get() = _timestampNanos

    override fun describeContents(): Int {
        log.error("describeContents: Not Implemented", true)
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        log.error("writeToParcel: Not Implemented", true)
    }
}