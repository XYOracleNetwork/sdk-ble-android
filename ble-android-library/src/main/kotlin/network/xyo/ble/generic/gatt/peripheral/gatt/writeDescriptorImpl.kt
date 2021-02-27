package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import network.xyo.base.hasDebugger
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteDescriptor
import java.lang.RuntimeException

suspend fun writeDescriptorImpl(
    connection: XYBluetoothGattConnect,
    descriptorToWrite: BluetoothGattDescriptor,
    callback: XYBluetoothGattCallback
): XYBluetoothResult<ByteArray?> {
    if(hasDebugger && connection.state != BluetoothGatt.STATE_CONNECTED)
        throw RuntimeException("cannot read characteristic")
    val gatt = connection.gatt
    if (gatt != null) {
        val writeDescriptor = XYBluetoothGattWriteDescriptor(gatt, callback)
        return writeDescriptor.start(descriptorToWrite)
    } else {
        return XYBluetoothResult<ByteArray?>(XYBluetoothResultErrorCode.NoGatt)
    }
}
