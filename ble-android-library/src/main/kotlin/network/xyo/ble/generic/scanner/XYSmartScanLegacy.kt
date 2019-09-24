package network.xyo.ble.generic.scanner

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

@kotlin.ExperimentalUnsignedTypes
class XYSmartScanLegacy(context: Context) : XYSmartScan(context) {
    override suspend fun start() = GlobalScope.async {
        super.start()
        return@async true
    }.await()

    override suspend fun stop() = GlobalScope.async {
        super.stop()
        return@async true
    }.await()
}