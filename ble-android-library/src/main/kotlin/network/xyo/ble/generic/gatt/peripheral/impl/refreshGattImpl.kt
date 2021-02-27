package network.xyo.ble.generic.gatt.peripheral.impl

import android.bluetooth.BluetoothGatt
import network.xyo.ble.generic.gatt.peripheral.ThreadSafeBluetoothGattWrapper
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode

fun refreshGattImpl(gatt: ThreadSafeBluetoothGattWrapper): XYBluetoothResult<Boolean> {
    val result = XYBluetoothResult<Boolean>()

    try {
        val localMethod = BluetoothGatt::class.java.getMethod("refresh")
        result.value = (localMethod.invoke(gatt) as Boolean)
    } catch (ex: NoSuchMethodException) {
        result.error = XYBluetoothResultErrorCode.FailedToRefreshGatt
    }

    return result
}
