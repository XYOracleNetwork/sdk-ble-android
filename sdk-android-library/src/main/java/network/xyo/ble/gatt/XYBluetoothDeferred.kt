package network.xyo.ble.gatt

import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

fun <T> asyncBle(
        context: CoroutineContext = XYBluetoothBase.BluetoothThread,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        parent: Job? = null,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return async(context, start, parent, block)
}