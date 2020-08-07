package network.xyo.ble.sample

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.generic.scanner.XYSmartScanModern

@kotlin.ExperimentalUnsignedTypes
class XYApplication : Application() {
    private var _scanner: XYSmartScan? = null
    val scanner: XYSmartScan
        get() {
            if (_scanner == null) {
                _scanner = XYSmartScanModern(this.applicationContext)
            }
            return _scanner!!
        }

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            log.error("Exception Thread: $t")
            log.error(e)
        }
    }

    override fun onTerminate() {
        log.info("onTerminate")
        GlobalScope.launch {
            scanner.stop()
        }
        super.onTerminate()
    }

    companion object : XYBase()
}
