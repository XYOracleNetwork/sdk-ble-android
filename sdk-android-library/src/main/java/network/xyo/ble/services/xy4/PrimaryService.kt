package network.xyo.ble.services.xy4

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

@Suppress("unused")
class PrimaryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid : UUID
        get() {
            return Companion.uuid
        }

    val stayAwake = IntegerCharacteristic(this, Characteristics.StayAwake.uuid)
    val unlock = BytesCharacteristic(this, Characteristics.Unlock.uuid)
    val lock = BytesCharacteristic(this, Characteristics.Lock.uuid)
    val major = IntegerCharacteristic(this, Characteristics.Major.uuid)
    val minor = IntegerCharacteristic(this, Characteristics.Minor.uuid)
    val uuid = BytesCharacteristic(this, Characteristics.Uuid.uuid)
    val buttonState = IntegerCharacteristic(this, Characteristics.ButtonState.uuid)
    val buzzer = IntegerCharacteristic(this, Characteristics.Buzzer.uuid)
    val buzzerConfig = BytesCharacteristic(this, Characteristics.BuzzerConfig.uuid)
    val adConfig = BytesCharacteristic(this, Characteristics.AdConfig.uuid)
    val buttonConfig = BytesCharacteristic(this, Characteristics.ButtonConfig.uuid)
    val lastError = IntegerCharacteristic(this, Characteristics.LastError.uuid)
    val uptime = IntegerCharacteristic(this, Characteristics.Uptime.uuid)
    val reset = IntegerCharacteristic(this, Characteristics.Reset.uuid)
    val selfTest = IntegerCharacteristic(this, Characteristics.SelfTest.uuid)
    val debug = IntegerCharacteristic(this, Characteristics.Debug.uuid)
    val leftBehind = IntegerCharacteristic(this, Characteristics.LeftBehind.uuid)
    val eddyStoneUid = BytesCharacteristic(this, Characteristics.EddystoneUID.uuid)
    val eddyStoneUrl = BytesCharacteristic(this, Characteristics.EddystoneURL.uuid)
    val eddyStoneEid = BytesCharacteristic(this, Characteristics.EddystoneEID.uuid)
    val color = BytesCharacteristic(this, Characteristics.Color.uuid)
    val hardwareCreateDate = BytesCharacteristic(this, Characteristics.HardwareCreateDate.uuid)

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
            EddystoneUID(UUID.fromString(       "a44eacf4-0104-0001-0012-5f784c9977b5")),
            EddystoneURL(UUID.fromString(       "a44eacf4-0104-0001-0013-5f784c9977b5")),
            EddystoneEID(UUID.fromString(       "a44eacf4-0104-0001-0014-5f784c9977b5")),
            Color(UUID.fromString(              "a44eacf4-0104-0001-0015-5f784c9977b5")),
            HardwareCreateDate(UUID.fromString( "a44eacf4-0104-0001-0017-5f784c9977b5"))
        }
    }
}