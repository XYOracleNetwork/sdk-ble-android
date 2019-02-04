package network.xyo.ble.gatt.peripheral

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import kotlinx.coroutines.*
import network.xyo.ble.gatt.XYBluetoothBase
import network.xyo.ble.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.gatt.peripheral.actions.XYBluetoothGattReadCharacteristic
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
) : XYBluetoothBase(context) {

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
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                close()
            }
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
            if (value <= 0) {
                _references = 0
                GlobalScope.launch {
                    disconnect().await()
                }
            } else {
                _references = value
            }
            log.info("References Set: $_references")
        }

    //last time this device was accessed (connected to)
    protected var lastAccessTime = 0L

    //last time we heard a ad from this device
    protected var lastAdTime = 0L

    var rssi: Int? = null

    protected var connection: XYBluetoothGattConnect? = null

    val _timeout = 15000L

    //force ble functions for this gatt to run in order
    fun <T> queueBle(
            timeout: Long = _timeout,
            action: String = "Unknown",
            context: CoroutineContext = bluetoothQueue,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            block: suspend CoroutineScope.() -> XYBluetoothResult<T>
    ) = GlobalScope.async(context, start) {
        log.info("queueBle: $action: started")
        return@async runBlocking {
            lastAccessTime = now
            log.info("queueBle: $action: runBlocking")
            try {
                return@runBlocking withTimeout(timeout) {
                    lastAccessTime = now
                    log.info("queueBle: $action: withTimeout")
                    val r = block()
                    log.info("queueBle: $action: withTimeout(After Block)")
                    return@withTimeout r
                }
            } catch (ex: TimeoutCancellationException) {
                log.error("queueBle: $action: Timeout")
                log.error(ex)
                close()
                return@runBlocking XYBluetoothResult<T>(XYBluetoothError(ex.message
                        ?: "Exception"))
            }
        }
    }

    protected var _stayConnected = false

    fun getStayConnected() : Boolean {
        return _stayConnected
    }

    fun setStayConnected(value: Boolean) {
        synchronized(_stayConnected) {
            if (value != _stayConnected) {
                _stayConnected = value
                if (!_stayConnected) {
                    references--
                } else {
                    references++
                }
            }
        }
    }

    val closed: Boolean
        get() = (connection == null)

    internal open fun onDetect(scanResult: XYScanResult?) {

    }

    internal open fun onConnectionStateChange(newState: Int) {

    }

    fun requestMtu (mtu : Int) : Deferred<XYBluetoothResult<Int>> = GlobalScope.async {
        return@async suspendCancellableCoroutine<XYBluetoothResult<Int>> { cont ->
            val key = "$mtu requestMtu $nowNano"

            centralCallback.addListener(key, object : BluetoothGattCallback() {
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)

                    centralCallback.removeListener(key)

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        cont.resume(XYBluetoothResult(mtu, null))
                        return
                    }

                    cont.resume(XYBluetoothResult(mtu, XYBluetoothError(status.toString())))
                }
            })

            GlobalScope.launch {
                if (connection?.gatt?.requestMtu(mtu)?.await() != true) {
                    cont.resume(XYBluetoothResult(mtu, XYBluetoothError("Request Failed")))
                }
            }
        }
    }

    fun waitForNotification (characteristicToWaitFor: UUID): Deferred<XYBluetoothResult<Any?>> = GlobalScope.async {
        log.info("waitForNotification")
        return@async suspendCancellableCoroutine<XYBluetoothResult<Any?>> { cont ->
            val listenerName = "waitForNotification$nowNano"
            val listener = object : BluetoothGattCallback() {
                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    super.onCharacteristicChanged(gatt, characteristic)
                    if (characteristicToWaitFor == characteristic?.uuid) {
                        centralCallback.removeListener(listenerName)
                        cont.resume<XYBluetoothResult<Any?>>(XYBluetoothResult(null, null))
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (newState != BluetoothGatt.STATE_CONNECTED) {
                        centralCallback.removeListener(listenerName)
                        cont.resume<XYBluetoothResult<Any?>>(XYBluetoothResult(null, XYBluetoothError("Device disconnected!")))
                    }
                }
            }

            centralCallback.addListener(listenerName, listener)
        }
    }

    private fun refreshGatt() = GlobalScope.async {
        log.info("refreshGatt")
        var result = false
        var error: XYBluetoothError? = null

        val gatt = connection?.gatt
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
        return@async XYBluetoothResult(result, error)
    }

    fun connect(timeout: Long = 5000) = queueBle(timeout, "connect") {
        log.info("connect: start")
        val device = this@XYBluetoothGatt.device ?: return@queueBle XYBluetoothResult(false, XYBluetoothError("No Device"))
        var connection = this@XYBluetoothGatt.connection //make it thread safe by putting it on the stack
        lastAccessTime = now
        if (connection == null) {
            connection = XYBluetoothGattConnect(device)
        }
        val connectionResult = connection.start(context).await()
        if (connectionResult.error != null) {
            close()
            return@queueBle XYBluetoothResult(false, connectionResult.error)
        }
        connection.callback.addListener("XYBluetoothGatt", centralCallback)
        this@XYBluetoothGatt.connection = connection
        return@queueBle XYBluetoothResult(true)
    }

    fun disconnect(timeout: Long = 1300) = queueBle(timeout, "disconnect") {
        log.info("disconnect:start")

        var error: XYBluetoothError? = null

        val callingGatt = connection?.gatt
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
                            error = XYBluetoothError("asyncDisconnect: connection failed(state): $status : $newState")
                            cont.resume(false)
                        }
                    }
                }
            }
            centralCallback.addListener(listenerName, listener)

            when (connection?.state) {
                BluetoothGatt.STATE_DISCONNECTED -> {
                    log.info("disconnect:already disconnected (upfront)")
                    centralCallback.removeListener(listenerName)
                    connection?.callback?.removeListener("XYBluetoothGatt")
                    connection = null
                    cont.resume(true)
                }
                BluetoothGatt.STATE_DISCONNECTING -> log.info("disconnect:disconnecting (upfront)") //dont call connect since already in progress
                else -> {
                    GlobalScope.launch {
                        callingGatt.disconnect().await()
                    }
                }
            }
        }

        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun close() {
        GlobalScope.launch {
            closeAsync().await()
        }
    }

    private fun closeAsync(timeout: Long = 2900) = queueBle(timeout, "close") {
        log.info("close:enter($connection?.gatt)")
        val gatt = connection?.gatt
        if (gatt == null) {
            log.info("close:already closed")
            return@queueBle XYBluetoothResult(true)
        } else {
            /*if (connectionState != ConnectionState.Disconnected) {
                log.info("close:disconnecting")
                disconnect().await()
            }*/
            log.info("close:closing")
            centralCallback.removeListener("XYBluetoothGatt")
            connection?.close()
            connection = null
            gatt.close().await()
            log.info("close: closed")
            return@queueBle XYBluetoothResult(true)
        }
    }

    //this can only be called after a successful discover
    protected fun findCharacteristic(service: UUID, characteristic: UUID, timeout:Long = 1500) = queueBle(timeout, "findCharacteristic") {

        log.info("findCharacteristic")
        var error: XYBluetoothError? = null
        var value: BluetoothGattCharacteristic? = null

        val callingGatt = connection?.gatt

        if (callingGatt == null) {
            error = XYBluetoothError("findCharacteristic: No Gatt")
        } else {
            val services = connection?.services
            if (services?.isEmpty() == false) {
                value = suspendCancellableCoroutine { cont ->
                    log.info("findCharacteristic")
                    GlobalScope.launch {
                        val foundService = callingGatt.getService(service)
                        if (foundService == null) {
                            error = XYBluetoothError("start: gatt.getService failed to find [$service]")
                            cont.resume(null)
                        } else {
                            log.info("findCharacteristic:service:$foundService")
                            val foundCharacteristic = foundService.getCharacteristic(characteristic)
                            log.info("findCharacteristic:characteristic:$foundCharacteristic")
                            cont.resume(foundCharacteristic)
                        }
                    }
                }
            } else {
                error = XYBluetoothError("Services Not Discovered Yet")
            }
        }
        log.info("findCharacteristic: Returning: $value")
        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun readCharacteristic(characteristicToRead: BluetoothGattCharacteristic, timeout: Long = 2000) = queueBle(timeout, "readCharacteristic") {
        log.info("readCharacteristic")
        assert(connection?.state == BluetoothGatt.STATE_CONNECTED)
        val gatt = connection?.gatt
        if (gatt != null) {
            val readCharacteristic = XYBluetoothGattReadCharacteristic(gatt, centralCallback)
            return@queueBle readCharacteristic.start(characteristicToRead).await()
        } else {
            return@queueBle XYBluetoothResult<BluetoothGattCharacteristic>(XYBluetoothError("Null Gatt"))
        }
    }

    protected fun writeCharacteristic(characteristicToWrite: BluetoothGattCharacteristic, timeout: Long = 2000) = queueBle(timeout, "writeCharacteristic") {
        log.info("writeCharacteristic")
        assert(connection?.state == BluetoothGatt.STATE_CONNECTED)
        val gatt = connection?.gatt
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

    protected fun setCharacteristicNotify(characteristicToWrite: BluetoothGattCharacteristic, notify: Boolean, timeout: Long = 1400) = queueBle(timeout, "setCharacteristicNotify") {
        log.info("setCharacteristicNotify")
        var error: XYBluetoothError? = null
        var value: Boolean? = null

        val gatt = connection?.gatt

        if (gatt == null) {
            error = XYBluetoothError("setCharacteristicNotify: No Gatt")
        } else {
            value = gatt.setCharacteristicNotification(characteristicToWrite, notify).await()
        }

        return@queueBle XYBluetoothResult(value, error)
    }

    protected fun writeDescriptor(descriptorToWrite: BluetoothGattDescriptor, timeout: Long = 1100) = queueBle(timeout, "writeDescriptor") {
        log.info("writeDescriptor")
        var error: XYBluetoothError? = null
        var value: ByteArray? = null

        val callingGatt = connection?.gatt

        if (callingGatt == null) {
            error = XYBluetoothError("writeDescriptor: No Gatt")
        } else {
            val listenerName = "writeDescriptor$nowNano"
            value = suspendCancellableCoroutine { cont ->
                val listener = object : BluetoothGattCallback() {
                    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                        log.info("onDescriptorWrite: $status")
                        super.onDescriptorWrite(gatt, descriptor, status)
                        //since it is always possible to have a rogue callback, make sure it is the one we are looking for
                        if (descriptorToWrite == descriptor) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                centralCallback.removeListener(listenerName)
                                cont.resume(descriptorToWrite.value)
                            } else {
                                error = XYBluetoothError("writeDescriptor: onDescriptorWrite failed: $status")
                                centralCallback.removeListener(listenerName)
                                cont.resume(null)
                            }
                        }
                    }

                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        log.info("onConnectionStateChange")
                        super.onConnectionStateChange(gatt, status, newState)
                        if (newState != BluetoothGatt.STATE_CONNECTED) {
                            error = XYBluetoothError("writeDescriptor: connection dropped")
                            centralCallback.removeListener(listenerName)
                            cont.resume(null)
                        }
                    }
                }
                centralCallback.addListener(listenerName, listener)
                GlobalScope.launch {
                    if (callingGatt.writeDescriptor(descriptorToWrite).await() != true) {
                        error = XYBluetoothError("writeDescriptor: gatt.writeDescriptor failed to start")
                        centralCallback.removeListener(listenerName)
                        cont.resume(null)
                    } else if (connection?.state != BluetoothGatt.STATE_CONNECTED) {
                        error = XYBluetoothError("writeDescriptor: connection dropped 2")
                        centralCallback.removeListener(listenerName)
                        cont.resume(null)
                    }
                }
            }
        }

        return@queueBle XYBluetoothResult(value, error)
    }

    //make a safe session to interact with the device
    //if null is passed back, the sdk was unable to create the safe session
    fun <T> connectionWithResult(closure: suspend () -> XYBluetoothResult<T>) = GlobalScope.async {
        log.info("connectionWithResult: start")
        var value: T? = null
        var error: XYBluetoothError?
        references++

        try {
            log.info("connectionWithResult: try")
            if (connect().await().error == null) {
                log.info("connectionWithResult: connected")
                val result = closure()
                error = result.error
                value = result.value
            } else {
                error = XYBluetoothError("connectionWithResult: Failed to Connect")
            }
        } finally {
            log.info("connectionWithResult: finally")
            references--
        }

        return@async XYBluetoothResult(value, error)
    }

    fun connection(timeout: Long = 12000, closure: suspend () -> Unit) = GlobalScope.async {
        log.info("connection")
        val value: Unit? = null
        var error: XYBluetoothError? = null
        references++

        try {

            if (connect(timeout).await().error == null) {
                closure()
            } else {
                error = XYBluetoothError("connection: Failed to Connect")
            }
        } finally {
            references--
        }

        return@async XYBluetoothResult(value, error)
    }

    //private var cleanUpThread: Job? = null

    //the goal is to leave connections hanging for a little bit in the case
    //that they need to be reestablished in short notice
    /*private fun cleanUpIfNeeded() {
        if (cleanUpThread == null) {
            cleanUpThread = GlobalScope.launch {

                while (!closed) {
                    //log.info("cleanUpIfNeeded: $references")

                    delay(1000) //fake pulse interval

                    //this initiates a fake pulse (this seems to blow up Android 7)
                    //connection?.gatt?.readRemoteRssi()

                    //the goal is to close the connection if the ref count is
                    //down to zero.  We have to check the lastAccess to make sure the delay is after
                    //the last guy, not an earlier one

                    log.info("cleanUpIfNeeded: Checking")

                    if (!getStayConnected() && !closed && references == 0 && (lastAccessTime + CLEANUP_DELAY) < now) {
                        log.info("cleanUpIfNeeded: Cleaning")
                        cleanUpThread = null
                        close()
                    } else if (references > 0) {
                        //if there is still a reference count on it, we want a full delay
                        lastAccessTime = now
                    }
                }
            }
        }
    }*/

    companion object: XYBase() {
        //gap after last connection that we wait to close the connection
        private const val CLEANUP_DELAY = 5000L
    }
}