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
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.*
import network.xyo.ble.utilities.XYCallByVersion

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
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: XYThreadSafeBluetoothGatt? = null
        val autoConnect = false
        val phy = null
        val handler = null

        assert(gatt == null)

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
        assert(state != BluetoothGatt.STATE_CONNECTED)
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

    suspend fun start(context: Context, transport: Int? = null) = GlobalScope.async {
        log.info("connect: start")

        val listenerName = "XYBluetoothGattConnect${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: Boolean? = null

        var connectStartSuccess = false

        if (gatt == null) {
            val gattConnectResult = connectGatt(context, transport)
            if (gattConnectResult.error != XYBluetoothResultErrorCode.None) {
                error = gattConnectResult.error
            } else {
                gatt = gattConnectResult.value
                connectStartSuccess = true
            }
        } else {
            val connectStarted = gatt?.connect()
            if (connectStarted != true) {
                error = XYBluetoothResultErrorCode.ConnectFailedToStart
            } else {
                connectStartSuccess = true
            }
        }

        if (connectStartSuccess && state != BluetoothGatt.STATE_CONNECTED) {
            value = suspendCancellableCoroutine { cont ->
                log.info("connect: suspendCancellableCoroutine")
                val listener = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        callback.removeListener(listenerName)
                        when {
                            status == BluetoothGatt.GATT_FAILURE -> {
                                log.info("connect:failure: $status : $newState")
                                error = XYBluetoothResultErrorCode.GattFailure
                                callback.removeListener(listenerName)
                                GlobalScope.launch {
                                    close()

                                    val idempotent = cont.tryResume(null)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                }
                            }
                            newState == BluetoothGatt.STATE_CONNECTED -> {
                                log.info("connect:connected")
                                callback.removeListener(listenerName)
                                GlobalScope.launch {
                                    if (discover().error == XYBluetoothResultErrorCode.None) {
                                        val idempotent = cont.tryResume(true)
                                        idempotent?.let {
                                            cont.completeResume(it)
                                        }
                                    } else {
                                        GlobalScope.launch {
                                            close()
                                            val idempotent = cont.tryResume(null)
                                            idempotent?.let {
                                                cont.completeResume(it)
                                            }
                                        }
                                    }
                                }
                            }

                            newState == BluetoothGatt.STATE_CONNECTING -> log.info("connect:connecting")

                            else -> {
                                error = XYBluetoothResultErrorCode.FailedToConnect
                                callback.removeListener(listenerName)
                                GlobalScope.launch {
                                    close()
                                    val idempotent = cont.tryResume(null)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                }
                            }
                        }
                    }
                }
                callback.addListener(listenerName, listener)

                // check if a connection happened while we were setting things up
                if (state == BluetoothGatt.STATE_CONNECTED) {
                    log.info("connect:already connected")
                    callback.removeListener(listenerName)
                    val idempotent = cont.tryResume(true)
                    idempotent?.let {
                        cont.completeResume(it)
                    }
                }
            }
        }

        log.info("connect: Returning[$value][$error]")
        return@async XYBluetoothResult(value, error)
    }.await()

    suspend fun close() = withContext(Dispatchers.Default) {
        log.info("disconnect:close")
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    companion object : XYBase()
}
