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
import network.xyo.ble.services.xy.*

@kotlin.ExperimentalUnsignedTypes
open class XY2BluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    val batteryService by lazy { BatteryService(this) }
    val deviceInformationService by lazy { DeviceInformationService(this) }
    val genericAccessService by lazy { GenericAccessService(this) }
    val genericAttributeService by lazy { GenericAttributeService(this) }
    val txPowerService by lazy { TxPowerService(this) }

    val basicConfigService by lazy { BasicConfigService(this) }
    val controlService by lazy { ControlService(this) }
    val csrOtaService by lazy { CsrOtaService(this) }
    val extendedConfigService by lazy { ExtendedConfigService(this) }
    val sensorService by lazy { SensorService(this) }

    override suspend fun find() = connection {
        return@connection controlService.buzzerSelect.set(0x02U)
    }

    override suspend fun stopFind() = connection {
        return@connection controlService.buzzerSelect.set(0xffU)
    }

    override val prefix = "xy:ibeacon"

    open class Listener : XYFinderBluetoothDevice.Listener()

    companion object : XYBase() {

        private val FAMILY_UUID = UUID.fromString("07775dd0-111b-11e4-9191-0800200c9a66")!!

        /*val DEFAULT_LOCK_CODE = ubyteArrayOf(
                0x2fU, 0xbeU, 0xa2U, 0x07U, 0x52U, 0xfeU, 0xbfU, 0x31U, 0x1dU, 0xacU, 0x5dU, 0xfaU, 0x7dU, 0x77U, 0x76U, 0x80U
        )*/

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
