package network.xyo.ble.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class EddystoneService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid = UUID.fromString("0000feaa-0000-1000-8000-00805F9B34FB")

        enum class characteristics(val uuid: UUID) {
        }
    }
}