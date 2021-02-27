package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode

fun refreshGattImpl(gatt: ThreadSafeBluetoothGattWrapper): XYBluetoothResult<Boolean> {
    var result = XYBluetoothResult<Boolean>()

    try {
        val localMethod = BluetoothGatt::class.java.getMethod("refresh")
        result.value = (localMethod.invoke(gatt) as Boolean)
    } catch (ex: NoSuchMethodException) {
        result.error = XYBluetoothResultErrorCode.FailedToRefreshGatt
    }

    return result
}
