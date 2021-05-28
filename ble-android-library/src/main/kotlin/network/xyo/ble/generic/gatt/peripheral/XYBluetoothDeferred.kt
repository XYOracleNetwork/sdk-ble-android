package network.xyo.ble.generic.gatt.peripheral

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import network.xyo.base.XYLogging
import network.xyo.ble.generic.XYBluetoothBase

class BleCoroutineScope : CoroutineScope {

    private var parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = XYBluetoothBase.BluetoothThread + parentJob

    fun onStart() {
        parentJob = Job()
    }

    fun onStop() {
        parentJob.cancel()
        // You can also cancel the whole scope with `cancel(cause: CancellationException)`
    }
}

val ble = BleCoroutineScope()

// causes *all* ble calls to be initiated in a single thread
// other functionality (non-gatt/ble initiating calls) should not be in these blocks
fun <T> bleAsync(
    timeout: Long = 10000L,
    context: CoroutineContext = XYBluetoothBase.BluetoothThread,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> XYBluetoothResult<T>
): Deferred<XYBluetoothResult<T>> {
    return ble.async(context, start) {
        try {
            return@async withTimeout(timeout) {
                return@withTimeout block()
            }
        } catch (ex: TimeoutCancellationException) {
            XYLogging("bleAsync").error("Excepted")
            XYLogging("bleAsync").error(ex)
            return@async XYBluetoothResult<T>(null, XYBluetoothResultErrorCode.Timeout)
        }
    }
}
