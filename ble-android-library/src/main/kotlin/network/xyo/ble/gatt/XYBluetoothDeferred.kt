package network.xyo.ble.gatt


import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

//causes *all* ble calls to be initiated in a single thread
fun <T> asyncBle(
        context: CoroutineContext = XYBluetoothBase.BluetoothThread,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return GlobalScope.async(context, start, block)
}

//forces items to complete in order - use this only for the base read/write calls to make sure they do
//not overlap (which causes problems)
//we should consider doing a queue per device in the future
fun <T> queueBle(
        context: CoroutineContext = XYBluetoothBase.BluetoothQueue,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return runBlocking {
        return@runBlocking async(context, start, block)
    }
}