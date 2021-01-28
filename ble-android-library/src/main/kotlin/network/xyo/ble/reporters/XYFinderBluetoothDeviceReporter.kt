package network.xyo.ble.reporters

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.devices.xy.XYFinderBluetoothDeviceButtonPress
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.reporters.XYBluetoothDeviceReporter

open class XYFinderBluetoothDeviceReporter<T: XYFinderBluetoothDevice, L: XYFinderBluetoothDeviceListener>: XYBluetoothDeviceReporter<XYBluetoothDevice, XYBluetoothDeviceListener>() {
    open fun buttonPressed(device: XYFinderBluetoothDevice, state: XYFinderBluetoothDeviceButtonPress) {
        log.info("reportButtonPressed")
        GlobalScope.launch {
            synchronized(listeners) {
                for (listener in listeners) {
                    val xyFinderListener = listener.value as? XYFinderBluetoothDeviceListener
                    if (xyFinderListener != null) {
                        log.info("reportButtonPressed: $xyFinderListener")
                        GlobalScope.launch {
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
