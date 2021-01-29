package network.xyo.ble.generic.gatt.peripheral.actions

import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

open class XYBluetoothGattAction<T>(
        val gatt: XYThreadSafeBluetoothGatt,
        val gattCallback: XYBluetoothGattCallback,
        timeout: Long)
    : XYBluetoothGattActionBase<T>(timeout) {
}
