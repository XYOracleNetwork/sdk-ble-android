package network.xyo.ble.sample

import android.app.Application
import android.os.Build
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.generic.scanner.XYSmartScanLegacy
import network.xyo.ble.generic.scanner.XYSmartScanModern
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.devices.xy.*
import network.xyo.ble.generic.devices.XYBluetoothDevice

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
