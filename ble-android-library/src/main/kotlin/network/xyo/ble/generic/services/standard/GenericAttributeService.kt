package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

class GenericAttributeService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val serviceChanged = ByteCharacteristic(this, Characteristics.ServiceChanged.uuid, "Service Changed")

    companion object {
        val uuid: UUID = UUID.fromString("00001801-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            ServiceChanged(UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb"))
        }
    }
}
