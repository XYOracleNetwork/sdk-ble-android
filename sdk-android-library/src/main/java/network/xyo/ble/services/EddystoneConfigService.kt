package network.xyo.ble.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class EddystoneConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    companion object {
        val uuid = UUID.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8")

        enum class characteristics(val uuid: UUID) {
        }
    }
}