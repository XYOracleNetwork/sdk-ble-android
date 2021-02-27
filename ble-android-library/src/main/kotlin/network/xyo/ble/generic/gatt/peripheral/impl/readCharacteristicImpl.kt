package network.xyo.ble.generic.gatt.peripheral.impl

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattReadCharacteristic

suspend fun readCharacteristicImpl(
    connection: XYBluetoothGattConnect,
    characteristicToRead: BluetoothGattCharacteristic,
    callback: XYBluetoothGattCallback
) : XYBluetoothResult<BluetoothGattCharacteristic> {

    var result = XYBluetoothResult<BluetoothGattCharacteristic>()

    if (connection.disconnected) {
        result.error = XYBluetoothResultErrorCode.Disconnected
    } else {
        connection.gatt?.let { gatt ->
            val readCharacteristic = XYBluetoothGattReadCharacteristic(gatt, callback)
            result = readCharacteristic.start(characteristicToRead)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
    }
    return result
}
