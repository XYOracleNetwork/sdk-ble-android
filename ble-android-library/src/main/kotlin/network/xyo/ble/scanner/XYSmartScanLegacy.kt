package network.xyo.ble.scanner

import android.content.Context

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