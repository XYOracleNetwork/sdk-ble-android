package network.xyo.ble.generic.scanner

import android.content.Context

@kotlin.ExperimentalUnsignedTypes
class XYSmartScanLegacy(context: Context) : XYSmartScan(context) {
    override suspend fun start(): Boolean {
        super.start()
        return true
    }

    override suspend fun stop(): Boolean {
        super.stop()
        return true
    }
}
