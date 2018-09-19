package network.xyo.ble.sample

import android.app.Application
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
import android.os.Build
import kotlinx.coroutines.experimental.async
import network.xyo.ble.devices.*
import network.xyo.ble.gatt.server.*
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.ble.scanner.XYFilteredSmartScanLegacy
import network.xyo.ble.scanner.XYFilteredSmartScanModern
import network.xyo.core.XYBase
import java.util.*

class XYApplication : Application() {
    private var bleServer : XYBluetoothGattServer? = null
    private var bleAdvertiser : XYBluetoothAdvertiser? = null
    private val characteristicRead = XYBluetoothReadCharacteristic(UUID.fromString("01ef8f90-e99f-48ae-87bb-f683b93c692f"))
    private val characteristicWrite = XYBluetoothWriteCharacteristic(UUID.fromString("02ef8f90-e99f-48ae-87bb-f683b93c692f"))

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
        startAdvertiser()
        createTestServer()
    }

    private fun startAdvertiser() = async {
        val advertiser = XYBluetoothAdvertiser(applicationContext)
        advertiser.changeContactable(true).await()
        advertiser.changeAdvertisingTxLevel(ADVERTISE_TX_POWER_HIGH).await()
        advertiser.changeIncludeDeviceName(true).await()
        bleAdvertiser = advertiser
        return@async advertiser.startAdvertising().await()
    }

    private fun createTestServer() = async {
        val server = XYBluetoothGattServer(applicationContext)
        val service = XYBluetoothService(UUID.fromString("3079ca44-ae64-4797-b4e5-a31e3304c481"), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        service.addCharacteristic(characteristicRead)
        service.addCharacteristic(characteristicWrite)
        server.startServer()
        bleServer = server
        return@async server.addService(service).await()
    }

    override fun onTerminate() {
        XYBase.logInfo("XYApplication", "onTerminate")
        scanner.stop()
        super.onTerminate()
    }
}