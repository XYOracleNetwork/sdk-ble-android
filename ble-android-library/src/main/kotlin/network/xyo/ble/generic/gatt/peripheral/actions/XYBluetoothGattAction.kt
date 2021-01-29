package network.xyo.ble.generic.gatt.peripheral.actions

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

open class XYBluetoothGattAction<T>(
        val gatt: XYThreadSafeBluetoothGatt,
        val gattCallback: XYBluetoothGattCallback,
        val timeout: Long)
    : XYBase() {
    var value: T? = null

    fun completeStartCoroutine(cont: CancellableContinuation<T?>, value: T? = null) {
        GlobalScope.launch {
            val idempotent = cont.tryResume(value)
            idempotent?.let { token ->
                cont.completeResume(token)
            }
        }
    }
}
