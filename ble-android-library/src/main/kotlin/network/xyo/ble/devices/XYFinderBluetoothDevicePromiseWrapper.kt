package network.xyo.ble.devices

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.XYPromise
import network.xyo.ble.gatt.peripheral.XYBluetoothResult

//We add explicit parameterless functions since java does not like optionals

@kotlin.ExperimentalUnsignedTypes
class XYFinderBluetoothDevicePromiseWrapper(val device: XYFinderBluetoothDevice) {
    fun find() {
        find(null)
    }

    fun find(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.find()
            promise?.resolve(result)
        }
    }

    fun stopFind() {
        stopFind(null)
    }

    fun stopFind(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.stopFind()
            promise?.resolve(result)
        }
    }

    fun lock() {
        lock(null)
    }

    fun lock(promise: XYPromise<XYBluetoothResult<ByteArray>>?) {
        GlobalScope.launch {
            val result = device.lock()
            promise?.resolve(result)
        }
    }

    fun unlock() {
        unlock(null)
    }

    fun unlock(promise: XYPromise<XYBluetoothResult<ByteArray>>?) {
        GlobalScope.launch {
            val result = device.unlock()
            promise?.resolve(result)
        }
    }

    fun stayAwake() {
        stayAwake(null)
    }

    fun stayAwake(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.stayAwake()
            promise?.resolve(result)
        }
    }

    fun fallAsleep() {
        fallAsleep(null)
    }

    fun fallAsleep(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.fallAsleep()
            promise?.resolve(result)
        }
    }

    fun restart() {
        restart(null)
    }

    fun restart(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.restart()
            promise?.resolve(result)
        }
    }

    fun batteryLevel() {
        batteryLevel(null)
    }

    fun batteryLevel(promise: XYPromise<XYBluetoothResult<Int>>?) {
        GlobalScope.launch {
            val result = device.batteryLevel()
            promise?.resolve(result)
        }
    }
}