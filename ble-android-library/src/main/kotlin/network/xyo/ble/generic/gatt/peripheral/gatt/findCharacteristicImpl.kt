package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import java.util.*

// this can only be called after a successful discover
fun findCharacteristicImpl(connection: XYBluetoothGattConnect, service: UUID, characteristic: UUID)
    : XYBluetoothResult<BluetoothGattCharacteristic> {
    val result = XYBluetoothResult<BluetoothGattCharacteristic>()

    if (connection.disconnected) {
        result.error = XYBluetoothResultErrorCode.Disconnected
    } else {
        connection.gatt?.let { gatt ->
            val services = connection.services
            if (services?.isEmpty() == false) {
                val foundService = gatt.service(service)
                if (foundService == null) {
                    result.error = XYBluetoothResultErrorCode.FailedToFindService
                } else {
                    result.value = foundService.getCharacteristic(characteristic)
                }
            } else {
                result.error = XYBluetoothResultErrorCode.ServicesNotDiscovered
            }
        } ?: run {result.error = XYBluetoothResultErrorCode.NoGatt}
    }
    return result
}
