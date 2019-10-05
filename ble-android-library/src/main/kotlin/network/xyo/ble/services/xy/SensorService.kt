package network.xyo.ble.services.xy

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class SensorService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val raw = ByteCharacteristic(this, Characteristics.Raw.uuid, "Raw")
    val timeout = ByteCharacteristic(this, Characteristics.Timeout.uuid, "Timeout")
    val threshold = ByteCharacteristic(this, Characteristics.Threshold.uuid, "Threshold")
    val inactive = ByteCharacteristic(this, Characteristics.Inactive.uuid, "Inactive")
    val movementCount = ByteCharacteristic(this, Characteristics.MovementCount.uuid, "Movement Count")

    companion object {
        val uuid: UUID = UUID.fromString("F014DD00-0439-3000-E001-00001001FFFF")

        enum class Characteristics(val uuid: UUID) {
            Raw(UUID.fromString("F014DD01-0439-3000-E001-00001001FFFF")),
            Timeout(UUID.fromString("F014DD02-0439-3000-E001-00001001FFFF")),
            Threshold(UUID.fromString("F014DD03-0439-3000-E001-00001001FFFF")),
            Inactive(UUID.fromString("F014DD04-0439-3000-E001-00001001FFFF")),
            MovementCount(UUID.fromString("F014DD05-0439-3000-E001-00001001FFFF"))
        }
    }
}