package network.xyo.ble.generic.gatt.peripheral

@Suppress("unused")
enum class XYBluetoothGattStatus(val status: Short) {
    NoResources(0x80),
    InternalError(0x81),
    WrongState(0x82),
    DBFull(0x83),
    Busy(0x84),
    Error(0x85),
    IllegalParameter(0x87),
    AuthFail(0x89)
}
