package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class CurrentTimeService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return LinkLossService.uuid
        }

    val currentTime = IntegerCharacteristic(this, characteristics.CurrentTime.uuid)
    val localTimeInformation = IntegerCharacteristic(this, characteristics.LocalTimeInformation.uuid)
    val referenceTimeInformation = IntegerCharacteristic(this, characteristics.ReferenceTimeInformation.uuid)

    companion object {
        val uuid = UUID.fromString("00001805-0000-1000-8000-00805F9B34FB")

        enum class characteristics(val uuid: UUID) {
            CurrentTime(UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")),
            LocalTimeInformation(UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")),
            ReferenceTimeInformation(UUID.fromString("00002a14-0000-1000-8000-00805f9b34fb")),
        }
    }
}