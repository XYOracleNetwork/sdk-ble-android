package network.xyo.ble.services.xy

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class BasicConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return Companion.uuid
        }

    val lockStatus = ByteCharacteristic(this, Characteristics.LockStatus.uuid, "Lock Status")
    val lock = BytesCharacteristic(this, Characteristics.Lock.uuid, "Lock")
    val unlock = BytesCharacteristic(this, Characteristics.Unlock.uuid, "Unlock")
    val uuid = BytesCharacteristic(this, Characteristics.Uuid.uuid, "UUID")
    val major = ByteCharacteristic(this, Characteristics.Major.uuid, "Major")
    val minor = ByteCharacteristic(this, Characteristics.Minor.uuid, "Minor")
    val interval = BytesCharacteristic(this, Characteristics.Interval.uuid, "Interval")
    val otaWrite = BytesCharacteristic(this, Characteristics.OtaWrite.uuid, "OTA Write")
    val reboot = ByteCharacteristic(this, Characteristics.Reboot.uuid, "Reboot")

    companion object {
        val uuid: UUID = UUID.fromString("F014EE00-0439-3000-E001-00001001FFFF")

        enum class Characteristics(val uuid: UUID) {
            LockStatus(UUID.fromString("F014EE01-0439-3000-E001-00001001FFFF")),
            Lock(UUID.fromString("F014EE02-0439-3000-E001-00001001FFFF")),
            Unlock(UUID.fromString("F014EE03-0439-3000-E001-00001001FFFF")),
            Uuid(UUID.fromString("F014EE04-0439-3000-E001-00001001FFFF")),
            Major(UUID.fromString("F014EE05-0439-3000-E001-00001001FFFF")),
            Minor(UUID.fromString("F014EE06-0439-3000-E001-00001001FFFF")),
            Interval(UUID.fromString("F014EE07-0439-3000-E001-00001001FFFF")),
            OtaWrite(UUID.fromString("F014EE09-0439-3000-E001-00001001FFFF")),
            Reboot(UUID.fromString("F014EE0A-0439-3000-E001-00001001FFFF"))
        }
    }
}