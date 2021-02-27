package network.xyo.ble.generic.gatt.peripheral.impl

import android.bluetooth.BluetoothGattDescriptor
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteDescriptor

suspend fun writeDescriptorImpl(
    connection: XYBluetoothGattConnect,
    descriptorToWrite: BluetoothGattDescriptor,
    callback: XYBluetoothGattCallback
): XYBluetoothResult<ByteArray> {
    var result = XYBluetoothResult<ByteArray>()

    if (connection.disconnected) {
        result.error = XYBluetoothResultErrorCode.Disconnected
    } else {
        connection.gatt?.let { gatt ->
            result = XYBluetoothGattWriteDescriptor(gatt, callback).start(descriptorToWrite)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
    }

    return result
}
