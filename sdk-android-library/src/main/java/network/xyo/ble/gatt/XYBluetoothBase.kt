package network.xyo.ble.gatt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper

import kotlinx.coroutines.newFixedThreadPoolContext
import network.xyo.core.XYBase
import kotlin.coroutines.*

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
        val BluetoothThread : CoroutineContext
        val BluetoothQueue = newFixedThreadPoolContext(1, "BluetoothQueue")

        init {
            BluetoothThread = if (android.os.Build.VERSION.SDK_INT < 20) {
                newFixedThreadPoolContext(1, "BluetoothThread")
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
                if (Looper.myLooper() == Looper.getMainLooper()) cont.resume(result.getOrThrow())
                else Handler(Looper.getMainLooper()).post { cont.resume(result.getOrThrow()) }
            }

            override val context: CoroutineContext
                get() = cont.context

        }

    }

}