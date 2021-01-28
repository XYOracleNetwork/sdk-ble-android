package network.xyo.ble.listeners

import androidx.annotation.WorkerThread
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDeviceListener
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice

open class XYFinderBluetoothDeviceListener : XYIBeaconBluetoothDeviceListener() {
    @WorkerThread
    open fun buttonSinglePressed(device: XYFinderBluetoothDevice) {}

    @WorkerThread
    open fun buttonDoublePressed(device: XYFinderBluetoothDevice) {}

    @WorkerThread
    open fun buttonLongPressed(device: XYFinderBluetoothDevice) {}
}
