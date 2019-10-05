package network.xyo.ble.generic.services.standard

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class GenericAccessService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return TxPowerService.uuid
        }

    val deviceName = ByteCharacteristic(this, Characteristics.DeviceName.uuid, "Device Name")
    val appearance = ByteCharacteristic(this, Characteristics.Appearance.uuid, "Appearance")
    val privacyFlag = ByteCharacteristic(this, Characteristics.PrivacyFlag.uuid, "Privacy Flag")
    val reconnectionAddress = ByteCharacteristic(this, Characteristics.ReconnectionAddress.uuid, "Reconnection Address")
    val peripheralPreferredConnectionParameters = ByteCharacteristic(this, Characteristics.PeripheralPreferredConnectionParameters.uuid, "Peripheral Preferred Connection Parameters")


    companion object {
        val uuid: UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            DeviceName(                                 UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")),
            Appearance(                                 UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")),
            PrivacyFlag(                                UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb")),
            ReconnectionAddress(                        UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb")),
            PeripheralPreferredConnectionParameters(    UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb"))
        }
    }
}