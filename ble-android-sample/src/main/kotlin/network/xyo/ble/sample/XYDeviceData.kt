package network.xyo.ble.sample

data class XYDeviceData(
    var controlPoint: String = "",
    var unreadAlertStatus: String = "",
    var newAlert: String = "",
    var supportedNewAlertCategory: String = "",
    var supportedUnreadAlertCategory: String = "",

    var level: String = "",

    var currentTime: String = "",
    var localTimeInformation: String = "",
    var referenceTimeInformation: String = "",

    var systemId: String = "",
    var modelNumberString: String = "",
    var serialNumberString: String = "",
    var firmwareRevisionString: String = "",
    var hardwareRevisionString: String = "",
    var softwareRevisionString: String = "",
    var manufacturerNameString: String = "",
    var ieeeRegulatoryCertificationDataList: String = "",
    var pnpId: String = "",

    var deviceName: String = "",
    var appearance: String = "",
    var privacyFlag: String = "",
    var reconnectionAddress: String = "",
    var peripheralPreferredConnectionParameters: String = "",

    var serviceChanged: String = "",

    var alertLevel: String = "",

    var txPowerLevel: String = "",

    var EddystoneService: String = "",
    var EddystoneConfigService: String = ""
)