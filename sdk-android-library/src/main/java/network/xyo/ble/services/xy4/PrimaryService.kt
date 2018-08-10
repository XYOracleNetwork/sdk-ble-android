package network.xyo.ble.services.xy4

import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class PrimaryService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid : UUID
        get() {
            return Companion.uuid
        }

    val stayAwake = IntegerCharacteristic(this, characteristics.StayAwake.uuid)
    val unlock = BytesCharacteristic(this, characteristics.Unlock.uuid)
    val lock = BytesCharacteristic(this, characteristics.Lock.uuid)
    val major = IntegerCharacteristic(this, characteristics.Major.uuid)
    val minor = IntegerCharacteristic(this, characteristics.Minor.uuid)
    val uuid = BytesCharacteristic(this, characteristics.Uuid.uuid)
    val buttonState = IntegerCharacteristic(this, characteristics.ButtonState.uuid)
    val buzzer = IntegerCharacteristic(this, characteristics.Buzzer.uuid)
    val buzzerConfig = BytesCharacteristic(this, characteristics.BuzzerConfig.uuid)
    val adConfig = BytesCharacteristic(this, characteristics.AdConfig.uuid)
    val buttonConfig = BytesCharacteristic(this, characteristics.ButtonConfig.uuid)
    val lastError = IntegerCharacteristic(this, characteristics.LastError.uuid)
    val uptime = IntegerCharacteristic(this, characteristics.Uptime.uuid)
    val reset = IntegerCharacteristic(this, characteristics.Reset.uuid)
    val selfTest = IntegerCharacteristic(this, characteristics.SelfTest.uuid)
    val debug = IntegerCharacteristic(this, characteristics.Debug.uuid)
    val leftBehind = IntegerCharacteristic(this, characteristics.LeftBehind.uuid)
    val eddyStoneUid = BytesCharacteristic(this, characteristics.EddystoneUID.uuid)
    val eddyStoneUrl = BytesCharacteristic(this, characteristics.EddystoneURL.uuid)
    val eddyStoneEid = BytesCharacteristic(this, characteristics.EddystoneEID.uuid)
    val color = BytesCharacteristic(this, characteristics.Color.uuid)
    val hardwareCreateDate = BytesCharacteristic(this, characteristics.HardwareCreateDate.uuid)

    companion object {

        val uuid = UUID.fromString("a44eacf4-0104-0001-0000-5f784c9977b5")

        enum class characteristics(val uuid: UUID) {
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