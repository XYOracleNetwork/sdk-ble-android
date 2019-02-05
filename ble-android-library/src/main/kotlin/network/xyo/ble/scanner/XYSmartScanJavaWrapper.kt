package network.xyo.ble.scanner

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class XYSmartScanJavaWrapper(val scanner: XYSmartScan) {

    abstract class Promise<T> {
        open fun resolve(value: T) {}
        open fun reject(error: String) {}
    }

    fun start() {
        start(null)
    }

    fun start(promise: Promise<Boolean>? = null){
        GlobalScope.launch {
            promise?.resolve(scanner.start().await())
        }
    }

    fun stop() {
        stop(null)
    }

    fun stop(promise: Promise<Boolean>? = null){
        GlobalScope.launch {
            promise?.resolve(scanner.stop().await())
        }
    }
}