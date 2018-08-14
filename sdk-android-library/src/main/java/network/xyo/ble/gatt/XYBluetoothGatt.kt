package network.xyo.ble.gatt

import android.annotation.TargetApi
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import network.xyo.ble.CallByVersion
import network.xyo.ble.scanner.XYScanResult
import kotlinx.coroutines.experimental.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

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

    protected var references = 0

    //last time this device was accessed (connected to)
    protected var lastAccessTime = 0L

    //last time we heard a ad from this device
    protected var lastAdTime = 0L

    var rssi: Int? = null

    open class XYBluetoothGattCallback : BluetoothGattCallback() {

    }

    enum class ConnectionState (val state: Int) {
        Unknown(-1),
        Disconnected(BluetoothGatt.STATE_DISCONNECTED),
        Connected(BluetoothGatt.STATE_CONNECTED),
        Connecting(BluetoothGatt.STATE_CONNECTING),
        Disconnecting(BluetoothGatt.STATE_DISCONNECTING)
    }

    var _connectionState : Int? = null
    val connectionState : ConnectionState
        get() {
            return when (_connectionState) {
                BluetoothGatt.STATE_DISCONNECTED -> ConnectionState.Disconnected
                BluetoothGatt.STATE_CONNECTING -> ConnectionState.Connecting
                BluetoothGatt.STATE_CONNECTED -> ConnectionState.Connected
                BluetoothGatt.STATE_DISCONNECTING -> ConnectionState.Disconnecting
                else -> ConnectionState.Unknown
            }
        }

    protected var _stayConnected = false

    var stayConnected : Boolean
        get() {
            return _stayConnected
        }
        set(value) {
            _stayConnected = value
            if (!_stayConnected) {
                cleanUpIfNeeded()
            }
        }

    val closed: Boolean
        get() = (gatt == null)

    private var gatt: BluetoothGatt? = null

    private val gattListeners = HashMap<String, XYBluetoothGattCallback>()

    internal fun addGattListener(key: String, listener: XYBluetoothGattCallback) {
        synchronized(gattListeners) {
            gattListeners[key] = listener
        }
    }

    internal fun removeGattListener(key: String) {
        synchronized(gattListeners) {
            gattListeners.remove(key)
        }
    }

    internal open fun onDetect(scanResult: XYScanResult?) {

    }

    protected fun connectGatt() : Deferred<XYBluetoothResult<Boolean>> {
        return asyncBle {
            logInfo("connectGatt")
            var error: XYBluetoothError? = null
            var value: Boolean? = null

            val device = this@XYBluetoothGatt.device
            if (device == null) {
                error = XYBluetoothError("connectGatt: No Device")
            } else {
                var gatt = this@XYBluetoothGatt.gatt

                if (callback != null) {
                    addGattListener("default", callback)
                }
                if (gatt == null) {
                    CallByVersion()
                            .add(Build.VERSION_CODES.O) {
                                gatt = connectGatt26(device, autoConnect, transport, phy, handler)
                            }
                            .add(Build.VERSION_CODES.M) {
                                gatt = connectGatt23(device, autoConnect, transport)
                            }
                            .add(Build.VERSION_CODES.KITKAT) {
                                gatt = connectGatt19(device, autoConnect)
                            }.call()
                    this@XYBluetoothGatt.gatt = gatt
                    if (gatt == null) {
                        error = XYBluetoothError("connectGatt: Failed to get gatt")
                    }
                    cleanUpIfNeeded()
                } else {
                    value = true
                }
            }
            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun connect() : Deferred<XYBluetoothResult<Boolean>> {
        return asyncBle {
            logInfo("connect")
            var error: XYBluetoothError? = null
            var value: Boolean? = null

            val gatt = this@XYBluetoothGatt.gatt

            if (gatt == null) {
                error = XYBluetoothError("connect: No Gatt")
            } else {
                val listenerName = "connect$nowNano"
                value = suspendCoroutine { cont ->
                    var resumed = false
                    logInfo("connect: suspendCoroutine")
                    val listener = object : XYBluetoothGattCallback() {
                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            super.onConnectionStateChange(gatt, status, newState)
                            when {
                                status == BluetoothGatt.GATT_FAILURE -> {
                                    logInfo("connect:failure: $status : $newState")
                                    error = XYBluetoothError("connect: connection failed(status): $status : $newState")
                                    removeGattListener(listenerName)
                                    resumed = true
                                    cont.resume(null)
                                }
                                newState == BluetoothGatt.STATE_CONNECTED -> {
                                    logInfo("connect:connected")
                                    removeGattListener(listenerName)
                                    resumed = true
                                    cont.resume(true)
                                }
                                newState == BluetoothGatt.STATE_CONNECTING -> logInfo("connect:connecting")
                                //wait some more
                                else -> {
                                    error = XYBluetoothError("connect: connection failed(state): $status : $newState")
                                    removeGattListener(listenerName)
                                    resumed = true
                                    cont.resume(null)
                                }
                            }
                        }
                    }
                    addGattListener(listenerName, listener)

                    if (connectionState == ConnectionState.Connected) {
                        logInfo("asyncConnect:already connected")
                        removeGattListener(listenerName)
                        resumed = true
                        cont.resume(true)
                    } else if (connectionState == ConnectionState.Connecting) {
                        logInfo("connect:connecting")
                        //dont call connect since already in progress
                    } else if (!gatt.connect()) {
                        logInfo("connect: failed to start connect")
                        error = XYBluetoothError("connect: gatt.readCharacteristic failed to start")
                        removeGattListener(listenerName)
                        resumed = true
                        cont.resume(null)
                    } else {
                        lastAccessTime = now
                        launch(CommonPool) {
                            try {
                                withTimeout(15, TimeUnit.SECONDS) {
                                    while (!resumed) {
                                        delay(500)
                                        lastAccessTime = now //prevent cleanup for cleaningup before the timeout
                                        logInfo("connect: waiting...")
                                    }
                                }
                            } catch (ex: TimeoutCancellationException) {
                                if (!resumed) {
                                    logInfo("connect: timeout - cancelling")
                                    removeGattListener(listenerName)
                                    close()
                                    cont.resume(null)
                                }
                            }
                        }
                    }
                }
            }
            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun disconnect() : Deferred<XYBluetoothResult<Boolean>>{
        return asyncBle {
            logInfo("disconnect")
            var error: XYBluetoothError? = null
            var value: Boolean? = null

            val gatt = this@XYBluetoothGatt.gatt

            if (gatt == null) {
                error = XYBluetoothError("asyncDisconnect: No Gatt")
            }

            if (gatt != null) {
                val listenerName = "asyncDisconnect$nowNano"
                value = suspendCoroutine { cont ->
                    val listener = object : XYBluetoothGattCallback() {
                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            super.onConnectionStateChange(gatt, status, newState)
                            when {
                                status == BluetoothGatt.GATT_FAILURE -> {
                                    error = XYBluetoothError("asyncDisconnect: disconnection failed(status): $status : $newState")
                                    removeGattListener(listenerName)
                                    cont.resume(null)
                                }
                                newState == BluetoothGatt.STATE_DISCONNECTED -> {
                                    removeGattListener(listenerName)
                                    cont.resume(true)
                                }
                                newState == BluetoothGatt.STATE_DISCONNECTING -> {
                                    //wait some more
                                }
                                else -> {
                                    //error = XYBluetoothError("asyncDisconnect: connection failed(state): $status : $newState")
                                    //cont.resume(null)
                                }
                            }
                        }
                    }
                    addGattListener(listenerName, listener)

                    when (connectionState) {
                        ConnectionState.Disconnected -> {
                            logInfo("asyncDisconnect:already disconnected")
                            removeGattListener(listenerName)
                            cont.resume(true)
                        }
                        ConnectionState.Disconnecting -> logInfo("asyncDisconnect:disconnecting")
                        //dont call connect since already in progress
                        else -> {
                            logInfo("asyncDisconnect:starting disconnect")
                            gatt.disconnect()
                        }
                    }
                }
            }
            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    protected fun close() : Deferred<XYBluetoothResult<Boolean>> {
        return asyncBle {
            logInfo("close")
            if (connectionState != ConnectionState.Disconnected) {
                disconnect().await()
            }
            gatt?.close()
            logInfo("close: Closed")
            removeGattListener("default")
            gatt = null
            return@asyncBle XYBluetoothResult(true)
        }
    }

    protected fun discover() : Deferred<XYBluetoothResult<List<BluetoothGattService>>> {
        var error: XYBluetoothError? = null
        var value: List<BluetoothGattService>? = null
        return asyncBle {
            val gatt = this@XYBluetoothGatt.gatt
            if (gatt == null) {
                error = XYBluetoothError("Gatt is Null")
            } else if (gatt.services != null && gatt.services.size > 0) {
                value = gatt.services
            } else {
                value = suspendCoroutine { cont ->
                    logInfo("discover")
                    val listenerName = "discover$nowNano"
                    val listener = object : XYBluetoothGattCallback() {
                        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                            super.onServicesDiscovered(gatt, status)
                            if (status != BluetoothGatt.GATT_SUCCESS) {
                                error = XYBluetoothError("discover: discoverStatus: $status")
                                removeGattListener(listenerName)
                                cont.resume(null)
                            } else {
                                if (gatt == null) {
                                    error = XYBluetoothError("discover: gatt: NULL")
                                    removeGattListener(listenerName)
                                    cont.resume(null)
                                } else {
                                    removeGattListener(listenerName)
                                    cont.resume(gatt.services)
                                }
                            }
                        }

                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            super.onConnectionStateChange(gatt, status, newState)
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("asyncDiscover: connection dropped")
                                removeGattListener(listenerName)
                                cont.resume(null)
                            }
                        }
                    }
                    addGattListener(listenerName, listener)
                    if (!gatt.discoverServices()) {
                        error = XYBluetoothError("asyncDiscover: gatt.discoverServices failed to start")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    }
                    if (connectionState != ConnectionState.Connected) {
                        error = XYBluetoothError("discover: connection dropped 2: $connectionState")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    }
                }
            }
            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    //this can only be called after a successful discover
    protected fun findCharacteristic(service: UUID, characteristic: UUID) : Deferred<XYBluetoothResult<BluetoothGattCharacteristic>> {

        return asyncBle {

            logInfo("findCharacteristic")
            var error: XYBluetoothError? = null
            var value: BluetoothGattCharacteristic? = null

            val gatt = this@XYBluetoothGatt.gatt

            if (gatt == null) {
                error = XYBluetoothError("findCharacteristic: No Gatt")
            } else {
                value = suspendCoroutine { cont ->
                    if (gatt.services?.size == 0) {
                        error = XYBluetoothError("Services Not Discovered Yet")
                        cont.resume(null)
                    } else {
                        logInfo("findCharacteristic")
                        val foundService = gatt.getService(service)
                        logInfo("findCharacteristic:service:$foundService")
                        if (foundService != null) {
                            val foundCharacteristic = foundService.getCharacteristic(characteristic)
                            logInfo("findCharacteristic:characteristic:$foundCharacteristic")
                            cont.resume(foundCharacteristic)
                        } else {
                            error = XYBluetoothError("findCharacteristic: Characteristic not Found!")
                            cont.resume(null)
                        }
                    }
                }
            }
            logInfo("findCharacteristic: Returning: $value")
            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    protected fun writeCharacteristic(characteristicToWrite: BluetoothGattCharacteristic) : Deferred<XYBluetoothResult<ByteArray>>{
        return asyncBle {
            logInfo("asyncWriteCharacteristic")
            var error: XYBluetoothError? = null
            var value: ByteArray? = null

            val gatt = this@XYBluetoothGatt.gatt

            if (gatt == null) {
                error = XYBluetoothError("writeCharacteristic: No Gatt")
            } else {
                val listenerName = "writeCharacteristic$nowNano"
                value = suspendCoroutine { cont ->
                    val listener = object : XYBluetoothGattCallback() {
                        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                            logInfo("onCharacteristicWrite: $status")
                            super.onCharacteristicWrite(gatt, characteristic, status)
                            //since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (characteristicToWrite == characteristic) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    removeGattListener(listenerName)
                                    cont.resume(characteristicToWrite.value)
                                } else {
                                    error = XYBluetoothError("writeCharacteristic: onCharacteristicWrite failed: $status")
                                    removeGattListener(listenerName)
                                    cont.resume(null)
                                }
                            }
                        }

                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            logInfo("onCharacteristicWrite")
                            super.onConnectionStateChange(gatt, status, newState)
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("writeCharacteristic: connection dropped")
                                removeGattListener(listenerName)
                                cont.resume(null)
                            }
                        }
                    }
                    addGattListener(listenerName, listener)
                    if (!gatt.writeCharacteristic(characteristicToWrite)) {
                        error = XYBluetoothError("writeCharacteristic: gatt.writeCharacteristic failed to start")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    } else if (connectionState != ConnectionState.Connected) {
                        error = XYBluetoothError("writeCharacteristic: connection dropped 2")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    }
                }
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    protected fun readCharacteristic(characteristicToRead: BluetoothGattCharacteristic) : Deferred<XYBluetoothResult<BluetoothGattCharacteristic>>{
        return asyncBle {
            logInfo("readCharacteristic")
            var error: XYBluetoothError? = null
            var value: BluetoothGattCharacteristic? = null

            val gatt = this@XYBluetoothGatt.gatt

            if (gatt == null) {
                error = XYBluetoothError("readCharacteristic: No Gatt")
            } else {
                val listenerName = "readCharacteristic$nowNano"
                value = suspendCoroutine { cont ->
                    val listener = object : XYBluetoothGattCallback() {
                        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                            super.onCharacteristicRead(gatt, characteristic, status)
                            //since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (characteristicToRead == characteristic) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    removeGattListener(listenerName)
                                    cont.resume(characteristic)
                                } else {
                                    error = XYBluetoothError("readCharacteristic: onCharacteristicRead failed: $status")
                                    removeGattListener(listenerName)
                                    cont.resume(null)
                                }
                            }
                        }

                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            super.onConnectionStateChange(gatt, status, newState)
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("readCharacteristic: connection dropped")
                                removeGattListener(listenerName)
                                cont.resume(null)
                            }
                        }
                    }
                    addGattListener(listenerName, listener)
                    if (!gatt.readCharacteristic(characteristicToRead)) {
                        error = XYBluetoothError("readCharacteristic: gatt.readCharacteristic failed to start")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    }
                    if (connectionState != ConnectionState.Connected) {
                        error = XYBluetoothError("readCharacteristic: connection dropped 2")
                        removeGattListener(listenerName)
                        cont.resume(null)
                    }
                }
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }



    //make a safe session to interact with the device
    //if null is passed back, the sdk was unable to create the safe session
    fun <T> connection(closure: suspend ()-> XYBluetoothResult<T>) : Deferred<XYBluetoothResult<T>> {
        val deferred = asyncBle<T> {
            logInfo("connection")
            var value: T? = null
            var error: XYBluetoothError? = null
            references++

            if (connectGatt().await().error == null) {
                if (connect().await().error == null) {
                    val discovered = discover().await()
                    error = discovered.error
                    if (error == null) {
                        val result = closure()
                        error = result.error
                        value = result.value
                    }
                }
            } else {
                error = XYBluetoothError("connection: Failed to Connect")
            }
            references--
            return@asyncBle XYBluetoothResult(value, error)
        }
        return deferred
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun connectGatt19(device: BluetoothDevice,
                              autoConnect: Boolean) : BluetoothGatt? {
        logInfo("connectGatt19")
        return device.connectGatt(context, autoConnect, centralCallback)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun connectGatt23(device: BluetoothDevice,
                              autoConnect: Boolean,
                              transport: Int?) : BluetoothGatt? {
        logInfo("connectGatt23")
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
                              handler: Handler?) : BluetoothGatt? {
        logInfo("connectGatt26")
        return when {
            transport == null -> device.connectGatt(context, autoConnect, centralCallback)
            phy == null -> device.connectGatt(context, autoConnect, centralCallback, transport)
            handler == null -> device.connectGatt(context, autoConnect, centralCallback, transport, phy)
            else -> device.connectGatt(context, autoConnect, centralCallback, transport, phy, handler)
        }
    }

    private val centralCallback = object : XYBluetoothGattCallback() {
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            logInfo("onCharacteristicChanged: $characteristic")
            synchronized(gattListeners) {
                for((key, listener) in gattListeners) {
                    launch(CommonPool) {
                        logInfo("onCharacteristicChanged: $key")
                        listener.onCharacteristicChanged(gatt, characteristic)
                    }
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            logInfo("onCharacteristicRead: $characteristic : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onCharacteristicRead(gatt, characteristic, status)
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            logInfo("onCharacteristicWrite: $status")
            super.onCharacteristicWrite(gatt, characteristic, status)
            synchronized(gattListeners) {
                logInfo("onCharacteristicWrite3: $status")
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onCharacteristicWrite(gatt, characteristic, status)
                    }
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            logInfo("onConnectionStateChange: ${gatt?.device?.address} $newState : $status")
            synchronized(gattListeners) {
                _connectionState = newState
                for ((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onConnectionStateChange(gatt, status, newState)
                    }
                }
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            logInfo("onDescriptorRead: $descriptor : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onDescriptorRead(gatt, descriptor, status)
                    }
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            logInfo("onDescriptorWrite: $descriptor : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onDescriptorWrite(gatt, descriptor, status)
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            logInfo("onMtuChanged: $mtu : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        @Suppress()
                        listener.onMtuChanged(gatt, mtu, status)
                    }
                }
            }
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            logInfo("onPhyRead: $txPhy : $rxPhy : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        @Suppress()
                        listener.onPhyRead(gatt, txPhy, rxPhy, status)
                    }
                }
            }
        }

        @TargetApi(26)
        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            logInfo("onPhyUpdate: $txPhy : $rxPhy : $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        @Suppress()
                        listener.onPhyUpdate(gatt, txPhy, rxPhy, status)
                    }
                }
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            logInfo("onReadRemoteRssi: $rssi : $status")
            this@XYBluetoothGatt.rssi = rssi
            onDetect(null)
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onReadRemoteRssi(gatt, rssi, status)
                    }
                }
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
            logInfo("onReliableWriteCompleted: $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onReliableWriteCompleted(gatt, status)
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            logInfo("onServicesDiscovered: $status")
            synchronized(gattListeners) {
                for((_, listener) in gattListeners) {
                    launch(CommonPool) {
                        listener.onServicesDiscovered(gatt, status)
                    }
                }
            }
        }
    }

    private var cleanUpThread : Job? = null

    //the goal is to leave connections hanging for a little bit in the case
    //that they need to be reestablished in short notice
    private fun cleanUpIfNeeded() {
        if (cleanUpThread == null) {
            cleanUpThread = launch(CommonPool) {
                logInfo("cleanUpIfNeeded")

                while (!closed) {
                    //if the global and local last connection times do not match
                    //after the delay, that means a newer connection is now responsible for closing it
                    val localAccessTime = now

                    delay(CLEANUP_DELAY)

                    //this initiates a fake pulse
                    gatt?.readRemoteRssi()

                    //the goal is to close the connection if the ref count is
                    //down to zero.  We have to check the lastAccess to make sure the delay is after
                    //the last guy, not an earlier one

                    logInfo("cleanUpIfNeeded: Checking")

                    if (!stayConnected && !closed && references == 0 && lastAccessTime == localAccessTime) {
                        logInfo("cleanUpIfNeeded: Cleaning")
                        close().await()
                    }
                }
                cleanUpThread = null
            }
        }
    }

    companion object {
        //gap after last connection that we wait to close the connection
        private const val CLEANUP_DELAY = 5000
    }
}