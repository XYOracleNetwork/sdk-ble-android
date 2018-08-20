package network.xyo.ble.sample

import android.app.Application
import android.os.Build
import network.xyo.ble.devices.*
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.ble.scanner.XYFilteredSmartScanLegacy
import network.xyo.ble.scanner.XYFilteredSmartScanModern
import network.xyo.core.XYBase

class XYApplication : Application() {
    private var _scanner: XYFilteredSmartScan? = null
    val scanner: XYFilteredSmartScan
        get() {
            if (_scanner == null) {
                _scanner = if (Build.VERSION.SDK_INT >= 21) {
                    XYFilteredSmartScanModern(this.applicationContext)
                } else {
                    XYFilteredSmartScanLegacy(this.applicationContext)
                }
            }
            return _scanner!!
        }

    override fun onCreate() {
        XYBase.logInfo("XYApplication", "onCreate")
        super.onCreate()

        XYAppleBluetoothDevice.enable(true)
        XYIBeaconBluetoothDevice.enable(true)
        XYFinderBluetoothDevice.enable(true)
        XY4BluetoothDevice.enable(true)
        XY3BluetoothDevice.enable(true)
        XY2BluetoothDevice.enable(true)
        XYGpsBluetoothDevice.enable(true)
        scanner.start()
    }

    override fun onTerminate() {
        XYBase.logInfo("XYApplication", "onTerminate")
        scanner.stop()
        super.onTerminate()
    }

}