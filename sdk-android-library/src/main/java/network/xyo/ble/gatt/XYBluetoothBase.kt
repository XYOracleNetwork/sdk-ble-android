package network.xyo.ble.gatt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.asCoroutineDispatcher
import network.xyo.core.XYBase
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

open class XYBluetoothBase(context: Context) : XYBase() {

    //we store this since on initial creation, the applicationContext may not yet be available
    private val _context = context

    //we want to use the application context for everything
    protected val context: Context
        get() {
            return _context.applicationContext
        }

    protected val bluetoothManager: BluetoothManager?
        get() {
            return context.applicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        }

    protected val bluetoothAdapter: BluetoothAdapter?
        get() {
            return bluetoothManager?.adapter
        }

    companion object {
        //this is the thread that all calls should happen on for gatt calls.
        val BluetoothThread: CoroutineContext
        val BluetoothQueue = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        init {
            BluetoothThread = if (android.os.Build.VERSION.SDK_INT > 20) {
                Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            } else {
                //if the device is before 20, use the UI thread for the BLE calls
                object : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
                    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
                            AndroidContinuation(continuation)
                }
            }
        }

        private class AndroidContinuation<T>(val cont: Continuation<T>) : Continuation<T> by cont {
            override fun resumeWith(result: Result<T>) {
                if (Looper.myLooper() == Looper.getMainLooper()) cont.resumeWith(result)
                else Handler(Looper.getMainLooper()).post { cont.resumeWith(result) }
            }
        }

    }

}