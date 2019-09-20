package network.xyo.ble.scanner

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.XYPromise
import network.xyo.base.XYBase

@kotlin.ExperimentalUnsignedTypes
class XYSmartScanPromiseWrapper(val scanner: XYSmartScan): XYBase() {

    fun start() {
        start(null)
    }

    fun start(promise: XYPromise<Boolean>? = null){
        GlobalScope.launch {
            val result = scanner.start().await()
            promise?.resolve(result)
        }
    }

    fun stop() {
        stop(null)
    }

    fun stop(promise: XYPromise<Boolean>? = null){
        GlobalScope.launch {
            val result = scanner.stop().await()
            promise?.resolve(result)
        }
    }
}