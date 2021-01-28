package network.xyo.ble.generic.listeners

import androidx.annotation.WorkerThread
import network.xyo.ble.generic.devices.XYBluetoothDevice

open class XYBluetoothDeviceListener {
    @WorkerThread
    open fun entered(device: XYBluetoothDevice) {}

    @WorkerThread
    open fun exited(device: XYBluetoothDevice) {}

    @WorkerThread
    open fun detected(device: XYBluetoothDevice) {}

    @WorkerThread
    open fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {}
}
