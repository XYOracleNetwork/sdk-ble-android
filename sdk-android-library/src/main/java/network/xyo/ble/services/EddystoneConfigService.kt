package network.xyo.ble.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class EddystoneConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid: UUID = UUID.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8")

        enum class Characteristics(val uuid: UUID) {
        }
    }
}