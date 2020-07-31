package network.xyo.ble.generic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.asCoroutineDispatcher
import network.xyo.base.XYBase

enum class XYBluetoothBaseStatus(val status: Short) {
    Success(0x00),
    UnknownBtLeCommand(0x01),
    UnknownConnectionIdentifier(0x02),
    AuthenticationFailure(0x03),
    PinOrKeyMissing(0x06),
    MemoryCapacityExceeded(0x07),
    Timeout(0x08),
    CommandDisallowed(0x0c),
    InvalidBtLeCommandParameters(0x12),
    RemoteUserTerminatedConnection(0x13),
    RemoteDevTerminationFromLowResources(0x14),
    RemoteDevTerminationFromPowerOff(0x15),
    LocalHostTerminatedConnection(0x16),
    UnsupportedRemoteFeature(0x1a),
    InvalidLmpParameters(0x1e),
    UnspecifiedError(0x1f),
    LmpResponseTimeout(0x22),
    LmpPduNotAllowed(0x24),
    InstantPassed(0x28),
    PairingWithUnitKeyUnsupported(0x29),
    DifferentTransactionCollision(0x2a),
    ControllerBusy(0x3a),
    ConnIntervalUnacceptable(0x3b),
    DirectedAdvertiserTimeout(0x3c),
    TerminatedDueToMicFailure(0x3d),
    FailedToEstablish(0x3e)
}

open class XYBluetoothBase(context: Context) : XYBase() {

    // we store this since on initial creation, the applicationContext may not yet be available
    private val _context = context

    // we want to use the application context for everything
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

    protected val isEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled ?: false

    protected val isDiscovering: Boolean
        get() = bluetoothAdapter?.isDiscovering ?: false

    protected val isLe2MPhySupported: Boolean
        get() {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                return bluetoothAdapter?.isLe2MPhySupported ?: false
            }
            return false
        }

    protected val isLeCodedPhySupported: Boolean
        get() {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                return bluetoothAdapter?.isLeCodedPhySupported ?: false
            }
            return false
        }

    protected val isLeExtendedAdvertisingSupported: Boolean
        get() {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                return bluetoothAdapter?.isLeExtendedAdvertisingSupported ?: false
            }
            return false
        }

    protected val isLePeriodicAdvertisingSupported: Boolean
        get() {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                return bluetoothAdapter?.isLePeriodicAdvertisingSupported ?: false
            }
            return false
        }

    protected val isMultipleAdvertisementSupported: Boolean
        get() = bluetoothAdapter?.isMultipleAdvertisementSupported ?: false

    protected val isOffloadedFilteringSupported: Boolean
        get() = bluetoothAdapter?.isOffloadedFilteringSupported ?: false

    protected val isOffloadedScanBatchingSupported: Boolean
        get() = bluetoothAdapter?.isOffloadedScanBatchingSupported ?: false

    companion object : XYBase() {
        // this is the thread that all calls should happen on for gatt calls.
        internal val BluetoothThread = when {
            (android.os.Build.VERSION.SDK_INT >= 26) -> {
                Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            }
            (android.os.Build.VERSION.SDK_INT >= 21) -> {
                Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            }
            else -> {
                // if the device is before 20, use the UI thread for the BLE calls
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
