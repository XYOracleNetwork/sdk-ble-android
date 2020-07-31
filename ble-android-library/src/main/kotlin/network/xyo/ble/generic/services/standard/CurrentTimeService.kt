package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.ByteCharacteristic
import network.xyo.ble.generic.services.Service

enum class CurrentTimeServiceCharacteristics(val uuid: UUID) {
    CurrentTime(UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")),
    LocalTimeInformation(UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")),
    ReferenceTimeInformation(UUID.fromString("00002a14-0000-1000-8000-00805f9b34fb")),
}

class CurrentTimeService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return LinkLossService.uuid
        }

    val currentTime = ByteCharacteristic(this, CurrentTimeServiceCharacteristics.CurrentTime.uuid, "Current Time")
    val localTimeInformation = ByteCharacteristic(this, CurrentTimeServiceCharacteristics.LocalTimeInformation.uuid, "Local Time Information")
    val referenceTimeInformation = ByteCharacteristic(this, CurrentTimeServiceCharacteristics.ReferenceTimeInformation.uuid, "Reference Time Information")

    companion object {
        val uuid: UUID = UUID.fromString("00001805-0000-1000-8000-00805F9B34FB")
    }
}
