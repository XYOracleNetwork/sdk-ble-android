package network.xyo.ble.devices.xy

import android.content.Context
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYCreator
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.*
import network.xyo.ble.listeners.XYFinderBluetoothDeviceListener
import network.xyo.ble.services.xy.*

/**
 * Listener for XY2 Devices.
 *
 * Brings in a renamed Finder Listener.
 * .listener is now camel cased into the name.
 */

@kotlin.ExperimentalUnsignedTypes
open class XY2BluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYLegacyFinderBluetoothDevice(context, scanResult, hash) {

    override suspend fun find(song: UByte?) = connection {
        return@connection controlService.buzzerSelect.set(song ?: 0x02U)
    }

    override suspend fun stopFind() = connection {
        return@connection controlService.buzzerSelect.set(0xffU)
    }

    override val prefix = "xy:ibeacon"

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("07775dd0-111b-11e4-9191-0800200c9a66")!!

        fun enable(enable: Boolean) {
            if (enable) {
                XYFinderBluetoothDevice.enable(true)
                addCreator(FAMILY_UUID, creator)
            } else {
                removeCreator(FAMILY_UUID)
            }
        }

        private val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {
                val hash = hashFromScanResult(scanResult)
                foundDevices[hash] = globalDevices[hash]
                        ?: XY2BluetoothDevice(context, scanResult, hash)
            }
        }
    }
}
