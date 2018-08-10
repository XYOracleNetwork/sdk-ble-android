package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class LinkLossService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val alertLevel = IntegerCharacteristic(this, characteristics.AlertLevel.uuid)

    companion object {
        val uuid = UUID.fromString("00001803-0000-1000-8000-00805F9B34FB")

        enum class characteristics(val uuid: UUID) {
            AlertLevel(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"))
        }
    }
}