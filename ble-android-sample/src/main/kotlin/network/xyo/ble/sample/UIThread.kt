package network.xyo.ble.sample

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class UiCoroutineScope : CoroutineScope {

    private var parentJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + parentJob

    fun onStart() {
        parentJob = Job()
    }

    fun onStop() {
        parentJob.cancel()
        // You can also cancel the whole scope with `cancel(cause: CancellationException)`
    }
}

val ui = UiCoroutineScope()
