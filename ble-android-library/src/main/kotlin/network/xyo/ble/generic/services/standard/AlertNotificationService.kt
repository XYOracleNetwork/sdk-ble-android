package network.xyo.ble.generic.services.standard

import java.util.UUID
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service

enum class AlertNotificationServiceCharacteristics(val uuid: UUID) {
    ControlPoint(UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb")),
    UnreadAlertStatus(UUID.fromString("00002a45-0000-1000-8000-00805f9b34fb")),
    NewAlert(UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")),
    SupportedNewAlertCategory(UUID.fromString("00002a47-0000-1000-8000-00805f9b34fb")),
    SupportedUnreadAlertCategory(UUID.fromString("00002a48-0000-1000-8000-00805f9b34fb"))
}

class AlertNotificationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val controlPoint = ByteCharacteristic(this, AlertNotificationServiceCharacteristics.ControlPoint.uuid, "Control Point")
    val unreadAlertStatus = ByteCharacteristic(this, AlertNotificationServiceCharacteristics.UnreadAlertStatus.uuid, "Unread Alert Status")
    val newAlert = ByteCharacteristic(this, AlertNotificationServiceCharacteristics.NewAlert.uuid, "New Alert")
    val supportedNewAlertCategory = ByteCharacteristic(this, AlertNotificationServiceCharacteristics.SupportedNewAlertCategory.uuid, "Supported New Alert Category")
    val supportedUnreadAlertCategory = ByteCharacteristic(this, AlertNotificationServiceCharacteristics.SupportedUnreadAlertCategory.uuid, "Supported Unread Alert Category")

    companion object {
        val uuid: UUID = UUID.fromString("00001811-0000-1000-8000-00805F9B34FB")
    }
}
