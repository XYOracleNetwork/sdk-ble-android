package network.xyo.ble.scanner

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.core.XYBase

class XYSmartScanJavaWrapper(val scanner: XYSmartScan): XYBase() {

    abstract class Promise<T> {
        open fun resolve(value: T?) {}
        open fun reject(error: String) {}
    }

    fun start() {
        start(null)
    }

    fun start(promise: Promise<Boolean>? = null){
        GlobalScope.launch {
            val result = scanner.start().await()
            promise?.resolve(result)
        }
    }

    fun stop() {
        stop(null)
    }

    fun stop(promise: Promise<Boolean>? = null){
        GlobalScope.launch {
            val result = scanner.stop().await()
            promise?.resolve(result)
        }
    }
}