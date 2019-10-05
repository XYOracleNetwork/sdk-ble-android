package network.xyo.ble.services.xy

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class PrimaryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid : UUID
        get() {
            return Companion.uuid
        }

    val stayAwake = ByteCharacteristic(this, Characteristics.StayAwake.uuid, "Stay Awake")
    val unlock = BytesCharacteristic(this, Characteristics.Unlock.uuid, "Unlock")
    val lock = BytesCharacteristic(this, Characteristics.Lock.uuid, "Lock")
    val major = ByteCharacteristic(this, Characteristics.Major.uuid, "Major")
    val minor = ByteCharacteristic(this, Characteristics.Minor.uuid, "Minor")
    val uuid = BytesCharacteristic(this, Characteristics.Uuid.uuid, "UUID")
    val buttonState = ByteCharacteristic(this, Characteristics.ButtonState.uuid, "Button State")
    val buzzer = ByteCharacteristic(this, Characteristics.Buzzer.uuid, "Buzzer")
    val buzzerConfig = BytesCharacteristic(this, Characteristics.BuzzerConfig.uuid, "Buzzer Config")
    val adConfig = BytesCharacteristic(this, Characteristics.AdConfig.uuid, "Ad Config")
    val buttonConfig = BytesCharacteristic(this, Characteristics.ButtonConfig.uuid, "Button Config")
    val lastError = ByteCharacteristic(this, Characteristics.LastError.uuid, "Last Error")
    val uptime = ByteCharacteristic(this, Characteristics.Uptime.uuid, "Uptime")
    val reset = ByteCharacteristic(this, Characteristics.Reset.uuid, "Reset")
    val selfTest = ByteCharacteristic(this, Characteristics.SelfTest.uuid, "Self Test")
    val debug = ByteCharacteristic(this, Characteristics.Debug.uuid, "Debug")
    val leftBehind = ByteCharacteristic(this, Characteristics.LeftBehind.uuid, "Left Behind")
    val color = BytesCharacteristic(this, Characteristics.Color.uuid, "Color")
    val hardwareCreateDate = BytesCharacteristic(this, Characteristics.HardwareCreateDate.uuid, "Hardware Create Date")

    companion object {

        val uuid: UUID = UUID.fromString("a44eacf4-0104-0001-0000-5f784c9977b5")

        enum class Characteristics(val uuid: UUID) {
            StayAwake(UUID.fromString(          "a44eacf4-0104-0001-0001-5f784c9977b5")),
            Unlock(UUID.fromString(             "a44eacf4-0104-0001-0002-5f784c9977b5")),
            Lock(UUID.fromString(               "a44eacf4-0104-0001-0003-5f784c9977b5")),
            Major(UUID.fromString(              "a44eacf4-0104-0001-0004-5f784c9977b5")),
            Minor(UUID.fromString(              "a44eacf4-0104-0001-0005-5f784c9977b5")),
            Uuid(UUID.fromString(               "a44eacf4-0104-0001-0006-5f784c9977b5")),
            ButtonState(UUID.fromString(        "a44eacf4-0104-0001-0007-5f784c9977b5")),
            Buzzer(UUID.fromString(             "a44eacf4-0104-0001-0008-5f784c9977b5")),
            BuzzerConfig(UUID.fromString(       "a44eacf4-0104-0001-0009-5f784c9977b5")),
            AdConfig(UUID.fromString(           "a44eacf4-0104-0001-000a-5f784c9977b5")),
            ButtonConfig(UUID.fromString(       "a44eacf4-0104-0001-000b-5f784c9977b5")),
            LastError(UUID.fromString(          "a44eacf4-0104-0001-000c-5f784c9977b5")),
            Uptime(UUID.fromString(             "a44eacf4-0104-0001-000d-5f784c9977b5")),
            Reset(UUID.fromString(              "a44eacf4-0104-0001-000e-5f784c9977b5")),
            SelfTest(UUID.fromString(           "a44eacf4-0104-0001-000f-5f784c9977b5")),
            Debug(UUID.fromString(              "a44eacf4-0104-0001-0010-5f784c9977b5")),
            LeftBehind(UUID.fromString(         "a44eacf4-0104-0001-0011-5f784c9977b5")),
            Color(UUID.fromString(              "a44eacf4-0104-0001-0015-5f784c9977b5")),
            HardwareCreateDate(UUID.fromString( "a44eacf4-0104-0001-0017-5f784c9977b5"))
        }
    }
}