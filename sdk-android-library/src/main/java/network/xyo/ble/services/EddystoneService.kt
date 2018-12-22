package network.xyo.ble.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class EddystoneService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid: UUID = UUID.fromString("0000feaa-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID)
    }
}