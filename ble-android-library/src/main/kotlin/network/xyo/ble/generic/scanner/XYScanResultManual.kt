package network.xyo.ble.generic.scanner

import android.bluetooth.BluetoothDevice
import android.os.Parcel

internal class XYScanResultManual(
    private val _device: BluetoothDevice?,
    private val _rssi: Int,
    private val _scanRecord: XYScanRecord?,
    private val _timestampNanos: Long
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
