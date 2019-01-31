package network.xyo.ble.gatt.peripheral

import kotlinx.coroutines.*
import network.xyo.ble.gatt.XYBluetoothBase
import kotlin.coroutines.CoroutineContext

//causes *all* ble calls to be initiated in a single thread
fun <T> asyncBle(
        context: CoroutineContext = XYBluetoothBase.BluetoothThread,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return GlobalScope.async(context, start, block)
}