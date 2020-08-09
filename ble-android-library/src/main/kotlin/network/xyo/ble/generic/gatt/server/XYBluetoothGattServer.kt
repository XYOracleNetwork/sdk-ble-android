package network.xyo.ble.generic.gatt.server

import android.bluetooth.*
import android.content.Context
import android.os.Build
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import network.xyo.ble.generic.XYBluetoothBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.asyncBle

@Suppress("unused")
open class XYBluetoothGattServer(context: Context) : XYBluetoothBase(context) {
    protected val listeners = ConcurrentHashMap<String, BluetoothGattServerCallback>()
    private val services = HashMap<UUID, XYBluetoothService>()
    private val characteristics = HashMap<UUID, XYBluetoothCharacteristic>()
    private val descriptors = HashMap<UUID, XYBluetoothDescriptor>()
    private var androidGattServer: BluetoothGattServer? = null

    val devices: Array<BluetoothDevice>?
        get() = bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)?.toTypedArray()

    fun startServer(): Boolean {
        var result = false
        val bluetoothManager = this.bluetoothManager
        if (bluetoothManager?.adapter != null) {
            synchronized(this) {
                androidGattServer = bluetoothManager.openGattServer(context, primaryCallback)
                if (androidGattServer != null) {
                    result = true
                }
            }
        }
        return result
    }

    fun stopServer() {
        log.info("stopServer")
        synchronized(this) {
            androidGattServer?.close()
            androidGattServer = null
        }
    }

    fun addListener(key: String, listener: BluetoothGattServerCallback) {
        log.info("addListener")
        synchronized(listeners) {
            listeners[key] = listener
        }
    }

    fun removeListener(key: String) {
        log.info("removeListener")
        synchronized(listeners) {
            listeners.remove(key)
        }
    }

    fun getServices(): Array<BluetoothGattService> {
        log.info("getServices")
        return services.values.toTypedArray()
    }

    open fun isDeviceConnected(bluetoothDevice: BluetoothDevice): Boolean {
        log.info("isDeviceConnected")
        val connectedDevices = devices
        if (connectedDevices != null) {
            for (device in connectedDevices) {
                if (bluetoothDevice.address == device.address) {
                    return true
                }
            }
        }
        return false
    }

    suspend fun addService(serviceToAdd: XYBluetoothService) = asyncBle {
        log.info("addService")
        if (androidGattServer == null) {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothResultErrorCode.NoGatt)
        }

        val addCallback = suspendCoroutine<Int> { cont ->
            addListener("addService", object : BluetoothGattServerCallback() {
                override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                    if (service?.uuid == serviceToAdd.uuid) {
                        removeListener("addService")
                        addServiceChildren(serviceToAdd)
                        cont.resume(status)
                    }
                }
            })

            androidGattServer?.addService(serviceToAdd)
        }

        services[serviceToAdd.uuid] = serviceToAdd
        return@asyncBle XYBluetoothResult(addCallback)
    }

    fun removeService(uuid: UUID) {
        if (androidGattServer != null) {
            val service = services[uuid] ?: return
            androidGattServer?.removeService(service)
            services.remove(uuid)
        }
    }

    private fun addServiceChildren(serviceToAdd: XYBluetoothService) {
        for (characteristic in serviceToAdd.characteristics) {
            if (characteristic is XYBluetoothCharacteristic) {
                characteristics[characteristic.uuid] = characteristic
            }

            for (descriptor in characteristic.descriptors) {
                if (descriptor is XYBluetoothDescriptor) {
                    descriptors[descriptor.uuid] = descriptor
                }
            }
        }
    }

    private fun sendResponseWithSuccess(byteArray: ByteArray?, requestId: Int, device: BluetoothDevice?, offset: Int?) {
        if (androidGattServer != null && device != null) {
            if (isDeviceConnected(device)) {
                androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset ?: 0, byteArray)
            }
        }
    }

    suspend fun waitForWrite(characteristic: BluetoothGattCharacteristic, deviceFilter: BluetoothDevice?) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForWrite $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponseWithSuccess(value, requestId, device, null)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    suspend fun waitForRead(characteristic: BluetoothGattCharacteristic, value: ByteArray, deviceFilter: BluetoothDevice?) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForRead $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponseWithSuccess(value, requestId, device, null)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    suspend fun sendNotification(characteristic: BluetoothGattCharacteristic, confirm: Boolean, deviceToSend: BluetoothDevice?) = asyncBle {
        if (androidGattServer != null) {
            val error = suspendCoroutine<Int> { cont ->
                val key = "sendNotification $characteristic"
                addListener(key, object : BluetoothGattServerCallback() {
                    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                        if (device?.address == deviceToSend?.address) {
                            removeListener(key)
                            cont.resume(status)
                        }
                    }
                })

                androidGattServer?.notifyCharacteristicChanged(deviceToSend, characteristic, confirm)
            }

            return@asyncBle XYBluetoothResult(error)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothResultErrorCode.NoGatt)
    }

    suspend fun disconnect(deviceToDisconnectFrom: BluetoothDevice) = asyncBle {
        if (androidGattServer != null) {
            val key = "disconnect $deviceToDisconnectFrom"
            val connectionState = suspendCoroutine<Int> { cont ->
                addListener(key, object : BluetoothGattServerCallback() {
                    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                        if (deviceToDisconnectFrom.address == device?.address) {
                            removeListener(key)
                            cont.resume(newState)
                        }
                    }
                })

                androidGattServer?.cancelConnection(deviceToDisconnectFrom)
            }

            return@asyncBle XYBluetoothResult(connectionState)
        }
        return@asyncBle XYBluetoothResult<Int>(XYBluetoothResultErrorCode.NoGatt)
    }

    open fun onBluetoothCharacteristicWrite(characteristic: BluetoothGattCharacteristic, device: BluetoothDevice?, value: ByteArray?): Boolean? {
        val characteristicHandler = characteristics[characteristic.uuid]
        if (characteristicHandler is XYBluetoothCharacteristic) {
            return characteristicHandler.onWriteRequest(value, device)
        }
        return null
    }

    open fun onBluetoothCharacteristicReadRequest(characteristic: BluetoothGattCharacteristic, device: BluetoothDevice?, offset: Int): XYReadRequest? {
        val characteristicHandler = characteristics[characteristic.uuid]
        if (characteristicHandler is XYBluetoothCharacteristic) {
            return characteristicHandler.onReadRequest(device, offset)
        }
        return null
    }

    open fun onBluetoothDescriptorWrite(descriptor: BluetoothGattDescriptor, device: BluetoothDevice?, value: ByteArray?): Boolean? {
        val descriptorHandler = descriptors[descriptor.uuid]
        if (descriptorHandler is XYBluetoothDescriptor) {
            return descriptorHandler.onWriteRequest(value, device)
        }
        return null
    }

    open fun onBluetoothDescriptorReadRequest(descriptor: BluetoothGattDescriptor, device: BluetoothDevice?, offset: Int): XYReadRequest? {
        val descriptorHandler = descriptors[descriptor.uuid]
        if (descriptorHandler is XYBluetoothDescriptor) {
            return descriptorHandler.onReadRequest(device, offset)
        }
        return null
    }

    class XYReadRequest(val byteArray: ByteArray, val offset: Int)

    private val primaryCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            log.info("onCharacteristicReadRequest")
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = onBluetoothCharacteristicReadRequest(characteristic, device, offset)
                if (readValue != null) {
                    sendResponseWithSuccess(readValue.byteArray, requestId, device, readValue.offset)
                } else {
                    log.info("Could not find response to send back.")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            log.info("onCharacteristicWriteRequest $device, $characteristic")
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = onBluetoothCharacteristicWrite(characteristic, device, value)
                if (readValue == true) {
                    sendResponseWithSuccess(value, requestId, device, null)
                } else {
                    log.info("Could not find a responder to write to.")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            log.info("onConnectionStateChange")
            super.onConnectionStateChange(device, status, newState)

            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> log.info("Device connected from server: ${device?.address}")
                BluetoothGatt.STATE_DISCONNECTED -> log.info("Device disconnect from server: ${device?.address}")
            }

            for ((_, listener) in listeners) {
                listener.onConnectionStateChange(device, status, newState)
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            log.info("onDescriptorReadRequest")
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)

            val service = services[descriptor?.characteristic?.service?.uuid]
            if (service != null && descriptor != null && device != null) {
                val readValue = onBluetoothDescriptorReadRequest(descriptor, device, offset)
                if (readValue != null) {
                    sendResponseWithSuccess(readValue.byteArray, requestId, device, readValue.offset)
                } else {
                    log.info("Could not find response to send back.")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onDescriptorReadRequest(device, requestId, offset, descriptor)
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            log.info("onDescriptorWriteRequest")
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)

            val service = services[descriptor?.characteristic?.service?.uuid]
            if (service != null && descriptor != null && device != null) {
                val readValue = onBluetoothDescriptorWrite(descriptor, device, value)
                if (readValue == true) {
                    sendResponseWithSuccess(value, requestId, device, null)
                } else {
                    log.info("Could not find a responder to write to. ${descriptor.uuid}")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            }
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            log.info("onExecuteWrite: $execute")
            super.onExecuteWrite(device, requestId, execute)

            sendResponseWithSuccess(byteArrayOf(), requestId, device, null)

            for ((_, listener) in listeners) {
                listener.onExecuteWrite(device, requestId, execute)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            log.info("onNotificationSent")
            super.onNotificationSent(device, status)

            for ((_, listener) in listeners) {
                listener.onNotificationSent(device, status)
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            log.info("onServiceAdded")
            super.onServiceAdded(status, service)

            for ((_, listener) in listeners) {
                listener.onServiceAdded(status, service)
            }
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)

            for ((_, listener) in listeners) {

                if (Build.VERSION.SDK_INT >= 22) {
                    listener.onMtuChanged(device, mtu)
                }
            }
        }
    }
}
