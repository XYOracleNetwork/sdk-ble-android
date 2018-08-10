package network.xyo.ble.services.xy3

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class CsrOtaService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid = UUID.fromString("00001016-D102-11E1-9B23-00025B00A5A5")

        enum class characteristics(val uuid: UUID) {
        }
    }
}