package network.xyo.ble.generic.gatt.peripheral.impl

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteCharacteristic

suspend fun writeCharacteristicImpl(
    connection: XYBluetoothGattConnect,
    characteristicToWrite: BluetoothGattCharacteristic,
    writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    callback: XYBluetoothGattCallback
): XYBluetoothResult<ByteArray> {
    var result = XYBluetoothResult<ByteArray>()

    if (connection.disconnected) {
        result.error = XYBluetoothResultErrorCode.Disconnected
    } else {
        connection.gatt?.let { gatt ->
            result = XYBluetoothGattWriteCharacteristic(gatt, callback).start(characteristicToWrite, writeType)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
    }
    return result
}
