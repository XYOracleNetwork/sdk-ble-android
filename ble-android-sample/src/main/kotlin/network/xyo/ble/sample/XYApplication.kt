package network.xyo.ble.sample

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.ble
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.generic.scanner.XYSmartScanModern

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

        XYAppleBluetoothDevice.enable(true)
        XYIBeaconBluetoothDevice.enable(true, canCreate = true)
        XYFinderBluetoothDevice.enable(true, canCreate = true)
        XY4BluetoothDevice.enable(true)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            log.error("Exception Thread: $t")
            log.error(e)
        }
    }

    override fun onTerminate() {
        log.info("onTerminate")
        ble.launch {
            scanner.stop()
        }
        super.onTerminate()
    }

    companion object : XYBase()
}
