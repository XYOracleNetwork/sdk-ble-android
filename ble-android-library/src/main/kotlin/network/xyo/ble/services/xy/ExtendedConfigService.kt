package network.xyo.ble.services.xy

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class ExtendedConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val virtualBeaconSettings = ByteCharacteristic(this, Characteristics.VirtualBeaconSettings.uuid, "Virtual Beacon Settings")
    val tone = ByteCharacteristic(this, Characteristics.Tone.uuid, "Tone")
    val stayAwake = ByteCharacteristic(this, Characteristics.StayAwake.uuid, "Stay Awake")
    val inactiveVirtualBeaconSettings = ByteCharacteristic(this, Characteristics.InactiveVirtualBeaconSettings.uuid, "Inactive Virtual Beacon Settings")
    val inactiveInterval = ByteCharacteristic(this, Characteristics.InactiveInterval.uuid, "Inactive Interval")
    val gpsInterval = ByteCharacteristic(this, Characteristics.GpsInterval.uuid, "GPS Interval")
    val gpsMode = ByteCharacteristic(this, Characteristics.GpsMode.uuid, "GPS Mode")
    val simId = ByteCharacteristic(this, Characteristics.SimId.uuid, "SIM Id")

    companion object {
        val uuid: UUID = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF")

        enum class Characteristics(val uuid: UUID) {
            VirtualBeaconSettings(UUID.fromString("F014FF02-0439-3000-E001-00001001FFFF")),
            Tone(UUID.fromString("F014FF03-0439-3000-E001-00001001FFFF")),
            StayAwake(UUID.fromString("F014FF05-0439-3000-E001-00001001FFFF")),
            InactiveVirtualBeaconSettings(UUID.fromString("F014FF06-0439-3000-E001-00001001FFFF")),
            InactiveInterval(UUID.fromString("F014FF07-0439-3000-E001-00001001FFFF")),
            GpsInterval(UUID.fromString("2ABBAA00-0439-3000-E001-00001001FFFF")),
            GpsMode(UUID.fromString("2A99AA00-0439-3000-E001-00001001FFFF")),
            SimId(UUID.fromString("2ACCAA00-0439-3000-E001-00001001FFFF"))
        }
    }
}