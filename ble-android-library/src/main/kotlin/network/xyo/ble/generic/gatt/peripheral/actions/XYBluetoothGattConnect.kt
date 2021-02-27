package network.xyo.ble.generic.gatt.peripheral.actions

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.Handler
import kotlinx.coroutines.*
import network.xyo.base.hasDebugger
import network.xyo.ble.generic.gatt.peripheral.*
import network.xyo.ble.generic.gatt.peripheral.gatt.ThreadSafeBluetoothGattWrapper
import network.xyo.ble.utilities.XYCallByVersion
import java.lang.RuntimeException

class XYBluetoothGattConnect(
        val device: BluetoothDevice)
    : XYBluetoothGattActionBase<XYBluetoothResultErrorCode>(1500000L) {

    var gatt: ThreadSafeBluetoothGattWrapper? = null
    var services: List<BluetoothGattService>? = null

    // we make sure we always monitor the connection state so that we do not miss a message and
    // get out of sync
    var callback = object : XYBluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            this@XYBluetoothGattConnect.state = newState
            this@XYBluetoothGattConnect.status = status
        }
    }

    var state = BluetoothGatt.STATE_DISCONNECTED
    var status = BluetoothGatt.GATT_SUCCESS

    val disconnected
        get() = state != BluetoothGatt.STATE_CONNECTED

    val connected
        get() = state == BluetoothGatt.STATE_CONNECTED

    // make sure we always close connections
    protected fun finalize() {
        val gatt = this.gatt
        if (gatt != null) {
            log.error("finalize: Finalize closing up connection!!!!")
            GlobalScope.launch {
                log.info("finalize: launch")
                close()
            }
            this.gatt = null
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun connectGatt19(
        context: Context,
        device: BluetoothDevice,
        autoConnect: Boolean
    ): BluetoothGatt? {
        log.info("connectGatt19")
        return device.connectGatt(context, autoConnect, callback)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun connectGatt23(
        context: Context,
        device: BluetoothDevice,
        autoConnect: Boolean,
        transport: Int?
    ): BluetoothGatt? {
        log.info("connectGatt23")
        return if (transport == null) {
            device.connectGatt(context, autoConnect, callback)
        } else {
            device.connectGatt(context, autoConnect, callback, transport)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun connectGatt26(
        context: Context,
        device: BluetoothDevice,
        autoConnect: Boolean,
        transport: Int?,
        phy: Int?,
        handler: Handler?
    ): BluetoothGatt? {
        log.info("connectGatt26")

        return when {
            transport == null -> device.connectGatt(context, autoConnect, callback)
            phy == null -> device.connectGatt(context, autoConnect, callback, transport)
            handler == null -> device.connectGatt(context, autoConnect, callback, transport, phy)
            else -> device.connectGatt(context, autoConnect, callback, transport, phy, handler)
        }
    }

    private suspend fun connectGatt(context: Context, transport: Int? = null): XYBluetoothResult<ThreadSafeBluetoothGattWrapper> {
        log.info("connectGatt")
        var value: ThreadSafeBluetoothGattWrapper? = null
        val autoConnect = false
        val phy = null
        val handler = null
        var error = XYBluetoothResultErrorCode.None

        if (gatt != null) {
            log.info("Arie: Returning Known Gatt")
            return XYBluetoothResult(gatt)
        }

        val result = bleAsync {
            var newGatt: ThreadSafeBluetoothGattWrapper? = null
            XYCallByVersion()
                .add(Build.VERSION_CODES.O) {
                    newGatt = ThreadSafeBluetoothGattWrapper(
                        connectGatt26(
                            context,
                            device,
                            autoConnect,
                            transport,
                            phy,
                            handler
                        )
                    )
                }
                .add(Build.VERSION_CODES.M) {
                    newGatt = ThreadSafeBluetoothGattWrapper(
                        connectGatt23(
                            context,
                            device,
                            autoConnect,
                            transport
                        )
                    )
                }
                .add(Build.VERSION_CODES.KITKAT) {
                    newGatt =
                        ThreadSafeBluetoothGattWrapper(connectGatt19(context, device, autoConnect))
                }.call()
            return@bleAsync XYBluetoothResult(newGatt)
        }.await()

        if (gatt != null) {
            throw Exception("Gatt Unexpectedly Appeared")
        }
        
        gatt = result.value

        if (result.value == null) {
            error = XYBluetoothResultErrorCode.FailedToConnectGatt
        } else {
            value = result.value
        }

        return XYBluetoothResult(value, error)
    }

    private suspend fun discover(): XYBluetoothResult<List<BluetoothGattService>> {
        log.info("discover")
        if(state != BluetoothGatt.STATE_CONNECTED) {
            if (hasDebugger) {
                throw RuntimeException("cannot read characteristic")
            } else {
                return XYBluetoothResult(XYBluetoothResultErrorCode.FailedToConnect)
            }
        }

        val gatt = this@XYBluetoothGattConnect.gatt // make thread safe
        return if (gatt != null) {
            val discover = XYBluetoothGattDiscover(gatt, callback)
            val discoverResult = discover.start()
            if (discoverResult.error == XYBluetoothResultErrorCode.None) {
                services = discoverResult.value
            }
            discoverResult
        } else {
            XYBluetoothResult(XYBluetoothResultErrorCode.NoGatt)
        }
    }

    private suspend  fun startWithNewGatt(context: Context, transport: Int? = null): XYBluetoothResultErrorCode {
        val gattConnectResult = connectGatt(context, transport)
        return if (gattConnectResult.error != XYBluetoothResultErrorCode.None) {
            gattConnectResult.error
        } else {
            gatt = gattConnectResult.value
            XYBluetoothResultErrorCode.None
        }
    }

    private suspend fun startWithExistingGatt(): XYBluetoothResultErrorCode {
        return gatt?.connect()?.error ?: XYBluetoothResultErrorCode.NoGatt
    }

    override fun completeStartCoroutine(cont: CancellableContinuation<XYBluetoothResultErrorCode?>, value: XYBluetoothResultErrorCode?) {
        GlobalScope.launch {
            if (value != XYBluetoothResultErrorCode.None) {
                close()
            }
            super.completeStartCoroutine(cont, value)
        }
    }

    suspend fun start(context: Context, transport: Int? = null) : XYBluetoothResult<Boolean> {
        val listenerName = "XYBluetoothGattConnect${hashCode()}"

        var error: XYBluetoothResultErrorCode? = if (gatt == null) startWithNewGatt(context, transport) else startWithExistingGatt()
        val connectStartSuccess = error == XYBluetoothResultErrorCode.None

        //not already connected, so try to connect
        if (connectStartSuccess && state != BluetoothGatt.STATE_CONNECTED) {
            error = suspendCancellableCoroutine { cont ->
                val listener = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        this@XYBluetoothGattConnect.state = newState
                        this@XYBluetoothGattConnect.status = status
                        callback.removeListener(listenerName)
                        when {
                            status == BluetoothGatt.GATT_FAILURE -> {
                                log.info("connect:failure: $status : $newState")
                                completeStartCoroutine(cont, XYBluetoothResultErrorCode.GattFailure)
                            }
                            newState == BluetoothGatt.STATE_CONNECTED -> {
                                log.info("connect:connected")
                                GlobalScope.launch {
                                    completeStartCoroutine(cont, discover().error)
                                }
                            }

                            else -> {
                                completeStartCoroutine(cont, XYBluetoothResultErrorCode.FailedToConnect)
                            }
                        }
                    }
                }
                callback.addListener(listenerName, listener)

                // check if a connection happened while we were setting things up
                if (state == BluetoothGatt.STATE_CONNECTED) {
                    log.info("connect:already connected")
                    callback.removeListener(listenerName)
                    completeStartCoroutine(cont, XYBluetoothResultErrorCode.None)
                }
            }
        }
        val resultError = error ?: XYBluetoothResultErrorCode.None
        return XYBluetoothResult(error == XYBluetoothResultErrorCode.None, resultError)
    }

    suspend fun disconnect() = withContext(Dispatchers.Default) {
        gatt?.disconnect()
    }

    suspend fun close() = withContext(Dispatchers.Default) {
        log.info("disconnect:close")
        disconnect()
        gatt?.close()
        gatt = null
    }
}
