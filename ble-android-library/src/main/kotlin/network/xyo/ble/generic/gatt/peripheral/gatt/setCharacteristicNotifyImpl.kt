package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect

suspend fun setCharacteristicNotifyImpl(
    connection: XYBluetoothGattConnect,
    characteristicToWrite: BluetoothGattCharacteristic,
    notify: Boolean
) : XYBluetoothResult<Boolean>{
    var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
    var value: Boolean? = null

    val gatt = connection.gatt

    if (gatt == null) {
        error = XYBluetoothResultErrorCode.NoGatt
    } else {
        value = gatt.setCharacteristicNotification(characteristicToWrite, notify).value
    }

    return XYBluetoothResult(value, error)
}
