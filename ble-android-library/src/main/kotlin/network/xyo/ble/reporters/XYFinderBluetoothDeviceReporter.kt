package network.xyo.ble.reporters

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.devices.xy.XYFinderBluetoothDeviceButtonPress
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.reporters.XYBluetoothDeviceReporter
import network.xyo.ble.generic.gatt.peripheral.ble

open class XYFinderBluetoothDeviceReporter<T: XYFinderBluetoothDevice>: XYBluetoothDeviceReporter<XYBluetoothDevice, XYBluetoothDeviceListener>() {
    open fun buttonPressed(device: T, state: XYFinderBluetoothDeviceButtonPress) {
        log.info("reportButtonPressed")
        ble.launch {
            synchronized(listeners) {
                for (listener in listeners) {
                    val xyFinderListener = listener.value as? XYFinderBluetoothDeviceListener
                    if (xyFinderListener != null) {
                        log.info("reportButtonPressed: $xyFinderListener")
                        ble.launch {
                            when (state) {
                                XYFinderBluetoothDeviceButtonPress.Single -> xyFinderListener.buttonSinglePressed(device)
                                XYFinderBluetoothDeviceButtonPress.Double -> xyFinderListener.buttonDoublePressed(device)
                                XYFinderBluetoothDeviceButtonPress.Long -> xyFinderListener.buttonLongPressed(device)
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
