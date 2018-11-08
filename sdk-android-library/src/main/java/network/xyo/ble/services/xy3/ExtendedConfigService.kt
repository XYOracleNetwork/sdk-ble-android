package network.xyo.ble.services.xy3

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

@Suppress("unused")
class ExtendedConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val virtualBeaconSettings = IntegerCharacteristic(this, Characteristics.VirtualBeaconSettings.uuid)
    val tone = IntegerCharacteristic(this, Characteristics.Tone.uuid)
    val registration = IntegerCharacteristic(this, Characteristics.Registration.uuid)
    val inactiveVirtualBeaconSettings = IntegerCharacteristic(this, Characteristics.InactiveVirtualBeaconSettings.uuid)
    val inactiveInterval = IntegerCharacteristic(this, Characteristics.InactiveInterval.uuid)
    val gpsInterval = IntegerCharacteristic(this, Characteristics.GpsInterval.uuid)
    val gpsMode = IntegerCharacteristic(this, Characteristics.GpsMode.uuid)
    val simId = IntegerCharacteristic(this, Characteristics.SimId.uuid)

    companion object {
        val uuid: UUID = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF")

        enum class Characteristics(val uuid: UUID) {
            VirtualBeaconSettings(UUID.fromString("F014FF02-0439-3000-E001-00001001FFFF")),
            Tone(UUID.fromString("F014FF03-0439-3000-E001-00001001FFFF")),
            Registration(UUID.fromString("F014FF05-0439-3000-E001-00001001FFFF")),
            InactiveVirtualBeaconSettings(UUID.fromString("F014FF06-0439-3000-E001-00001001FFFF")),
            InactiveInterval(UUID.fromString("F014FF07-0439-3000-E001-00001001FFFF")),
            GpsInterval(UUID.fromString("2ABBAA00-0439-3000-E001-00001001FFFF")),
            GpsMode(UUID.fromString("2A99AA00-0439-3000-E001-00001001FFFF")),
            SimId(UUID.fromString("2ACCAA00-0439-3000-E001-00001001FFFF"))
        }
    }
}