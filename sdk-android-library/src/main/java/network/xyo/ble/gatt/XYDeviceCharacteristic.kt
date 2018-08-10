package network.xyo.ble.gatt

import java.util.UUID

/**
 * Created by arietrouw on 12/31/16.
 */

object XYDeviceCharacteristic {

    val ControlBuzzer = UUID.fromString("F014FFF1-0439-3000-E001-00001001FFFF")
    val ControlHandshake = UUID.fromString("F014FFF2-0439-3000-E001-00001001FFFF")
    val ControlVersion = UUID.fromString("F014FFF4-0439-3000-E001-00001001FFFF")
    val ControlBuzzerSelect = UUID.fromString("F014FFF6-0439-3000-E001-00001001FFFF")
    val ControlSurge = UUID.fromString("F014FFF7-0439-3000-E001-00001001FFFF")
    val ControlButton = UUID.fromString("F014FFF8-0439-3000-E001-00001001FFFF")
    val ControlDisconnect = UUID.fromString("F014FFF9-0439-3000-E001-00001001FFFF")

    val ExtendedConfigVirtualBeaconSettings = UUID.fromString("F014FF02-0439-3000-E001-00001001FFFF")
    val ExtendedConfigTone = UUID.fromString("F014FF03-0439-3000-E001-00001001FFFF")
    val ExtendedConfigRegistration = UUID.fromString("F014FF05-0439-3000-E001-00001001FFFF")
    val ExtendedConfigInactiveVirtualBeaconSettings = UUID.fromString("F014FF06-0439-3000-E001-00001001FFFF")
    val ExtendedConfigInactiveInterval = UUID.fromString("F014FF07-0439-3000-E001-00001001FFFF")
    val ExtendedConfigGPSInterval = UUID.fromString("2ABBAA00-0439-3000-E001-00001001FFFF")
    val ExtendedConfigGPSMode = UUID.fromString("2A99AA00-0439-3000-E001-00001001FFFF")
    val ExtendedConfigSIMId = UUID.fromString("2ACCAA00-0439-3000-E001-00001001FFFF")

    val BasicConfigLockStatus = UUID.fromString("F014EE01-0439-3000-E001-00001001FFFF")
    val BasicConfigLock = UUID.fromString("F014EE02-0439-3000-E001-00001001FFFF")
    val BasicConfigUnlock = UUID.fromString("F014EE03-0439-3000-E001-00001001FFFF")
    val BasicConfigUUID = UUID.fromString("F014EE04-0439-3000-E001-00001001FFFF")
    val BasicConfigMajor = UUID.fromString("F014EE05-0439-3000-E001-00001001FFFF")
    val BasicConfigMinor = UUID.fromString("F014EE06-0439-3000-E001-00001001FFFF")
    val BasicConfigInterval = UUID.fromString("F014EE07-0439-3000-E001-00001001FFFF")
    val BasicConfigOtaWrite = UUID.fromString("F014EE09-0439-3000-E001-00001001FFFF")
    val BasicConfigReboot = UUID.fromString("F014EE0A-0439-3000-E001-00001001FFFF")

    val ExtendedControlSIMStatus = UUID.fromString("2ADDAA00-0439-3000-E001-00001001FFFF")
    val ExtendedControlLED = UUID.fromString("2AAAAA00-0439-3000-E001-00001001FFFF")
    val ExtendedControlSelfTest = UUID.fromString("2a77AA00-0439-3000-E001-00001001FFFF")

    val BatteryLevel = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    val BatterySinceCharged = UUID.fromString("00002a20-0000-1000-8000-00805f9b34fb")

    val SensorRaw = UUID.fromString("F014DD01-0439-3000-E001-00001001FFFF")
    val SensorTimeout = UUID.fromString("F014DD02-0439-3000-E001-00001001FFFF")
    val SensorThreshold = UUID.fromString("F014DD03-0439-3000-E001-00001001FFFF")
    val SensorInactive = UUID.fromString("F014DD04-0439-3000-E001-00001001FFFF")
    val SensorMovementCount = UUID.fromString("F014DD05-0439-3000-E001-00001001FFFF")

    val GpsProfile = UUID.fromString("2abbAA00-0439-3000-E001-00001001FFFF")

