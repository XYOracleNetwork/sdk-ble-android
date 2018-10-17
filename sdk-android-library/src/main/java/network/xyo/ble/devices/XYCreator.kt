package network.xyo.ble.devices

import android.content.Context
import network.xyo.ble.scanner.XYScanResult
import network.xyo.core.XYBase
import java.util.concurrent.ConcurrentHashMap

abstract class XYCreator : XYBase() {
    //create a device object of best fit
    //we pass in the devices list to prevent garbage collection hell
    abstract fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<Int, XYBluetoothDevice>, foundDevices: HashMap<Int, XYBluetoothDevice>)
}