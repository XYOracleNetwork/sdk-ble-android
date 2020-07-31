package network.xyo.ble.services.xy

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.ByteCharacteristic
import network.xyo.ble.generic.services.BytesCharacteristic
import network.xyo.ble.generic.services.Service

enum class BasicConfigServiceCharacteristics(val uuid: UUID) {
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

class BasicConfigService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return Companion.uuid
        }

    val lockStatus = ByteCharacteristic(this, BasicConfigServiceCharacteristics.LockStatus.uuid, "Lock Status")
    val lock = BytesCharacteristic(this, BasicConfigServiceCharacteristics.Lock.uuid, "Lock")
    val unlock = BytesCharacteristic(this, BasicConfigServiceCharacteristics.Unlock.uuid, "Unlock")
    val uuid = BytesCharacteristic(this, BasicConfigServiceCharacteristics.Uuid.uuid, "UUID")
    val major = ByteCharacteristic(this, BasicConfigServiceCharacteristics.Major.uuid, "Major")
    val minor = ByteCharacteristic(this, BasicConfigServiceCharacteristics.Minor.uuid, "Minor")
    val interval = BytesCharacteristic(this, BasicConfigServiceCharacteristics.Interval.uuid, "Interval")
    val otaWrite = BytesCharacteristic(this, BasicConfigServiceCharacteristics.OtaWrite.uuid, "OTA Write")
    val reboot = ByteCharacteristic(this, BasicConfigServiceCharacteristics.Reboot.uuid, "Reboot")

    companion object {
        val uuid: UUID = UUID.fromString("F014EE00-0439-3000-E001-00001001FFFF")
    }
}
