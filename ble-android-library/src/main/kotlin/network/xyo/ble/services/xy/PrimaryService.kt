package network.xyo.ble.services.xy

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class PrimaryServiceCharacteristics(val uuid: UUID) {
    StayAwake(UUID.fromString("a44eacf4-0104-0001-0001-5f784c9977b5")),
    Unlock(UUID.fromString("a44eacf4-0104-0001-0002-5f784c9977b5")),
    Lock(UUID.fromString("a44eacf4-0104-0001-0003-5f784c9977b5")),
    Major(UUID.fromString("a44eacf4-0104-0001-0004-5f784c9977b5")),
    Minor(UUID.fromString("a44eacf4-0104-0001-0005-5f784c9977b5")),
    Uuid(UUID.fromString("a44eacf4-0104-0001-0006-5f784c9977b5")),
    ButtonState(UUID.fromString("a44eacf4-0104-0001-0007-5f784c9977b5")),
    Buzzer(UUID.fromString("a44eacf4-0104-0001-0008-5f784c9977b5")),
    BuzzerConfig(UUID.fromString("a44eacf4-0104-0001-0009-5f784c9977b5")),
    AdConfig(UUID.fromString("a44eacf4-0104-0001-000a-5f784c9977b5")),
    ButtonConfig(UUID.fromString("a44eacf4-0104-0001-000b-5f784c9977b5")),
    LastError(UUID.fromString("a44eacf4-0104-0001-000c-5f784c9977b5")),
    Uptime(UUID.fromString("a44eacf4-0104-0001-000d-5f784c9977b5")),
    Reset(UUID.fromString("a44eacf4-0104-0001-000e-5f784c9977b5")),
    SelfTest(UUID.fromString("a44eacf4-0104-0001-000f-5f784c9977b5")),
    Debug(UUID.fromString("a44eacf4-0104-0001-0010-5f784c9977b5")),
    LeftBehind(UUID.fromString("a44eacf4-0104-0001-0011-5f784c9977b5")),
    Color(UUID.fromString("a44eacf4-0104-0001-0015-5f784c9977b5")),
    HardwareCreateDate(UUID.fromString("a44eacf4-0104-0001-0017-5f784c9977b5"))
}

class PrimaryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return Companion.uuid
        }

    val stayAwake = ByteCharacteristic(this, PrimaryServiceCharacteristics.StayAwake.uuid, "Stay Awake")
    val unlock = BytesCharacteristic(this, PrimaryServiceCharacteristics.Unlock.uuid, "Unlock")
    val lock = BytesCharacteristic(this, PrimaryServiceCharacteristics.Lock.uuid, "Lock")
    val major = ByteCharacteristic(this, PrimaryServiceCharacteristics.Major.uuid, "Major")
    val minor = ByteCharacteristic(this, PrimaryServiceCharacteristics.Minor.uuid, "Minor")
    val uuid = BytesCharacteristic(this, PrimaryServiceCharacteristics.Uuid.uuid, "UUID")
    val buttonState = ByteCharacteristic(this, PrimaryServiceCharacteristics.ButtonState.uuid, "Button State")
    val buzzer = ByteCharacteristic(this, PrimaryServiceCharacteristics.Buzzer.uuid, "Buzzer")
    val buzzerConfig = BytesCharacteristic(this, PrimaryServiceCharacteristics.BuzzerConfig.uuid, "Buzzer Config")
    val adConfig = BytesCharacteristic(this, PrimaryServiceCharacteristics.AdConfig.uuid, "Ad Config")
    val buttonConfig = BytesCharacteristic(this, PrimaryServiceCharacteristics.ButtonConfig.uuid, "Button Config")
    val lastError = ByteCharacteristic(this, PrimaryServiceCharacteristics.LastError.uuid, "Last Error")
    val uptime = ByteCharacteristic(this, PrimaryServiceCharacteristics.Uptime.uuid, "Uptime")
    val reset = ByteCharacteristic(this, PrimaryServiceCharacteristics.Reset.uuid, "Reset")
    val selfTest = ByteCharacteristic(this, PrimaryServiceCharacteristics.SelfTest.uuid, "Self Test")
    val debug = ByteCharacteristic(this, PrimaryServiceCharacteristics.Debug.uuid, "Debug")
    val leftBehind = ByteCharacteristic(this, PrimaryServiceCharacteristics.LeftBehind.uuid, "Left Behind")
    val color = BytesCharacteristic(this, PrimaryServiceCharacteristics.Color.uuid, "Color")
    val hardwareCreateDate = BytesCharacteristic(this, PrimaryServiceCharacteristics.HardwareCreateDate.uuid, "Hardware Create Date")

    companion object {

        val uuid: UUID = UUID.fromString("a44eacf4-0104-0001-0000-5f784c9977b5")
    }
}
