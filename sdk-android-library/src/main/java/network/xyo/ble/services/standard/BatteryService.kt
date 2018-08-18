package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class BatteryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val level = IntegerCharacteristic(this, Characteristics.Level.uuid)

    companion object {
        val uuid: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            Level(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"))
        }
    }
}