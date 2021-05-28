package network.xyo.ble.generic.scanner

import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.utilities.XYPromise
import network.xyo.ble.generic.gatt.peripheral.ble

// we use this to allow java code to access our coroutines

class XYSmartScanPromise(val scanner: XYSmartScan) : XYBase() {

    fun start() {
        start(null)
    }

    fun start(promise: XYPromise<Boolean>? = null) {
        ble.launch {
            val result = scanner.start()
            promise?.resolve(result)
        }
    }

    fun stop() {
        stop(null)
    }

    fun stop(promise: XYPromise<Boolean>? = null) {
        ble.launch {
            val result = scanner.stop()
            promise?.resolve(result)
        }
    }
}
