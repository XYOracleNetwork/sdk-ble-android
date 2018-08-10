package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class DeviceInformationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val systemId = IntegerCharacteristic(this, characteristics.SystemId.uuid)
    val modelNumberString = StringCharacteristic(this, characteristics.ModelNumberString.uuid)
    val serialNumberString = StringCharacteristic(this, characteristics.SerialNumberString.uuid)
    val firmwareRevisionString = StringCharacteristic(this, characteristics.FirmwareRevisionString.uuid)
    val hardwareRevisionString = StringCharacteristic(this, characteristics.HardwareRevisionString.uuid)
    val softwareRevisionString = StringCharacteristic(this, characteristics.SoftwareRevisionString.uuid)
    val manufacturerNameString = StringCharacteristic(this, characteristics.ManufacturerNameString.uuid)
    val ieeeRegulatoryCertificationDataList = IntegerCharacteristic(this, characteristics.IEEE11073_20601RegulatoryCertificationDataList.uuid)
    val pnpId = IntegerCharacteristic(this, characteristics.PnPId.uuid)

    companion object {
        val uuid = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")

        enum class characteristics(val uuid: UUID) {
            SystemId(                                       UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb")),
            ModelNumberString(                              UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")),
            SerialNumberString(                             UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")),
            FirmwareRevisionString(                         UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")),
            HardwareRevisionString(                         UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")),
            SoftwareRevisionString(                         UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")),
            ManufacturerNameString(                         UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")),
            IEEE11073_20601RegulatoryCertificationDataList( UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb")),
            PnPId(                                          UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"))

        }
    }
}