@file:Suppress("SpellCheckingInspection")

package network.xyo.ble.services.standard

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class DeviceInformationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val systemId = IntegerCharacteristic(this, Characteristics.SystemId.uuid)
    val modelNumberString = StringCharacteristic(this, Characteristics.ModelNumberString.uuid)
    val serialNumberString = StringCharacteristic(this, Characteristics.SerialNumberString.uuid)
    val firmwareRevisionString = StringCharacteristic(this, Characteristics.FirmwareRevisionString.uuid)
    val hardwareRevisionString = StringCharacteristic(this, Characteristics.HardwareRevisionString.uuid)
    val softwareRevisionString = StringCharacteristic(this, Characteristics.SoftwareRevisionString.uuid)
    val manufacturerNameString = StringCharacteristic(this, Characteristics.ManufacturerNameString.uuid)
    val ieeeRegulatoryCertificationDataList = IntegerCharacteristic(this, Characteristics.Ieee11073x20601RegulatoryCertificationDataList.uuid)
    val pnpId = IntegerCharacteristic(this, Characteristics.PnPId.uuid)

    companion object {
        val uuid: UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            SystemId(                                       UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb")),
            ModelNumberString(                              UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")),
            SerialNumberString(                             UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")),
            FirmwareRevisionString(                         UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")),
            HardwareRevisionString(                         UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")),
            SoftwareRevisionString(                         UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")),
            ManufacturerNameString(                         UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")),
            Ieee11073x20601RegulatoryCertificationDataList( UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb")),
            PnPId(                                          UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"))

        }
    }
}