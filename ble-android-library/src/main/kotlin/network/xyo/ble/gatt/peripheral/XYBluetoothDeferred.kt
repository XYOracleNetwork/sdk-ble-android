package network.xyo.ble.gatt.peripheral

import kotlinx.coroutines.*
import network.xyo.ble.XYBluetoothBase
import network.xyo.core.XYLogging
import kotlin.coroutines.CoroutineContext

//causes *all* ble calls to be initiated in a single thread
//other functionality (non-gatt/ble initiating calls) should not be in these blocks
fun <T> asyncBle(
        timeout: Long = 10000L,
        context: CoroutineContext = XYBluetoothBase.BluetoothThread,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T?
): Deferred<T?> {
    var result: T? = null
    XYLogging("asyncBle").info("Enter")
    return GlobalScope.async(context, start) {
        XYLogging("asyncBle").info("Global Scope")
        try {
            result = withTimeout(timeout) {
                XYLogging("asyncBle").info("withTimeout")
                return@withTimeout block()
            }
        } catch (ex: TimeoutCancellationException) {
            XYLogging("asyncBle").info("Excepted")
            XYLogging("asyncBle").error(ex)
        }
        XYLogging("asyncBle").info("Returning")
        return@async result
    }
}