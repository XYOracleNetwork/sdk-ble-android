package network.xyo.ble.sample

import android.app.Application
import android.os.Build
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.*
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.ble.scanner.XYSmartScanLegacy
import network.xyo.ble.scanner.XYSmartScanModern
import network.xyo.core.XYBase

class XYApplication : Application() {
    private var _scanner: XYSmartScan? = null
    val scanner: XYSmartScan
        get() {
            if (_scanner == null) {
                _scanner = if (Build.VERSION.SDK_INT >= 21) {
                    XYSmartScanModern(this.applicationContext)
                } else {
                    XYSmartScanLegacy(this.applicationContext)
                }
            }
            return _scanner!!
        }

    override fun onCreate() {
        super.onCreate()

        XYAppleBluetoothDevice.enable(true)
        XYIBeaconBluetoothDevice.enable(true)
        XYFinderBluetoothDevice.enable(true)
        XY4BluetoothDevice.enable(true)
        XY3BluetoothDevice.enable(true)
        XY2BluetoothDevice.enable(true)
        XYGpsBluetoothDevice.enable(true)

        GlobalScope.launch {
            scanner.start()
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