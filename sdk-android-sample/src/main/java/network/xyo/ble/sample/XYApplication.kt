package network.xyo.ble.sample

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.os.Build
import android.util.Log
import network.xyo.ble.devices.*
import network.xyo.ble.gatt.server.*
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.ble.scanner.XYFilteredSmartScanLegacy
import network.xyo.ble.scanner.XYFilteredSmartScanModern
import network.xyo.core.XYBase
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

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