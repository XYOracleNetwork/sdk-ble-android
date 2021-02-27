package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*
import network.xyo.ble.generic.XYBluetoothBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import network.xyo.ble.generic.scanner.XYScanResult

// XYBluetoothGatt is a pure wrapper that does not add any functionality
// other than the ability to call the BluetoothGatt functions using coroutines

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
            onConnectionStateChange(newState)
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_DISCONNECTED -> close()
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
        val timeoutToUse = timeout ?: defaultTimeout
        return@async runBlocking {
            lastAccessTime = now
            try {
                return@runBlocking withTimeout(timeoutToUse) {
                    lastAccessTime = now
                    return@withTimeout block()
                }
            } catch (ex: TimeoutCancellationException) {
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
        connection.let { connection ->
            if (connection != null) {
                return@queueBleAsync requestMtuImpl(connection, mtu, centralCallback)
            } else {
                return@queueBleAsync XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }

    suspend fun waitForNotificationAsync(characteristicToWaitFor: UUID): XYBluetoothResult<Any?> {
        return waitForNotificationImpl(characteristicToWaitFor, centralCallback)
    }

    private fun refreshGatt(): XYBluetoothResult<Boolean> {
        val gatt = connection?.gatt
        return if (gatt == null) {
            XYBluetoothResult(false, XYBluetoothResultErrorCode.NoGatt)
        } else {
            refreshGattImpl(gatt)
        }
    }

    suspend fun connect(timeout: Long = 60000) = queueBleAsync(timeout, "connect") {
        log.info("connect: start")
        val device = this@XYBluetoothGatt.device
            ?: return@queueBleAsync XYBluetoothResult(false, XYBluetoothResultErrorCode.NoDevice)
        var connection =
            this@XYBluetoothGatt.connection // make it thread safe by putting it on the stack
        lastAccessTime = now
        if (connection == null) {
            connection = XYBluetoothGattConnect(device)
            connection.callback.addListener("Gatt", object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt?,
                    status: Int,
                    newState: Int
                ) {
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
            disconnectAsync().await()
        }
    }

    suspend fun disconnectAsync(timeout: Long = 2900) = queueBleAsync(timeout, "disconnect") {
        connection?.disconnect()
        return@queueBleAsync XYBluetoothResult(true)
    }

    protected fun close() {
        GlobalScope.launch {
            closeAsync().await()
        }
    }

    private suspend fun closeAsync(timeout: Long = 2900) = queueBleAsync(timeout, "close") {
        connection?.close()
        connection = null
        return@queueBleAsync XYBluetoothResult(true)
    }

    // this can only be called after a successful discover
    protected fun findCharacteristicAsync(
        service: UUID,
        characteristic: UUID,
        timeout: Long = 1500
    ) = queueBleAsync(timeout, "findCharacteristic") {
        connection.let { connection ->
            if (connection != null) {
                return@queueBleAsync findCharacteristicImpl(connection, service, characteristic)
            } else {
                return@queueBleAsync XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }

    protected suspend fun readCharacteristic(
        characteristicToRead: BluetoothGattCharacteristic,
        timeout: Long = 10000
    ) = queueBleAsync(timeout, "readCharacteristic") {
        connection.let { connection ->
            return@queueBleAsync if (connection != null) {
                readCharacteristicImpl(connection, characteristicToRead, centralCallback)
            } else {
                XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }.await()

    protected suspend fun writeCharacteristicAsync(
        characteristicToWrite: BluetoothGattCharacteristic,
        timeout: Long = 10000,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) = queueBleAsync(timeout, "writeCharacteristic") {
        connection.let { connection ->
            return@queueBleAsync if (connection != null) {
                writeCharacteristicImpl(
                    connection,
                    characteristicToWrite,
                    writeType,
                    centralCallback
                )
            } else {
                XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }

    protected suspend fun setCharacteristicNotifyAsync(
        characteristic: BluetoothGattCharacteristic,
        notify: Boolean,
        timeout: Long = 10000
    ) = queueBleAsync(timeout, "setCharacteristicNotify") {
        connection.let { connection ->
            return@queueBleAsync if (connection != null) {
                setCharacteristicNotifyImpl(connection, characteristic, notify)
            } else {
                XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }

    protected suspend fun writeDescriptorAsync(
        descriptor: BluetoothGattDescriptor,
        timeout: Long = 1100
    ) = queueBleAsync(timeout, "writeDescriptor") {
        connection.let { connection ->
            return@queueBleAsync if (connection != null) {
                writeDescriptorImpl(connection, descriptor, centralCallback)
            } else {
                XYBluetoothResult(XYBluetoothResultErrorCode.Disconnected)
            }
        }
    }

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
