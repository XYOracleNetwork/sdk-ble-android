package network.xyo.ble.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import kotlinx.coroutines.*
import network.xyo.ble.CallByVersion
import network.xyo.ble.gatt.*
import network.xyo.ble.gatt.peripheral.actions.XYBluetoothGattDiscover
import network.xyo.ble.gatt.peripheral.actions.XYBluetoothGattWriteCharacteristic
import network.xyo.ble.scanner.XYScanResult
import network.xyo.core.XYBase
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

//XYBluetoothGatt is a pure wrapper that does not add any functionality
//other than the ability to call the BluetoothGatt functions using coroutines

open class XYBluetoothGatt protected constructor(
        context: Context,
        protected var device: BluetoothDevice?,
        private var autoConnect: Boolean,
        private val callback: XYBluetoothGattCallback?,
        private val transport: Int?,
        private val phy: Int?,
        private val handler: Handler?
) : XYBluetoothGattBase(context) {

    enum class BleConnectionStatus(val status: Short) {
        Success (0),
        Timeout(8),
        RemoteDisconnect(19),
        FailedToEstablish(62),
        NoResources(128),
        IntertnalError(129),
        WrongState(130),
        DBFull(131),
        Busy(132),
        GattError(133),
        IllegalParameter(135),
        AuthFail(137)
    }

    val bluetoothQueue = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    protected val centralCallback = object: XYBluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            _connectionState = newState
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            this@XYBluetoothGatt.rssi = rssi
            onDetect(null)
        }
    }

    private var _references = 0
    protected var references: Int
        get() {
            log.info("References Get: $_references")
            return _references
        }
        set(value) {
            _references = value
            log.info("References Set: $_references")
        }

    //last time this device was accessed (connected to)
    protected var lastAccessTime = 0L

    //last time we heard a ad from this device
    protected var lastAdTime = 0L

    var rssi: Int? = null

    val _timeout = 15000L

    //force ble functions for this gatt to run in order
    fun <T> queueBle(
            timeout: Long = _timeout,
            context: CoroutineContext = bluetoothQueue,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> XYBluetoothResult<T>
    ): Deferred<XYBluetoothResult<T>> {
        lastAccessTime = now
        return GlobalScope.async(context, start) {
            lastAccessTime = now
            return@async runBlocking {
                lastAccessTime = now
                try {
                    return@runBlocking withTimeout(timeout) {
                        lastAccessTime = now
                        return@withTimeout block()
                    }
                } catch (ex: TimeoutCancellationException) {
                    log.error("queueBle Timeout")
                    log.error(ex)
                    return@runBlocking XYBluetoothResult<T>(XYBluetoothError(ex.message
                            ?: "Exception"))
                }
            }
        }
    }

    protected var _stayConnected = false

    fun getStayConnected() : Boolean {
        return _stayConnected
    }

    fun setStayConnected(value: Boolean) = GlobalScope.async {
        if (value == _stayConnected) {
            return@async XYBluetoothResult(value)
        }

        _stayConnected = value
        if (!_stayConnected) {
            cleanUpIfNeeded()
            return@async XYBluetoothResult(true)
        } else {
            val gattResult = connectGatt().await()
            if (gattResult.error != null) {
                return@async connect().await()
            } else {
                return@async gattResult
            }
        }
    }

    val closed: Boolean
        get() = (gatt == null)

    internal var gatt: BluetoothGatt? = null

    internal open fun onDetect(scanResult: XYScanResult?) {

    }

    internal open fun onConnectionStateChange(newState: Int) {

    }

    fun requestMtu (mtu : Int) : Deferred<XYBluetoothResult<Int>> = asyncBle {
        return@asyncBle suspendCancellableCoroutine<XYBluetoothResult<Int>> { cont ->
            val key = "$mtu requestMtu $nowNano"

            centralCallback.addListener(key, object : BluetoothGattCallback() {
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)

                    if (gatt == this@XYBluetoothGatt.gatt) {

                        centralCallback.removeListener(key)

                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            cont.resume(XYBluetoothResult(mtu, null))
                            return
                        }

                        cont.resume(XYBluetoothResult(mtu, XYBluetoothError(status.toString())))
                    }
                }
            })

            gatt?.requestMtu(mtu)
        }
    }

    fun waitForNotification (characteristicToWaitFor: UUID): Deferred<XYBluetoothResult<Any?>> = asyncBle {
        log.info("waitForNotification")
        return@asyncBle suspendCancellableCoroutine<XYBluetoothResult<Any?>> { cont ->
            val listenerName = "waitForNotification$nowNano"
            val listener = object : BluetoothGattCallback() {
                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    super.onCharacteristicChanged(gatt, characteristic)
                    if (gatt == this@XYBluetoothGatt.gatt) {
                        if (characteristicToWaitFor == characteristic?.uuid) {
                            centralCallback.removeListener(listenerName)
                            cont.resume<XYBluetoothResult<Any?>>(XYBluetoothResult(null, null))
                        }
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (gatt == this@XYBluetoothGatt.gatt) {
                        if (newState != BluetoothGatt.STATE_CONNECTED) {
                            centralCallback.removeListener(listenerName)
                            cont.resume<XYBluetoothResult<Any?>>(XYBluetoothResult(null, XYBluetoothError("Device disconnected!")))
                        }
                    }
                }
            }

            centralCallback.addListener(listenerName, listener)
        }
    }

    private fun refreshGatt() = asyncBle {
        log.info("refreshGatt")
        var result = false
        var error: XYBluetoothError? = null

        val gatt = this@XYBluetoothGatt.gatt
        if (gatt == null) {
            error = XYBluetoothError("connect: No Gatt")
        } else {
            try {
                val localMethod = BluetoothGatt::class.java.getMethod("refresh")
                log.info("refreshGatt found method $localMethod")
                result = (localMethod.invoke(gatt) as Boolean)
            } catch (ex: NoSuchMethodException) {
                //null receiver
                error = XYBluetoothError("refreshGatt: Failed to refresh gatt")
                log.error("refreshGatt catch $ex", true)
                //method not found
            }
        }
        return@asyncBle XYBluetoothResult(result, error)
    }

    private fun connectGatt() = GlobalScope.async {
        log.info("connectGatt")
        var error: XYBluetoothError? = null
        var value: Boolean? = null

        val device = this@XYBluetoothGatt.device
        if (device == null) {
            error = XYBluetoothError("connectGatt: No Device")
        } else {

            var callingGatt = this@XYBluetoothGatt.gatt

            if (callback != null) {
                centralCallback.addListener("default", callback)
            }
            if (callingGatt == null) {
                this@XYBluetoothGatt.gatt = asyncBle {
                    CallByVersion()
                            .add(Build.VERSION_CODES.O) {
                                callingGatt = connectGatt26(device, autoConnect, transport, phy, handler)
                            }
                            .add(Build.VERSION_CODES.M) {
                                callingGatt = connectGatt23(device, autoConnect, transport)
                            }
                            .add(Build.VERSION_CODES.KITKAT) {
                                callingGatt = connectGatt19(device, autoConnect)
                            }.call()
                    return@asyncBle XYBluetoothResult(callingGatt)
                }.await().value
                if (callingGatt == null) {
                    error = XYBluetoothError("connectGatt: Failed to get gatt")
                }
                cleanUpIfNeeded()
            } else {
                value = true
            }
        }
        return@async XYBluetoothResult(value, error)
    }

    fun connect() = queueBle(7500) {
        log.info("connect")

        //this prevents a queued close from closing while we run
        lastAccessTime = now

        var error: XYBluetoothError? = null
        var value: Boolean? = null
        var callingGatt = this@XYBluetoothGatt.gatt

        if (callingGatt == null) {
            val gattConnectResult = connectGatt().await()
            if (gattConnectResult.error != null) {
                error = gattConnectResult.error
            } else {
                callingGatt = this@XYBluetoothGatt.gatt
                if (callingGatt == null) {
                    error = XYBluetoothError("connect: No Gatt")
                }
            }
        }
        if (callingGatt != null && error == null) {
            val listenerName = "connect$nowNano"
            value = suspendCancellableCoroutine { cont ->
                log.info("connect: suspendCancellableCoroutine")
                val listener = object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        centralCallback.removeListener(listenerName)
                        when {
                            status == BluetoothGatt.GATT_FAILURE -> {
                                log.info("connect:failure: $status : $newState")
                                error = XYBluetoothError("connect: connection failed(status): $status : $newState")
                                centralCallback.removeListener(listenerName)
                                cont.resume(null)
                            }
                            newState == BluetoothGatt.STATE_CONNECTED -> {
                                log.info("connect:connected")
                                centralCallback.removeListener(listenerName)
                                cont.resume(true)
                            }

                            newState == BluetoothGatt.STATE_CONNECTING -> log.info("connect:connecting")

                            else -> {
                                error = XYBluetoothError("connect: connection failed unknown(state): $status : $newState")
                                centralCallback.removeListener(listenerName)
                                cont.resume(null)
                            }
                        }
                    }
                }
                centralCallback.addListener(listenerName, listener)

                if (connectionState == ConnectionState.Connected) {
                    log.info("connect:already connected")
                    centralCallback.removeListener(listenerName)
                    cont.resume(true)
                } else if (connectionState == ConnectionState.Connecting) {
                    log.info("connect:connecting")
                    //dont call connect since already in progress
                } else {
                    GlobalScope.launch {
                        asyncBle {
                            val connectStarted = callingGatt.connect()
                            if (!connectStarted) {
                                error = XYBluetoothError("start: gatt.connectStarted failed to start")
                                centralCallback.removeListener(listenerName)
                                cont.resume(null)
                            }
                            return@asyncBle XYBluetoothResult(connectStarted)
                        }.await()
                    }
                }
            }
        }
        log.info("connect: Returning[$value]")
        return@queueBle XYBluetoothResult(value, error)
    }

    fun disconnect() = queueBle(1300) {
        log.info("disconnect:start")

        var error: XYBluetoothError? = null

        val callingGatt = this@XYBluetoothGatt.gatt
                ?: return@queueBle XYBluetoothResult(true, XYBluetoothError("Already Disconnected"))

        val listenerName = "disconnect: ${this@XYBluetoothGatt.device?.address}"
        val value = suspendCancellableCoroutine<Boolean> { cont ->
            val listener = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    when {
                        status == BluetoothGatt.GATT_FAILURE -> {
                            error = XYBluetoothError("disconnect: disconnection failed(status): $status : $newState")
                            centralCallback.removeListener(listenerName)
                            cont.resume(false)
                        }
                        newState == BluetoothGatt.STATE_DISCONNECTED -> {
                            log.info("disconnect:disconnected")
                            centralCallback.removeListener(listenerName)
                            cont.resume(true)
                        }
                        newState == BluetoothGatt.STATE_DISCONNECTING -> {
                            log.info("disconnect:disconnecting")
                            //wait some more
                        }
                        else -> {
                            log.info("disconnect:other")
                            // error = XYBluetoothError("asyncDisconnect: connection failed(state): $status : $newState")
                            // cont.resume(null)
                        }
                    }
                }
            }
            centralCallback.addListener(listenerName, listener)

            when (connectionState) {
                ConnectionState.Disconnected -> {
                    log.info("disconnect:already disconnected (upfront)")
                    centralCallback.removeListener(listenerName)
                    cont.resume(true)
                }
                ConnectionState.Disconnecting -> log.info("disconnect:disconnecting (upfront)")
                //dont call connect since already in progress
                else -> {
                    GlobalScope.launch {
                        asyncBle {
                            callingGatt.disconnect()
                            return@asyncBle XYBluetoothResult(true)
                        }.await()
                    }
                }
            }
        }

        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun close() = queueBle(1900) {
        log.info("close:enter($gatt)")
        val gatt = gatt ?: return@queueBle XYBluetoothResult(true)
        if (connectionState != ConnectionState.Disconnected) {
            log.info("close:disconnecting")
            disconnect().await()
        }
        log.info("close:thread")
        return@queueBle GlobalScope.async {
            return@async asyncBle {
                log.info("close:closing")
                gatt.close()
                log.info("close: closed")
                centralCallback.removeListener("default")
                this@XYBluetoothGatt.gatt = null
                log.info("close: returning")
                return@asyncBle XYBluetoothResult(true)
            }.await()
        }.await()
    }

    private fun discover() = queueBle(3500) {
        log.info("discover")
        assert(connectionState == ConnectionState.Connected)
        val gatt = this@XYBluetoothGatt.gatt
        if (gatt != null) {
            val discover = XYBluetoothGattDiscover(gatt, centralCallback)
            return@queueBle discover.start().await()
        } else {
            return@queueBle XYBluetoothResult<List<BluetoothGattService>>(XYBluetoothError("Null Gatt"))
        }
    }

    //this can only be called after a successful discover
    protected fun findCharacteristic(service: UUID, characteristic: UUID) = queueBle(1500) {

        log.info("findCharacteristic")
        var error: XYBluetoothError? = null
        var value: BluetoothGattCharacteristic? = null

        val callingGatt = this@XYBluetoothGatt.gatt

        if (callingGatt == null) {
            error = XYBluetoothError("findCharacteristic: No Gatt")
        } else {
            value = suspendCancellableCoroutine { cont ->
                if (callingGatt.services?.size == 0) {
                    error = XYBluetoothError("Services Not Discovered Yet")
                    cont.resume(null)
                } else {
                    log.info("findCharacteristic")
                    GlobalScope.launch {
                        asyncBle {
                            val foundService = callingGatt.getService(service)
                            var foundCharacteristic: BluetoothGattCharacteristic? = null
                            if (foundService == null) {
                                error = XYBluetoothError("start: gatt.getService failed to find")
                                cont.resume(null)
                            } else {
                                log.info("findCharacteristic:service:$foundService")
                                foundCharacteristic = foundService.getCharacteristic(characteristic)
                                log.info("findCharacteristic:characteristic:$foundCharacteristic")
                                cont.resume(foundCharacteristic)
                            }
                            return@asyncBle XYBluetoothResult(foundCharacteristic)
                        }.await()
                    }
                }
            }
        }
        log.info("findCharacteristic: Returning: $value")
        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun writeCharacteristic(characteristicToWrite: BluetoothGattCharacteristic) = queueBle(2000) {
        log.info("writeCharacteristic")
        assert(connectionState == ConnectionState.Connected)
        val gatt = this@XYBluetoothGatt.gatt
        if (gatt != null) {
            val writeCharacteristic = XYBluetoothGattWriteCharacteristic(gatt, centralCallback)
            return@queueBle writeCharacteristic.start(characteristicToWrite).await()
        } else {
            return@queueBle XYBluetoothResult<ByteArray>(XYBluetoothError("Null Gatt"))
        }
    }

    /**
     * Fix for known coroutine bug - throws "already resumed"
     * from Docs: **This is unstable API and it is subject to change.**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/common/kotlinx-coroutines-core-common/src/AbstractContinuation.kt
     */
    /*private fun <T> Continuation<T>.tryResumeSilent(value: T) {
        try {
            resume(value)
        } catch (ex: Exception) {
            // This function throws if the coroutine is cancelled or completed while suspended.
            // It seems that the proper fix for this is to actually cancel it if it is cancelled and actually throw and error
            // if it is resumed twice
            log.error(ex, false)
        }
    }*/

    protected fun setCharacteristicNotify(characteristicToWrite: BluetoothGattCharacteristic, notify: Boolean): XYBluetoothResult<Boolean> {
        log.info("setCharacteristicNotify")
        var error: XYBluetoothError? = null
        var value: Boolean? = null

        val gatt = this@XYBluetoothGatt.gatt

        if (gatt == null) {
            error = XYBluetoothError("setCharacteristicNotify: No Gatt")
        } else {
            value = gatt.setCharacteristicNotification(characteristicToWrite, notify)
        }

        return XYBluetoothResult(value, error)
    }

    protected fun writeDescriptor(descriptorToWrite: BluetoothGattDescriptor) = queueBle(1100) {
        log.info("writeDescriptor")
        var error: XYBluetoothError? = null
        var value: ByteArray? = null

        val callingGatt = this@XYBluetoothGatt.gatt

        if (callingGatt == null) {
            error = XYBluetoothError("writeDescriptor: No Gatt")
        } else {
            val listenerName = "writeDescriptor$nowNano"
            value = suspendCancellableCoroutine { cont ->
                var resumed = false
                val listener = object : BluetoothGattCallback() {
                    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                        log.info("onDescriptorWrite: $status")
                        super.onDescriptorWrite(gatt, descriptor, status)
                        if (!resumed && gatt == callingGatt) {
                            //since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (descriptorToWrite == descriptor) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    centralCallback.removeListener(listenerName)
                                    resumed = true
                                    cont.resume(descriptorToWrite.value)
                                } else {
                                    error = XYBluetoothError("writeDescriptor: onDescriptorWrite failed: $status")
                                    centralCallback.removeListener(listenerName)
                                    resumed = true
                                    cont.resume(null)
                                }
                            }
                        }
                    }

                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        log.info("onConnectionStateChange")
                        super.onConnectionStateChange(gatt, status, newState)
                        if (!resumed && gatt == callingGatt) {
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("writeDescriptor: connection dropped")
                                centralCallback.removeListener(listenerName)
                                resumed = true
                                cont.resume(null)
                            }
                        }
                    }
                }
                centralCallback.addListener(listenerName, listener)
                if (!callingGatt.writeDescriptor(descriptorToWrite)) {
                    error = XYBluetoothError("writeDescriptor: gatt.writeDescriptor failed to start")
                    centralCallback.removeListener(listenerName)
                    resumed = true
                    cont.resume(null)
                } else if (connectionState != ConnectionState.Connected) {
                    error = XYBluetoothError("writeDescriptor: connection dropped 2")
                    centralCallback.removeListener(listenerName)
                    resumed = true
                    cont.resume(null)
                }
            }
        }

        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun readCharacteristic(characteristicToRead: BluetoothGattCharacteristic) = queueBle(1200) {
        log.info("readCharacteristic")
        var error: XYBluetoothError? = null
        var value: BluetoothGattCharacteristic? = null

        val callingGatt = this@XYBluetoothGatt.gatt

        if (callingGatt == null) {
            error = XYBluetoothError("readCharacteristic: No Gatt")
        } else {
            val listenerName = "readCharacteristic$nowNano"
            value = suspendCancellableCoroutine { cont ->
                var resumed = false
                val listener = object : BluetoothGattCallback() {

                    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                        super.onCharacteristicRead(gatt, characteristic, status)
                        if (!resumed && gatt == callingGatt) {
                            //since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (characteristicToRead == characteristic) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    centralCallback.removeListener(listenerName)
                                    resumed = true
                                    cont.resume(characteristic)
                                } else {
                                    error = XYBluetoothError("readCharacteristic: onCharacteristicRead failed: $status")
                                    centralCallback.removeListener(listenerName)
                                    resumed = true
                                    cont.resume(null)
                                }
                            }
                        }
                    }

                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        if (!resumed && coroutineContext.isActive && callingGatt == gatt) {
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("readCharacteristic: connection dropped")
                                centralCallback.removeListener(listenerName)
                                resumed = true
                                cont.resume(null)
                            }
                        }
                    }
                }
                centralCallback.addListener(listenerName, listener)
                if (!callingGatt.readCharacteristic(characteristicToRead)) {
                    error = XYBluetoothError("readCharacteristic: gatt.readCharacteristic failed to start")
                    centralCallback.removeListener(listenerName)
                    resumed = true
                    cont.resume(null)
                }
                if (connectionState != ConnectionState.Connected) {
                    error = XYBluetoothError("readCharacteristic: connection dropped 2")
                    centralCallback.removeListener(listenerName)
                    resumed = true
                    cont.resume(null)
                }
            }
        }

        return@queueBle XYBluetoothResult(value, error)
    }


    //make a safe session to interact with the device
    //if null is passed back, the sdk was unable to create the safe session
    fun <T> connectionWithResult(closure: suspend () -> XYBluetoothResult<T>) = GlobalScope.async {
        log.info("connection")
        var value: T? = null
        var error: XYBluetoothError?
        references++

        if (connect().await().error == null) {
            val discovered = discover().await()
            error = discovered.error
            if (error == null) {
                val result = closure()
                error = result.error
                value = result.value
            }
        } else {
            error = XYBluetoothError("connection: Failed to Connect")
        }

        references--
        return@async XYBluetoothResult(value, error)
    }

    fun connection(closure: suspend () -> Unit) = GlobalScope.async {
        log.info("connection")
        val value: Unit? = null
        var error: XYBluetoothError?
        references++

        if (connect().await().error == null) {
            val discovered = discover().await()
            error = discovered.error
            if (error == null) {
                closure()
            }
        } else {
            error = XYBluetoothError("connection: Failed to Connect")
        }

        references--
        return@async XYBluetoothResult(value, error)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun connectGatt19(device: BluetoothDevice,
                              autoConnect: Boolean): BluetoothGatt? {
        log.info("connectGatt19")
        return device.connectGatt(context, autoConnect, centralCallback)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun connectGatt23(device: BluetoothDevice,
                              autoConnect: Boolean,
                              transport: Int?): BluetoothGatt? {
        log.info("connectGatt23")
        return if (transport == null) {
            device.connectGatt(context, autoConnect, centralCallback)
        } else {
            device.connectGatt(context, autoConnect, centralCallback, transport)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun connectGatt26(device: BluetoothDevice,
                              autoConnect: Boolean,
                              transport: Int?,
                              phy: Int?,
                              handler: Handler?): BluetoothGatt? {
        log.info("connectGatt26")
        return when {
            transport == null -> device.connectGatt(context, autoConnect, centralCallback)
            phy == null -> device.connectGatt(context, autoConnect, centralCallback, transport)
            handler == null -> device.connectGatt(context, autoConnect, centralCallback, transport, phy)
            else -> device.connectGatt(context, autoConnect, centralCallback, transport, phy, handler)
        }
    }

    private var cleanUpThread: Job? = null

    //the goal is to leave connections hanging for a little bit in the case
    //that they need to be reestablished in short notice
    private fun cleanUpIfNeeded() {
        if (cleanUpThread == null) {
            cleanUpThread = GlobalScope.launch {

                while (!closed) {
                    //log.info("cleanUpIfNeeded: $references")

                    delay(1000) //fake pulse interval

                    //this initiates a fake pulse
                    gatt?.readRemoteRssi()

                    //the goal is to close the connection if the ref count is
                    //down to zero.  We have to check the lastAccess to make sure the delay is after
                    //the last guy, not an earlier one

                    log.info("cleanUpIfNeeded: Checking")

                    if (!getStayConnected() && !closed && references == 0 && (lastAccessTime + CLEANUP_DELAY) < now) {
                        log.info("cleanUpIfNeeded: Cleaning")
                        close().await()
                    } else if (references > 0) {
                        //if there is still a reference count on it, we want a full delay
                        lastAccessTime = now
                    }
                }
                cleanUpThread = null
            }
        }
    }

    companion object: XYBase() {
        //gap after last connection that we wait to close the connection
        private const val CLEANUP_DELAY = 30_000L
    }
}