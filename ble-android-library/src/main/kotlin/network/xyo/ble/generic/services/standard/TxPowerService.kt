package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class TxPowerServiceCharacteristics(val uuid: UUID) {
    TxPowerLevel(UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb"))
}

class TxPowerService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return GenericAccessService.uuid
        }

    val txPowerLevel = ByteCharacteristic(this, TxPowerServiceCharacteristics.TxPowerLevel.uuid, "Tx Power Level")

    companion object {
        val uuid: UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB")
    }
}
