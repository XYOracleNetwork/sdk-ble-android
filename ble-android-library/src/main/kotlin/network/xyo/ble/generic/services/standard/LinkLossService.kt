package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class LinkLossServiceCharacteristics(val uuid: UUID) {
    AlertLevel(UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb"))
}

class LinkLossService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val alertLevel = ByteCharacteristic(this, LinkLossServiceCharacteristics.AlertLevel.uuid, "Alert Level")

    companion object {
        val uuid: UUID = UUID.fromString("00001803-0000-1000-8000-00805F9B34FB")
    }
}
