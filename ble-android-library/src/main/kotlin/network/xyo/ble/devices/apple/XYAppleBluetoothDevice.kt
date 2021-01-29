package network.xyo.ble.devices.apple

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.SparseArray
import java.util.concurrent.ConcurrentHashMap
import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.scanner.XYScanResult

/**
 * Listener for Apple Bluetooth Devices.
 *
 * Brings in a renamed Listener.
 * .listener is now camel cased into the name.
 */

open class XYAppleBluetoothDevice(context: Context, device: BluetoothDevice, hash: String) : XYBluetoothDevice(context, device, hash) {

    companion object : XYBase() {

        const val MANUFACTURER_ID = 0x004c

        var canCreate = false

        fun enable(enable: Boolean) {
            if (enable) {
                manufacturerToCreator.append(MANUFACTURER_ID, creator)
            } else {
                manufacturerToCreator.remove(MANUFACTURER_ID)
            }
        }

        internal val typeToCreator = SparseArray<XYCreator>()

        private val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {

                for (i in 0 until typeToCreator.size()) {
                    val typeId = typeToCreator.keyAt(i)
                    val bytes = scanResult.scanRecord?.getManufacturerSpecificData(MANUFACTURER_ID)
                    if (bytes != null) {
                        if (bytes[0] == typeId.toByte()) {
                            typeToCreator.get(typeId)?.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)
                            return
                        }
                    }
                }

                val hash = hashFromScanResult(scanResult)
                val device = scanResult.device

                if (canCreate && device != null) {
                    foundDevices[hash] = globalDevices[hash] ?: XYAppleBluetoothDevice(context, device, hash)
                }
            }
        }

        fun hashFromScanResult(scanResult: XYScanResult): String {
            return scanResult.address
        }
    }
}
