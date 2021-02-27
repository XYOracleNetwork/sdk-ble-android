package network.xyo.ble.generic.gatt.peripheral.actions

import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.gatt.ThreadSafeBluetoothGattWrapper

open class XYBluetoothGattAction<T>(
    val gatt: ThreadSafeBluetoothGattWrapper,
    val gattCallback: XYBluetoothGattCallback,
    timeout: Long)
    : XYBluetoothGattActionBase<T>(timeout) {
}
