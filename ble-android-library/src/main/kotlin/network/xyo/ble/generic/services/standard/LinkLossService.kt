package network.xyo.ble.generic.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class LinkLossService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val alertLevel = IntegerCharacteristic(this, Characteristics.AlertLevel.uuid, "Alert Level")

    companion object {
        val uuid: UUID = UUID.fromString("00001803-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            AlertLevel(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"))
        }
    }
}