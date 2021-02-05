package network.xyo.ble.generic.gatt.peripheral

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import java.util.UUID
import java.util.concurrent.Executors
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlinx.coroutines.*
import network.xyo.base.hasDebugger
import network.xyo.ble.generic.XYBluetoothBase
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattReadCharacteristic
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteCharacteristic
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattWriteDescriptor
import network.xyo.ble.generic.scanner.XYScanResult

// XYBluetoothGatt is a pure wrapper that does not add any functionality
// other than the ability to call the BluetoothGatt functions using coroutines

@Suppress("unused")
enum class XYBluetoothGattStatus(val status: Short) {
    NoResources(0x80),
    InternalError(0x81),
    WrongState(0x82),
    DBFull(0x83),
    Busy(0x84),
    Error(0x85),
    IllegalParameter(0x87),
    AuthFail(0x89)
}

@Suppress("unused")
open class XYBluetoothGatt protected constructor(
    context: Context,
    protected var device: BluetoothDevice?,
    private var autoConnect: Boolean,
    private val callback: XYBluetoothGattCallback?,
    private val transport: Int?,
    private val phy: Int?,
    private val handler: Handler?
) : XYBluetoothBase(context) {

    open val bluetoothQueue = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    protected var state = BluetoothGatt.STATE_DISCONNECTED

    protected val centralCallback = object : XYBluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            state = newState
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
    protected open var references: Int
        get() {
            log.info("References Get: $_references")
            return _references
        }
        set(value) {
            if (value <= 0) {
                _references = 0
                close()
            } else {
                _references = value
            }
            log.info("References Set: $_references")
        }

    // last time this device was accessed (connected to)
    protected var lastAccessTime: Long? = null

    // last time we heard a ad from this device
    protected var lastAdTime: Long? = null

    // last time we heard a ad from this device
    protected var enterTime: Long? = null

    var rssi: Int? = null

    protected var connection: XYBluetoothGattConnect? = null

    fun services(): List<BluetoothGattService> {
        return connection?.services ?: emptyList()
    }

    open val defaultTimeout = 15000L

    // force ble functions for this gatt to run in order
    @Suppress("BlockingMethodInNonBlockingContext")
    open fun <T> queueBleAsync(
        timeout: Long? = null,
        action: String = "Unknown",
        context: CoroutineContext = bluetoothQueue,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> XYBluetoothResult<T>
    ) = GlobalScope.async(context, start) {
        log.info("queueBle: $action: started")
        val timeoutToUse = timeout ?: defaultTimeout
        return@async runBlocking {
            lastAccessTime = now
            log.info("queueBle: $action: runBlocking")
            try {
                return@runBlocking withTimeout(timeoutToUse) {
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
                return@runBlocking XYBluetoothResult<T>(XYBluetoothResultErrorCode.Timeout)
            }
        }
    }

    private var stayConnectedValue = false

    var stayConnected: Boolean
        get() {
            return stayConnectedValue
        }
        set(value) {
            synchronized(value) {
                if (stayConnectedValue != value) {
                    stayConnectedValue = value
                    if (!stayConnectedValue) {
                        references--
                    } else {
                        references++
                    }
                }
            }
        }

    val closed: Boolean
        get() = (connection == null)

    open fun onDetect(scanResult: XYScanResult?) {
    }

    open fun onConnectionStateChange(newState: Int) {
        state = newState
    }

    suspend fun requestMtu(mtu: Int, timeout: Long = 2900) = queueBleAsync(timeout, "requestMtu") {
        return@queueBleAsync suspendCancellableCoroutine<XYBluetoothResult<Int>> { cont ->
            val key = "$mtu requestMtu $nowNano"

            centralCallback.addListener(key, object : BluetoothGattCallback() {
                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                    super.onMtuChanged(gatt, mtu, status)

                    centralCallback.removeListener(key)

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        cont.resume(XYBluetoothResult(mtu))
                        return
                    }

                    cont.resume(XYBluetoothResult(XYBluetoothResultErrorCode.Unknown))
                }
            })

            GlobalScope.launch {
                if (connection?.gatt?.requestMtu(mtu)?.value != true) {
                    cont.resume(XYBluetoothResult(XYBluetoothResultErrorCode.Unknown))
                }
            }
        }
    }.await()

    suspend fun waitForNotification(characteristicToWaitFor: UUID): XYBluetoothResult<Any?> {
        return suspendCancellableCoroutine { cont ->
            val listenerName = "waitForNotification$nowNano"
            val listener = object : BluetoothGattCallback() {
                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    super.onCharacteristicChanged(gatt, characteristic)
                    if (characteristicToWaitFor == characteristic?.uuid) {
                        centralCallback.removeListener(listenerName)
                        cont.resume(XYBluetoothResult(null, XYBluetoothResultErrorCode.None))
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    state = newState
                    if (newState != BluetoothGatt.STATE_CONNECTED) {
                        centralCallback.removeListener(listenerName)
                        cont.resume(XYBluetoothResult(null, XYBluetoothResultErrorCode.Disconnected))
                    }
                }
            }

            centralCallback.addListener(listenerName, listener)
        }
    }

    private fun refreshGatt(): XYBluetoothResult<Boolean> {
        log.info("refreshGatt")
        var result = false
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None

        val gatt = connection?.gatt
        if (gatt == null) {
            error = XYBluetoothResultErrorCode.NoGatt
        } else {
            try {
                val localMethod = BluetoothGatt::class.java.getMethod("refresh")
                log.info("refreshGatt found method $localMethod")
                result = (localMethod.invoke(gatt) as Boolean)
            } catch (ex: NoSuchMethodException) {
                // null receiver
                error = XYBluetoothResultErrorCode.FailedToRefreshGatt
                log.error("refreshGatt catch $ex", true)
                // method not found
            }
        }
        return XYBluetoothResult(result, error)
    }

    suspend fun connect(timeout: Long = 60000) = queueBleAsync(timeout, "connect") {
        log.info("connect: start")
        val device = this@XYBluetoothGatt.device
                ?: return@queueBleAsync XYBluetoothResult(false, XYBluetoothResultErrorCode.NoDevice)
        var connection = this@XYBluetoothGatt.connection // make it thread safe by putting it on the stack
        lastAccessTime = now
        if (connection == null) {
            connection = XYBluetoothGattConnect(device)
            connection.callback.addListener("Gatt", object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    state = newState
                    this@XYBluetoothGatt.onConnectionStateChange(newState)
                }
            })
        }
        val connectionResult = connection.start(context, transport)
        if (connectionResult.error != XYBluetoothResultErrorCode.None) {
            close()
            return@queueBleAsync XYBluetoothResult(false, connectionResult.error)
        }
        connection.callback.addListener("XYBluetoothGatt", centralCallback)
        this@XYBluetoothGatt.connection = connection
        return@queueBleAsync XYBluetoothResult(true)
    }.await()

    fun disconnect() {
        GlobalScope.launch {
            disconnectAsync()
        }
    }

    suspend fun disconnectAsync(timeout: Long = 2900) = queueBleAsync(timeout, "disconnect") {
        connection?.disconnect()
        return@queueBleAsync XYBluetoothResult(true)
    }

    protected fun close() {
        GlobalScope.launch {
            closeAsync()
        }
    }

    private suspend fun closeAsync(timeout: Long = 2900) = queueBleAsync(timeout, "close") {
        connection?.close()
        connection = null
        return@queueBleAsync XYBluetoothResult(true)
    }

    // this can only be called after a successful discover
    protected suspend fun findCharacteristic(service: UUID, characteristic: UUID, timeout: Long = 1500) = queueBleAsync(timeout, "findCharacteristic") {

        log.info("findCharacteristic")
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: BluetoothGattCharacteristic? = null

        val callingGatt = connection?.gatt

        if (callingGatt == null) {
            error = XYBluetoothResultErrorCode.NoGatt
        } else {
            val services = connection?.services
            if (services?.isEmpty() == false) {
                val foundService = callingGatt.getService(service)
                if (foundService == null) {
                    error = XYBluetoothResultErrorCode.FailedToFindService
                } else {
                    log.info("findCharacteristic:service:$foundService")
                    value = foundService.getCharacteristic(characteristic)
                }
            } else {
                error = XYBluetoothResultErrorCode.ServicesNotDiscovered
            }
        }
        log.info("findCharacteristic: Returning: $value")
        return@queueBleAsync XYBluetoothResult(value, error)
    }.await()

    protected suspend fun readCharacteristic(characteristicToRead: BluetoothGattCharacteristic, timeout: Long = 10000) = queueBleAsync(timeout, "readCharacteristic") {
        log.info("readCharacteristic")
        if(hasDebugger && connection?.state != BluetoothGatt.STATE_CONNECTED)
            throw RuntimeException("cannot read characteristic")
        val gatt = connection?.gatt
        if (gatt != null) {
            val readCharacteristic = XYBluetoothGattReadCharacteristic(gatt, centralCallback)
            return@queueBleAsync readCharacteristic.start(characteristicToRead)
        } else {
            return@queueBleAsync XYBluetoothResult<BluetoothGattCharacteristic?>(XYBluetoothResultErrorCode.NoGatt)
        }
    }.await()

    protected suspend fun writeCharacteristic(
        characteristicToWrite: BluetoothGattCharacteristic,
        timeout: Long = 10000,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) = queueBleAsync(timeout, "writeCharacteristic") {

        log.info("Arie:writeCharacteristic")
        if(hasDebugger && connection?.state != BluetoothGatt.STATE_CONNECTED)
            throw RuntimeException("cannot read characteristic")
        val gatt = connection?.gatt
        if (gatt != null) {
            val writeCharacteristic = XYBluetoothGattWriteCharacteristic(gatt, centralCallback)
            log.info("Arie:writeCharacteristic:Done")
            return@queueBleAsync writeCharacteristic.start(characteristicToWrite, writeType)
        } else {
            log.info("Arie:writeCharacteristic:noGatt")
            return@queueBleAsync XYBluetoothResult<ByteArray?>(XYBluetoothResultErrorCode.NoGatt)
        }
    }.await()

    protected suspend fun setCharacteristicNotify(characteristicToWrite: BluetoothGattCharacteristic, notify: Boolean, timeout: Long = 10000) = queueBleAsync(timeout, "setCharacteristicNotify") {
        log.info("setCharacteristicNotify")
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: Boolean? = null

        val gatt = connection?.gatt

        if (gatt == null) {
            error = XYBluetoothResultErrorCode.NoGatt
        } else {
            value = gatt.setCharacteristicNotification(characteristicToWrite, notify).value
        }

        return@queueBleAsync XYBluetoothResult(value, error)
    }.await()

    protected suspend fun writeDescriptor(descriptorToWrite: BluetoothGattDescriptor, timeout: Long = 1100) = queueBleAsync(timeout, "writeDescriptor") {
        log.info("writeDescriptor")
        if(hasDebugger && connection?.state != BluetoothGatt.STATE_CONNECTED)
            throw RuntimeException("cannot read characteristic")
        val gatt = connection?.gatt
        if (gatt != null) {
            val writeDescriptor = XYBluetoothGattWriteDescriptor(gatt, centralCallback)
            return@queueBleAsync writeDescriptor.start(descriptorToWrite)
        } else {
            return@queueBleAsync XYBluetoothResult<ByteArray?>(XYBluetoothResultErrorCode.NoGatt)
        }
    }.await()

    suspend fun <T> connection(closure: suspend () -> XYBluetoothResult<T>): XYBluetoothResult<T> {
        return connectionAsync(closure).await()
    }

    // make a safe session to interact with the device
    fun <T> connectionAsync(closure: suspend () -> XYBluetoothResult<T>) = GlobalScope.async {
        var value: T? = null
        val error: XYBluetoothResultErrorCode
        references++

        try {
            if (connect().error == XYBluetoothResultErrorCode.None) {
                val result = closure()
                error = result.error
                value = result.value
            } else {
                error = XYBluetoothResultErrorCode.FailedToConnect
            }
        } finally {
            references--
        }

        return@async XYBluetoothResult(value, error)
    }
}
