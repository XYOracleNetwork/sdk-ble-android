package network.xyo.ble.generic.gatt.peripheral.actions

import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import android.os.Handler
import com.jaredrummler.android.device.BuildConfig
import kotlinx.coroutines.*
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.*
import network.xyo.ble.utilities.XYCallByVersion
import java.lang.RuntimeException

class XYBluetoothGattConnect(val device: BluetoothDevice) : XYBase() {

    private var _timeout = 1500000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    var gatt: XYThreadSafeBluetoothGatt? = null
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

    private suspend fun connectGatt(context: Context, transport: Int? = null) = GlobalScope.async {
        log.info("connectGatt")
        var value: XYThreadSafeBluetoothGatt? = null
        val autoConnect = false
        val phy = null
        val handler = null
        var error = XYBluetoothResultErrorCode.None

        if(BuildConfig.DEBUG && gatt == null)
            throw RuntimeException("cannot read characteristic")

        val result = asyncBle {
            var newGatt: XYThreadSafeBluetoothGatt? = null
            XYCallByVersion()
                    .add(Build.VERSION_CODES.O) {
                        newGatt = XYThreadSafeBluetoothGatt(connectGatt26(context, device, autoConnect, transport, phy, handler))
                    }
                    .add(Build.VERSION_CODES.M) {
                        newGatt = XYThreadSafeBluetoothGatt(connectGatt23(context, device, autoConnect, transport))
                    }
                    .add(Build.VERSION_CODES.KITKAT) {
                        newGatt = XYThreadSafeBluetoothGatt(connectGatt19(context, device, autoConnect))
                    }.call()
            return@asyncBle XYBluetoothResult(newGatt)
        }

        if (result?.value == null) {
            error = XYBluetoothResultErrorCode.FailedToConnectGatt
        } else {
            value = result.value
        }

        return@async XYBluetoothResult(value, error)
    }.await()

    private suspend fun discover() = GlobalScope.async {
        log.info("discover")
        if(BuildConfig.DEBUG && state != BluetoothGatt.STATE_CONNECTED)
            throw RuntimeException("cannot read characteristic")
        val gatt = this@XYBluetoothGattConnect.gatt // make thread safe
        if (gatt != null) {
            val discover = XYBluetoothGattDiscover(gatt, callback)
            val discoverResult = discover.start()
            if (discoverResult.error == XYBluetoothResultErrorCode.None) {
                services = discoverResult.value
            }
            return@async discoverResult
        } else {
            return@async XYBluetoothResult<List<BluetoothGattService>>(XYBluetoothResultErrorCode.NoGatt)
        }
    }.await()

    private suspend  fun startWithNewGatt(context: Context, transport: Int? = null): XYBluetoothResultErrorCode {
        val gattConnectResult = connectGatt(context, transport)
        if (gattConnectResult.error != XYBluetoothResultErrorCode.None) {
            return gattConnectResult.error
        } else {
            gatt = gattConnectResult.value
            return XYBluetoothResultErrorCode.None
        }
    }

    private suspend fun startWithExistingGatt(): XYBluetoothResultErrorCode {
        val connectStarted = gatt?.connect()
        if (connectStarted != true) {
            return XYBluetoothResultErrorCode.ConnectFailedToStart
        } else {
            return XYBluetoothResultErrorCode.None
        }
    }

    fun completeStartCoroutine(cont: CancellableContinuation<XYBluetoothResultErrorCode>, error: XYBluetoothResultErrorCode) {
        GlobalScope.launch {
            if (error != XYBluetoothResultErrorCode.None) {
                close()
            }
            cont.tryResume(error)?.let { token ->
                cont.completeResume(token)
            }
        }
    }

    suspend fun start(context: Context, transport: Int? = null) : XYBluetoothResult<Boolean> {
        val listenerName = "XYBluetoothGattConnect${hashCode()}"

        var error = if (gatt == null) startWithNewGatt(context, transport) else startWithExistingGatt()
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

        return XYBluetoothResult(error == XYBluetoothResultErrorCode.None, error)
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
