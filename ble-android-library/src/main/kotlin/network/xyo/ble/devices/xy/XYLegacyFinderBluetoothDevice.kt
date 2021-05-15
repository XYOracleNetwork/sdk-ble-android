package network.xyo.ble.devices.xy

import android.content.Context
import network.xyo.base.XYBase
import network.xyo.ble.devices.apple.XYAppleBluetoothDevice
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.services.standard.BatteryService
import network.xyo.ble.generic.services.standard.TxPowerService
import network.xyo.ble.services.xy.BasicConfigService
import network.xyo.ble.services.xy.ControlService
import network.xyo.ble.services.xy.ExtendedConfigService
import network.xyo.ble.services.xy.SensorService
import java.nio.ByteBuffer
import java.util.*


open class XYLegacyFinderBluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val batteryService by lazy { BatteryService(this) }
    val txPowerService by lazy { TxPowerService(this) }

    val basicConfigService by lazy { BasicConfigService(this) }
    val controlService by lazy { ControlService(this) }
    val extendedConfigService by lazy { ExtendedConfigService(this) }
    val sensorService by lazy { SensorService(this) }

    override suspend fun find(song: UByte?) = connection {
        return@connection controlService.buzzerSelect.set(song ?: 0x02U)
    }

    override suspend fun stopFind() = connection {
        return@connection controlService.buzzerSelect.set(0xffU)
    }

    override val prefix = "xy:ibeacon"

    companion object : XYBase() {

        private fun majorFromScanResult(scanResult: XYScanResult): Int? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(18).toInt()
            } else {
                null
            }
        }

        private fun minorFromScanResult(scanResult: XYScanResult): Int? {
            val bytes = scanResult.scanRecord?.getManufacturerSpecificData(XYAppleBluetoothDevice.MANUFACTURER_ID)
            return if (bytes != null) {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.getShort(20).toInt().and(0xfff0).or(0x0004)
            } else {
                null
            }
        }

        fun hashFromScanResult(scanResult: XYScanResult): String {
            val uuid = iBeaconUuidFromScanResult(scanResult)
            val major = majorFromScanResult(scanResult)
            val minor = minorFromScanResult(scanResult)
            return "$uuid:$major:$minor"
        }
    }
}
