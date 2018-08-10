package network.xyo.ble.services.xy3

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class ExtendedConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val virtualBeaconSettings = IntegerCharacteristic(this, characteristics.VirtualBeaconSettings.uuid)
    val tone = IntegerCharacteristic(this, characteristics.Tone.uuid)
    val registration = IntegerCharacteristic(this, characteristics.Registration.uuid)
    val inactiveVirtualBeaconSettings = IntegerCharacteristic(this, characteristics.InactiveVirtualBeaconSettings.uuid)
    val inactiveInterval = IntegerCharacteristic(this, characteristics.InactiveInterval.uuid)
    val gpsInterval = IntegerCharacteristic(this, characteristics.GpsInterval.uuid)
    val gpsMode = IntegerCharacteristic(this, characteristics.GpsMode.uuid)
    val simId = IntegerCharacteristic(this, characteristics.SimId.uuid)

    companion object {
        val uuid = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF")

        enum class characteristics(val uuid: UUID) {
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