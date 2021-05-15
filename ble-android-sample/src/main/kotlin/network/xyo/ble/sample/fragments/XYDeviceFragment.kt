package network.xyo.ble.sample.fragments

import androidx.viewbinding.ViewBinding
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData


abstract class XYDeviceFragment<T>(var device: XYBluetoothDevice, var deviceData : XYDeviceData) : XYAppBaseFragment<T>() where T: ViewBinding
