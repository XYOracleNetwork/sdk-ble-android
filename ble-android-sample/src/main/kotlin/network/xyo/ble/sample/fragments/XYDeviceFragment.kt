package network.xyo.ble.sample.fragments

import androidx.viewbinding.ViewBinding
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData

@kotlin.ExperimentalUnsignedTypes
abstract class XYDeviceFragment<T> : XYAppBaseFragment<T>() where T: ViewBinding {
    var device : XYBluetoothDevice? = null
    var deviceData : XYDeviceData? = null
}
