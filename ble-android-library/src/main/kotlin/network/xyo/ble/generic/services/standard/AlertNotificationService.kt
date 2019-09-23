package network.xyo.ble.generic.services

import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

class AlertNotificationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val controlPoint = IntegerCharacteristic(this, Characteristics.ControlPoint.uuid, "Control Point")
    val unreadAlertStatus = IntegerCharacteristic(this, Characteristics.UnreadAlertStatus.uuid, "Unread Alert Status")
    val newAlert = IntegerCharacteristic(this, Characteristics.NewAlert.uuid, "New Alert")
    val supportedNewAlertCategory = IntegerCharacteristic(this, Characteristics.SupportedNewAlertCategory.uuid, "Supported New Alert Category")
    val supportedUnreadAlertCategory = IntegerCharacteristic(this, Characteristics.SupportedUnreadAlertCategory.uuid, "Supported Unread Alert Category")

    companion object {
        val uuid: UUID = UUID.fromString("00001811-0000-1000-8000-00805F9B34FB")

        enum class Characteristics(val uuid: UUID) {
            ControlPoint(                   UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb")),
            UnreadAlertStatus(              UUID.fromString("00002a45-0000-1000-8000-00805f9b34fb")),
            NewAlert(                       UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb")),
            SupportedNewAlertCategory(      UUID.fromString("00002a47-0000-1000-8000-00805f9b34fb")),
            SupportedUnreadAlertCategory(   UUID.fromString("00002a48-0000-1000-8000-00805f9b34fb"))
        }
    }
}