package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteDescriptor

suspend fun setCharacteristicNotifyImpl(
    connection: XYBluetoothGattConnect,
    characteristicToWrite: BluetoothGattCharacteristic,
    notify: Boolean
) : XYBluetoothResult<Boolean>{
    var result = XYBluetoothResult<Boolean>()

    if (connection.disconnected) {
        result.error = XYBluetoothResultErrorCode.Disconnected
    } else {
        connection.gatt?.let { gatt ->
            result = gatt.setCharacteristicNotification(characteristicToWrite, notify)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
    }

    return result
}
