package network.xyo.ble.gatt

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

//causes *all* ble calls to be initiated in a single thread
fun <T> asyncBle(
        context: CoroutineContext = XYBluetoothBase.BluetoothThread,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return GlobalScope.async(context, start, block)
}