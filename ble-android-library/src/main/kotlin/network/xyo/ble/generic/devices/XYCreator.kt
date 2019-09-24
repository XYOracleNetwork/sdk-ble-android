package network.xyo.ble.generic.devices

import android.content.Context
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.base.XYBase
import java.util.concurrent.ConcurrentHashMap

abstract class XYCreator : XYBase() {
    //create a device object of best fit
    //we pass in the devices list to prevent garbage collection hell
    abstract fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>)
}