package network.xyo.ble.firmware

import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice

open class XYOtaUpdate : XYBase() {
    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
        open fun progress(sent: Int, total: Int) {}
    }
}
