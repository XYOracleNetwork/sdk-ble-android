package network.xyo.ble.generic.services.standard

import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class AlertNotificationService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val controlPoint = ByteCharacteristic(this, Characteristics.ControlPoint.uuid, "Control Point")
    val unreadAlertStatus = ByteCharacteristic(this, Characteristics.UnreadAlertStatus.uuid, "Unread Alert Status")
    val newAlert = ByteCharacteristic(this, Characteristics.NewAlert.uuid, "New Alert")
    val supportedNewAlertCategory = ByteCharacteristic(this, Characteristics.SupportedNewAlertCategory.uuid, "Supported New Alert Category")
    val supportedUnreadAlertCategory = ByteCharacteristic(this, Characteristics.SupportedUnreadAlertCategory.uuid, "Supported Unread Alert Category")

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