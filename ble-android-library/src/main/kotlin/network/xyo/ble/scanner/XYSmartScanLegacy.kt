package network.xyo.ble.scanner

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class XYSmartScanLegacy(context: Context) : XYSmartScan(context) {
    override fun start() = GlobalScope.async {
        super.start()
        return@async true
    }

    override fun stop() = GlobalScope.async {
        super.stop()
        return@async true
    }
}