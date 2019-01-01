package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class TxPowerService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return GenericAccessService.uuid
        }

    val txPowerLevel = IntegerCharacteristic(this, Characteristics.TxPowerLevel.uuid)

    companion object {
        val uuid: UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            TxPowerLevel(UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb"))
        }
    }
}