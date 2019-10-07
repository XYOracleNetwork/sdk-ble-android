package network.xyo.ble.generic.gatt.peripheral

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import network.xyo.base.XYLogging
import network.xyo.ble.generic.XYBluetoothBase

// causes *all* ble calls to be initiated in a single thread
// other functionality (non-gatt/ble initiating calls) should not be in these blocks
suspend fun <T> asyncBle(
    timeout: Long = 10000L,
    context: CoroutineContext = XYBluetoothBase.BluetoothThread,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T?
): T? {
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
    }.await()
}
