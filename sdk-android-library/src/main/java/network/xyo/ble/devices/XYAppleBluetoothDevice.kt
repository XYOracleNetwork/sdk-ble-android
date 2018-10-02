package network.xyo.ble.devices

import android.bluetooth.BluetoothDevice
import android.content.Context
import network.xyo.ble.scanner.XYScanResult
import network.xyo.core.XYBase
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class XYAppleBluetoothDevice(context: Context, device: BluetoothDevice, hash: Int) : XYBluetoothDevice(context, device, hash) {

    open class Listener : XYBluetoothDevice.Listener() {
    }

    companion object : XYBase() {

        const val MANUFACTURER_ID = 0x004c

        var canCreate = false

        fun enable(enable: Boolean) {
            if (enable) {
                manufacturerToCreator[MANUFACTURER_ID] = creator
            } else {
                manufacturerToCreator.remove(MANUFACTURER_ID)
            }
        }

        internal val typeToCreator = HashMap<Byte, XYCreator>()

        internal val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<Int, XYBluetoothDevice>, foundDevices: HashMap<Int, XYBluetoothDevice>) {
                for ((typeId, creator) in typeToCreator) {
                    val bytes = scanResult.scanRecord?.getManufacturerSpecificData(MANUFACTURER_ID)
                    if (bytes != null) {
                        if (bytes[0] == typeId) {
                            creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)
                            return
                        }
                    }
                }

                val hash = hashFromScanResult(scanResult)
                val device = scanResult.device

                if (canCreate && hash != null && device != null) {
                    foundDevices[hash] = globalDevices[hash] ?: XYAppleBluetoothDevice(context, device, hash)
                }
            }
        }

        fun hashFromScanResult(scanResult: XYScanResult): Int? {
            return scanResult.address.hashCode()
        }
    }
}