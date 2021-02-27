package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

fun refreshGattImpl(gatt: XYThreadSafeBluetoothGatt): XYBluetoothResult<Boolean> {
    var result = false
    var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None

    try {
        val localMethod = BluetoothGatt::class.java.getMethod("refresh")
        result = (localMethod.invoke(gatt) as Boolean)
    } catch (ex: NoSuchMethodException) {
        error = XYBluetoothResultErrorCode.FailedToRefreshGatt
    }

    return XYBluetoothResult(result, error)
}
