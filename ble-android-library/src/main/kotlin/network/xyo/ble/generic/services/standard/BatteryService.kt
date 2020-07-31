package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class BatteryServiceCharacteristics(val uuid: UUID) {
    Level(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"))
}

class BatteryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val level = ByteCharacteristic(this, BatteryServiceCharacteristics.Level.uuid, "Level")

    companion object {
        val uuid: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    }
}
