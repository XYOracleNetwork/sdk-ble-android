package network.xyo.ble.listeners

import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener

open class XYIBeaconBluetoothDeviceListener : XYBluetoothDeviceListener() {
    open fun iBeaconDetected(uuid: String, major: UShort, minor: UShort) {
    }
}
