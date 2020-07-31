package network.xyo.ble.services.xy

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class ControlServiceCharacteristics(val uuid: UUID) {
    Buzzer(UUID.fromString("F014FFF1-0439-3000-E001-00001001FFFF")),
    Handshake(UUID.fromString("F014FFF2-0439-3000-E001-00001001FFFF")),
    Version(UUID.fromString("F014FFF4-0439-3000-E001-00001001FFFF")),
    BuzzerSelect(UUID.fromString("F014FFF6-0439-3000-E001-00001001FFFF")),
    Surge(UUID.fromString("F014FFF7-0439-3000-E001-00001001FFFF")),
    Button(UUID.fromString("F014FFF8-0439-3000-E001-00001001FFFF")),
    Disconnect(UUID.fromString("F014FFF9-0439-3000-E001-00001001FFFF"))
}

class ControlService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val buzzer = ByteCharacteristic(this, ControlServiceCharacteristics.Buzzer.uuid, "Buzzer")
    val handshake = ByteCharacteristic(this, ControlServiceCharacteristics.Handshake.uuid, "Handshake")
    val version = StringCharacteristic(this, ControlServiceCharacteristics.Version.uuid, "Version")
    val buzzerSelect = ByteCharacteristic(this, ControlServiceCharacteristics.BuzzerSelect.uuid, "Buzzer Select")
    val surge = ByteCharacteristic(this, ControlServiceCharacteristics.Surge.uuid, "Surge")
    val button = ByteCharacteristic(this, ControlServiceCharacteristics.Button.uuid, "Button")
    val disconnect = ByteCharacteristic(this, ControlServiceCharacteristics.Disconnect.uuid, "Disconnect")

    companion object {
        val uuid: UUID = UUID.fromString("F014ED15-0439-3000-E001-00001001FFFF")
    }
}
