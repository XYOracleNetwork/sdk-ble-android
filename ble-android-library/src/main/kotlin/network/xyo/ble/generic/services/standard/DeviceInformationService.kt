@file:Suppress("SpellCheckingInspection")

package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class DeviceInformationServiceCharacteristics(val uuid: UUID) {
    SystemId(UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb")),
    ModelNumberString(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")),
    SerialNumberString(UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")),
    FirmwareRevisionString(UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")),
    HardwareRevisionString(UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")),
    SoftwareRevisionString(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")),
    ManufacturerNameString(UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")),
    Ieee11073x20601RegulatoryCertificationDataList(UUID.fromString("00002a2a-0000-1000-8000-00805f9b34fb")),
    PnPId(UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"))
}

class DeviceInformationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val systemId = ByteCharacteristic(this, DeviceInformationServiceCharacteristics.SystemId.uuid, "System Id")
    val modelNumberString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.ModelNumberString.uuid, "Model Number")
    val serialNumberString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.SerialNumberString.uuid, "Serial Number")
    val firmwareRevisionString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.FirmwareRevisionString.uuid, "Firmware Revision")
    val hardwareRevisionString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.HardwareRevisionString.uuid, "Hardware Revision")
    val softwareRevisionString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.SoftwareRevisionString.uuid, "Software Revision")
    val manufacturerNameString = StringCharacteristic(this, DeviceInformationServiceCharacteristics.ManufacturerNameString.uuid, "Manufacturer Name")
    val ieeeRegulatoryCertificationDataList = ByteCharacteristic(this, DeviceInformationServiceCharacteristics.Ieee11073x20601RegulatoryCertificationDataList.uuid, "IEEE Regulatory Certification")
    val pnpId = ByteCharacteristic(this, DeviceInformationServiceCharacteristics.PnPId.uuid, "Plug-n-Play ID")

    companion object {
        val uuid: UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")
    }
}
