package network.xyo.ble.generic.reporters

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener

open class XYBluetoothDeviceReporter<T: XYBluetoothDevice, L: XYBluetoothDeviceListener>: XYBase() {
    val listeners = HashMap<String, XYBluetoothDeviceListener>()

    fun addListener(key: String, listener: L) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners[key] = listener
            }
        }
    }

    fun removeListener(key: String) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners.remove(key)
            }
        }
    }

    open fun enter(device: T) {
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.entered(device)
                }
            }
        }
    }

    open fun exit(device: T) {
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.exited(device)
                }
            }
        }
    }

    open fun detected(device: T) {
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.detected(device)
                }
            }
        }
    }

    open fun connectionStateChanged(device: T, newState: Int) {
        log.info("connectionStateChanged: ${device.className} : $newState")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.connectionStateChanged(device, newState)
                }
            }
        }
    }
}
