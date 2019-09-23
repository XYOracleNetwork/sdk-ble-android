package network.xyo.ble.generic.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class CurrentTimeService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return LinkLossService.uuid
        }

    val currentTime = IntegerCharacteristic(this, Characteristics.CurrentTime.uuid, "Current Time")
    val localTimeInformation = IntegerCharacteristic(this, Characteristics.LocalTimeInformation.uuid, "Local Time Information")
    val referenceTimeInformation = IntegerCharacteristic(this, Characteristics.ReferenceTimeInformation.uuid, "Reference Time Information")

    companion object {
        val uuid: UUID = UUID.fromString("00001805-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            CurrentTime(UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")),
            LocalTimeInformation(UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")),
            ReferenceTimeInformation(UUID.fromString("00002a14-0000-1000-8000-00805f9b34fb")),
        }
    }
}