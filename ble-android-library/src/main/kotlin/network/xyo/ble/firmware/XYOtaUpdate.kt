package network.xyo.ble.firmware

import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice

open class XYOtaUpdateListener {
    open fun updated(device: XYBluetoothDevice) {}
    open fun failed(device: XYBluetoothDevice, error: String) {}
    open fun progress(sent: Int, total: Int) {}
}

open class XYOtaUpdate : XYBase() {
}
