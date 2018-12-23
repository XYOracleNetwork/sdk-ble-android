package network.xyo.ble.scanner

import android.content.Context

class XYFilteredSmartScanLegacy(context: Context) : XYFilteredSmartScan(context) {
    override suspend fun start(): Boolean {
        super.start()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return true
    }

    override suspend fun stop(): Boolean {
        super.stop()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return true
    }
}