package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.base.hasDebugger
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattReadCharacteristic
import java.lang.RuntimeException

suspend fun readCharacteristicImpl(
    connection: XYBluetoothGattConnect,
    characteristicToRead: BluetoothGattCharacteristic,
    callback: XYBluetoothGattCallback
) : XYBluetoothResult<BluetoothGattCharacteristic?> {
    if(hasDebugger && connection.state != BluetoothGatt.STATE_CONNECTED)
        throw RuntimeException("cannot read characteristic")
    val gatt = connection.gatt
    return if (gatt != null) {
        val readCharacteristic = XYBluetoothGattReadCharacteristic(gatt, callback)
        readCharacteristic.start(characteristicToRead)
    } else {
        XYBluetoothResult(XYBluetoothResultErrorCode.NoGatt)
    }
}
