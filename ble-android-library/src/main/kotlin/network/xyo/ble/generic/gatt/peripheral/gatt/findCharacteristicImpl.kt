package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import java.util.*

// this can only be called after a successful discover
fun findCharacteristicImpl(connection: XYBluetoothGattConnect, service: UUID, characteristic: UUID)
    : XYBluetoothResult<BluetoothGattCharacteristic> {
    var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
    var value: BluetoothGattCharacteristic? = null

    val callingGatt = connection.gatt

    if (callingGatt == null) {
        error = XYBluetoothResultErrorCode.NoGatt
    } else {
        val services = connection.services
        if (services?.isEmpty() == false) {
            val foundService = callingGatt.getService(service)
            if (foundService == null) {
                error = XYBluetoothResultErrorCode.FailedToFindService
            } else {
                value = foundService.getCharacteristic(characteristic)
            }
        } else {
            error = XYBluetoothResultErrorCode.ServicesNotDiscovered
        }
    }
    return XYBluetoothResult(value, error)
}
