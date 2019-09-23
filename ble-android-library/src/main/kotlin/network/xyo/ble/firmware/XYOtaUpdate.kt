package network.xyo.ble.firmware

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.base.XYBase

open class XYOtaUpdate: XYBase() {
    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
        open fun progress(sent: Int, total: Int) {}
    }
}