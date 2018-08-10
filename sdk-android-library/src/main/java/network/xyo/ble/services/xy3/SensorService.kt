package network.xyo.ble.services.xy3

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class SensorService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val raw = IntegerCharacteristic(this, characteristics.Raw.uuid)
    val timeout = IntegerCharacteristic(this, characteristics.Timeout.uuid)
    val threshold = IntegerCharacteristic(this, characteristics.Threshold.uuid)
    val inactive = IntegerCharacteristic(this, characteristics.Inactive.uuid)
    val movementCount = IntegerCharacteristic(this, characteristics.MovementCount.uuid)

    companion object {
        val uuid = UUID.fromString("F014DD00-0439-3000-E001-00001001FFFF")

        enum class characteristics(val uuid: UUID) {
            Raw(UUID.fromString("F014DD01-0439-3000-E001-00001001FFFF")),
            Timeout(UUID.fromString("F014DD02-0439-3000-E001-00001001FFFF")),
            Threshold(UUID.fromString("F014DD03-0439-3000-E001-00001001FFFF")),
            Inactive(UUID.fromString("F014DD04-0439-3000-E001-00001001FFFF")),
            MovementCount(UUID.fromString("F014DD05-0439-3000-E001-00001001FFFF"))
        }
    }
}