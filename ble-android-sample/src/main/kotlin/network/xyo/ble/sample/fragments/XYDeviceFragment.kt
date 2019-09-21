package network.xyo.ble.sample.fragments

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData

@kotlin.ExperimentalUnsignedTypes
abstract class XYDeviceFragment : XYAppBaseFragment() {
    var device : XYBluetoothDevice? = null
    var deviceData : XYDeviceData? = null
}