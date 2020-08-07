package network.xyo.ble.services.xy

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class CsrOtaServiceCharacteristics(val uuid: UUID)

class CsrOtaService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid: UUID = UUID.fromString("00001016-D102-11E1-9B23-00025B00A5A5")
    }
}
