package network.xyo.ble.generic.gatt.peripheral

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import network.xyo.ble.generic.XYBluetoothBase

// causes *all* ble calls to be initiated in a single thread
// other functionality (non-gatt/ble initiating calls) should not be in these blocks

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
