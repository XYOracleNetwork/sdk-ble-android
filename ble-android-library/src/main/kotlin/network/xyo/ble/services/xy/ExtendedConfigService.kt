package network.xyo.ble.services.xy

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.ByteCharacteristic
import network.xyo.ble.generic.services.Service

enum class ExtendedConfigServiceCharacteristics(val uuid: UUID) {
    VirtualBeaconSettings(UUID.fromString("F014FF02-0439-3000-E001-00001001FFFF")),
    Tone(UUID.fromString("F014FF03-0439-3000-E001-00001001FFFF")),
    StayAwake(UUID.fromString("F014FF05-0439-3000-E001-00001001FFFF")),
    InactiveVirtualBeaconSettings(UUID.fromString("F014FF06-0439-3000-E001-00001001FFFF")),
    InactiveInterval(UUID.fromString("F014FF07-0439-3000-E001-00001001FFFF")),
    GpsInterval(UUID.fromString("2ABBAA00-0439-3000-E001-00001001FFFF")),
    GpsMode(UUID.fromString("2A99AA00-0439-3000-E001-00001001FFFF")),
    SimId(UUID.fromString("2ACCAA00-0439-3000-E001-00001001FFFF"))
}

class ExtendedConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val virtualBeaconSettings = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.VirtualBeaconSettings.uuid, "Virtual Beacon Settings")
    val tone = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.Tone.uuid, "Tone")
    val stayAwake = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.StayAwake.uuid, "Stay Awake")
    val inactiveVirtualBeaconSettings = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.InactiveVirtualBeaconSettings.uuid, "Inactive Virtual Beacon Settings")
    val inactiveInterval = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.InactiveInterval.uuid, "Inactive Interval")
    val gpsInterval = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.GpsInterval.uuid, "GPS Interval")
    val gpsMode = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.GpsMode.uuid, "GPS Mode")
    val simId = ByteCharacteristic(this, ExtendedConfigServiceCharacteristics.SimId.uuid, "SIM Id")

    companion object {
        val uuid: UUID = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF")
    }
}
