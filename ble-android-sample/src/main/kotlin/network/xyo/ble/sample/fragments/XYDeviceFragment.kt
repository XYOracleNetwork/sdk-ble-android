package network.xyo.ble.sample.fragments

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData

abstract class XYDeviceFragment : XYAppBaseFragment() {
    var device : XYBluetoothDevice? = null
    var deviceData : XYDeviceData? = null
}