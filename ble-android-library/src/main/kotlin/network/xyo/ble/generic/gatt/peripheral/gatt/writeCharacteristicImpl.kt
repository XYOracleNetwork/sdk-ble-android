package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.base.hasDebugger
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteCharacteristic
import java.lang.RuntimeException

suspend fun writeCharacteristicImpl(
    connection: XYBluetoothGattConnect,
    characteristicToWrite: BluetoothGattCharacteristic,
    writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
    callback: XYBluetoothGattCallback)
: XYBluetoothResult<ByteArray?> {
    if(hasDebugger && connection.state != BluetoothGatt.STATE_CONNECTED)
        throw RuntimeException("cannot read characteristic")
    val gatt = connection.gatt
    if (gatt != null) {
        val writeCharacteristic = XYBluetoothGattWriteCharacteristic(gatt, callback)
        return writeCharacteristic.start(characteristicToWrite, writeType)
    } else {
        return XYBluetoothResult(XYBluetoothResultErrorCode.NoGatt)
    }
}
