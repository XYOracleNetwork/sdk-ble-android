package network.xyo.ble.generic.gatt.peripheral.actions

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.ble

open class XYBluetoothGattActionBase<T>(
        val timeout: Long)
    : XYBase() {
    var value: T? = null

    open fun completeStartCoroutine(cont: CancellableContinuation<T?>, value: T? = null) {
        ble.launch {
            val idempotent = cont.tryResume(value)
            idempotent?.let { token ->
                cont.completeResume(token)
            }
        }
    }
}
