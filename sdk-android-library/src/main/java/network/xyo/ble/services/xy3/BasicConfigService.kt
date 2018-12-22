package network.xyo.ble.services.xy3

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

@Suppress("unused")
class BasicConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return Companion.uuid
        }

    val lockStatus = IntegerCharacteristic(this, Characteristics.LockStatus.uuid)
    val lock = BytesCharacteristic(this, Characteristics.Lock.uuid)
    val unlock = BytesCharacteristic(this, Characteristics.Unlock.uuid)
    val uuid = BytesCharacteristic(this, Characteristics.Uuid.uuid)
    val major = IntegerCharacteristic(this, Characteristics.Major.uuid)
    val minor = IntegerCharacteristic(this, Characteristics.Minor.uuid)
    val interval = BytesCharacteristic(this, Characteristics.Interval.uuid)
    val otaWrite = BytesCharacteristic(this, Characteristics.OtaWrite.uuid)
    val reboot = IntegerCharacteristic(this, Characteristics.Reboot.uuid)

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