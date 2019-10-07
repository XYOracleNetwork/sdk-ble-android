package network.xyo.ble.generic.devices

import android.content.Context
import java.util.concurrent.ConcurrentHashMap
import network.xyo.base.XYBase
import network.xyo.ble.generic.scanner.XYScanResult

abstract class XYCreator : XYBase() {
    // create a device object of best fit
    // we pass in the devices list to prevent garbage collection hell
    abstract fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>)
}
