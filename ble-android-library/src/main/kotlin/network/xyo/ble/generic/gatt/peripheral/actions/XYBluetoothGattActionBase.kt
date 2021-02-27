package network.xyo.ble.generic.gatt.peripheral.actions

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import network.xyo.base.XYBase

open class XYBluetoothGattActionBase<T>(
        val timeout: Long)
    : XYBase() {
    var value: T? = null

    open fun completeStartCoroutine(cont: CancellableContinuation<T?>, value: T? = null) {
        GlobalScope.launch {
            val idempotent = cont.tryResume(value)
            idempotent?.let { token ->
                cont.completeResume(token)
            }
        }
    }
}