    val XY4PrimaryStayAwake = UUID.fromString("a44eacf4-0104-0001-0001-5f784c9977b5")
    val XY4PrimaryUnlock = UUID.fromString("a44eacf4-0104-0001-0002-5f784c9977b5")
    val XY4PrimaryLock = UUID.fromString("a44eacf4-0104-0001-0003-5f784c9977b5")
    val XY4PrimaryMajor = UUID.fromString("a44eacf4-0104-0001-0004-5f784c9977b5")
    val XY4PrimaryMinor = UUID.fromString("a44eacf4-0104-0001-0005-5f784c9977b5")
    val XY4PrimaryUUID = UUID.fromString("a44eacf4-0104-0001-0006-5f784c9977b5")
    val XY4PrimaryButtonState = UUID.fromString("a44eacf4-0104-0001-0007-5f784c9977b5")
    val XY4PrimaryBuzzer = UUID.fromString("a44eacf4-0104-0001-0008-5f784c9977b5")
    val XY4PrimaryBuzzerConfig = UUID.fromString("a44eacf4-0104-0001-0009-5f784c9977b5")
    val XY4PrimaryAdConfig = UUID.fromString("a44eacf4-0104-0001-000a-5f784c9977b5")
    val XY4PrimaryButtonConfig = UUID.fromString("a44eacf4-0104-0001-000b-5f784c9977b5")
    val XY4PrimaryLastError = UUID.fromString("a44eacf4-0104-0001-000c-5f784c9977b5")
    val XY4PrimaryUptime = UUID.fromString("a44eacf4-0104-0001-000d-5f784c9977b5")
    val XY4PrimaryReset = UUID.fromString("a44eacf4-0104-0001-000e-5f784c9977b5")
    val XY4PrimarySelfTest = UUID.fromString("a44eacf4-0104-0001-000f-5f784c9977b5")
    val XY4PrimaryDebug = UUID.fromString("a44eacf4-0104-0001-0010-5f784c9977b5")
    val XY4PrimaryLeftBehind = UUID.fromString("a44eacf4-0104-0001-0011-5f784c9977b5")
    val XY4PrimaryEddystoneUID = UUID.fromString("a44eacf4-0104-0001-0012-5f784c9977b5")
    val XY4PrimaryEddystoneURL = UUID.fromString("a44eacf4-0104-0001-0013-5f784c9977b5")
    val XY4PrimaryEddystoneEID = UUID.fromString("a44eacf4-0104-0001-0014-5f784c9977b5")
    val XY4PrimaryColor = UUID.fromString("a44eacf4-0104-0001-0015-5f784c9977b5")
    val XY4PrimaryHardwareCreateDate = UUID.fromString("a44eacf4-0104-0001-0017-5f784c9977b5")

    // dialog ota characteristics start
    val SPOTA_MEM_DEV_UUID = UUID.fromString("8082caa8-41a6-4021-91c6-56f9b954cc34")
    val SPOTA_GPIO_MAP_UUID = UUID.fromString("724249f0-5eC3-4b5f-8804-42345af08651")
    val SPOTA_MEM_INFO_UUID = UUID.fromString("6c53db25-47a1-45fe-a022-7c92fb334fd4")
    val SPOTA_PATCH_LEN_UUID = UUID.fromString("9d84b9a3-000c-49d8-9183-855b673fda31")
    val SPOTA_PATCH_DATA_UUID = UUID.fromString("457871e8-d516-4ca1-9116-57d0b17b9cb2")
    val SPOTA_SERV_STATUS_UUID = UUID.fromString("5f78df94-798c-46f5-990a-b3eb6a065c88")
    val SPOTA_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    // dialog ota characteristics end

    val DeviceName = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    val Appearance = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    val PrivacyFlag = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb")
    val ConnParams = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb")

    val AttributeServiceChanged = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb")

    val DeviceManufacturer = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
    val DeviceModel = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")
    val DeviceSoftware = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")
    val DeviceHardware = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
    val DeviceFirmware = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
    val DeviceSystemID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb")

    val PowerLevel = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")

    val AlertLevel = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb")

    val TimeCurrent = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
    val TimeLocal = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")
}
