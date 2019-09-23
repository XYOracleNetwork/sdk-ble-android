package network.xyo.ble.services.xy

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class ControlService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val buzzer = IntegerCharacteristic(this, Characteristics.Buzzer.uuid, "Buzzer")
    val handshake = IntegerCharacteristic(this, Characteristics.Handshake.uuid, "Handshake")
    val version = StringCharacteristic(this, Characteristics.Version.uuid, "Version")
    val buzzerSelect = IntegerCharacteristic(this, Characteristics.BuzzerSelect.uuid, "Buzzer Select")
    val surge = IntegerCharacteristic(this, Characteristics.Surge.uuid, "Surge")
    val button = IntegerCharacteristic(this, Characteristics.Button.uuid, "Button")
    val disconnect = IntegerCharacteristic(this, Characteristics.Disconnect.uuid, "Disconnect")

    companion object {
        val uuid: UUID = UUID.fromString("F014ED15-0439-3000-E001-00001001FFFF")

        enum class Characteristics(val uuid: UUID) {
            Buzzer(UUID.fromString("F014FFF1-0439-3000-E001-00001001FFFF")),
            Handshake(UUID.fromString("F014FFF2-0439-3000-E001-00001001FFFF")),
            Version(UUID.fromString("F014FFF4-0439-3000-E001-00001001FFFF")),
            BuzzerSelect(UUID.fromString("F014FFF6-0439-3000-E001-00001001FFFF")),
            Surge(UUID.fromString("F014FFF7-0439-3000-E001-00001001FFFF")),
            Button(UUID.fromString("F014FFF8-0439-3000-E001-00001001FFFF")),
            Disconnect(UUID.fromString("F014FFF9-0439-3000-E001-00001001FFFF"))
        }
    }
}