package network.xyo.ble.gatt.server

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.ble.gatt.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

open class XYBluetoothGattServer(context: Context) : XYBluetoothBase(context) {
    protected val listeners = ConcurrentHashMap<String, BluetoothGattServerCallback>()
    private val services = HashMap<UUID, XYBluetoothService>()
    private var androidGattServer: BluetoothGattServer? = null

    val devices: Array<BluetoothDevice>?
        get() = bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)?.toTypedArray()

    fun startServer(): Boolean {
        var result = false
        synchronized(this) {
            if (androidGattServer == null) {
                androidGattServer = bluetoothManager?.openGattServer(context, primaryCallback)
                if (androidGattServer != null) {
                    result = true
                }
            }
        }
        return result
    }

    fun stopServer() {
        logInfo("stopServer")
        synchronized(this) {
            androidGattServer?.close()
            androidGattServer = null
        }
    }

    fun addListener(key: String, listener: BluetoothGattServerCallback) {
        logInfo("addListener")
        synchronized(listeners) {
            listeners[key] = listener
        }
    }

    fun removeListener(key: String) {
        logInfo("removeListener")
        synchronized(listeners) {
            listeners.remove(key)
        }
    }

    fun getServices(): Array<BluetoothGattService> {
        logInfo("getServices")
        return services.values.toTypedArray()
    }

    fun isDeviceConnected(bluetoothDevice: BluetoothDevice): Boolean {
        logInfo("isDeviceConnected")
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

    fun addService(serviceToAdd: XYBluetoothService) = asyncBle {
        logInfo("addService")
        if (androidGattServer == null) {
            return@asyncBle XYBluetoothResult<XYGattStatus>(XYBluetoothError("No Gatt Server"))
        }

        val addCallback = suspendCoroutine<Int> { cont ->
            addListener("addService", object : BluetoothGattServerCallback() {
                override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                    if (service?.uuid == serviceToAdd.uuid)
                        removeListener("addService")
                    cont.resume(status)
                }
            })

            androidGattServer?.addService(serviceToAdd)
        }

        services[serviceToAdd.uuid] = serviceToAdd
        serviceToAdd.addListener(this.toString(), serviceChangeListener)

        return@asyncBle XYBluetoothResult(XYGattStatus(addCallback))
    }

    fun removeService(uuid: UUID) {
        if (androidGattServer != null) {
            val service = services[uuid] ?: return
            service.removeListener(this.toString())
            androidGattServer?.removeService(service)
            services.remove(uuid)
        }
    }

    private fun sendResponse(byteArray: ByteArray?, requestId: Int, device: BluetoothDevice?) {
        if (androidGattServer != null) {
            androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArray)
        }
    }

    fun waitForWrite(characteristic: BluetoothGattCharacteristic, deviceFilter: BluetoothDevice?) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForWrite $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponse(value, requestId, device)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    fun waitForRead(characteristic: BluetoothGattCharacteristic, value: ByteArray, deviceFilter: BluetoothDevice?) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForRead $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponse(value, requestId, device)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    fun sendNotification(characteristic: BluetoothGattCharacteristic, confirm: Boolean, deviceToSend: BluetoothDevice?) = asyncBle {
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

                cont.resume(0)

            }

            return@asyncBle XYBluetoothResult(error)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No gatt server!"))
    }

    fun disconnect(deviceToDisconnectFrom: BluetoothDevice) = asyncBle {
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
        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No gatt server!"))
    }

    private val serviceChangeListener = object : XYBluetoothService.XYBluetoothServiceListener {
        override fun onCharacteristicChange(characteristic: BluetoothGattCharacteristic) {
            GlobalScope.async {
                val connectedDevices = androidGattServer?.connectedDevices
                if (connectedDevices != null) {
                    for (connectedDevice in connectedDevices) {
                        sendNotification(characteristic, false, connectedDevice)
                    }
                }
            }
        }
    }

    private val primaryCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            logInfo("onCharacteristicReadRequest")
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = service.onBluetoothCharacteristicReadRequest(characteristic, device)
                if (readValue != null) {
                    sendResponse(readValue, requestId, device)
                } else {
                    logInfo("Could not find response to send back.")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
           logInfo("onCharacteristicWriteRequest")
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = service.onBluetoothCharacteristicWrite(characteristic, device, value)
                if (readValue == true) {
                    sendResponse(value, requestId, device)
                } else {
                    logInfo("Could not find a responder to write to.")
                    androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    androidGattServer?.cancelConnection(device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)

            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> logInfo("Device connected from server: ${device?.address}")
                BluetoothGatt.STATE_DISCONNECTED -> logInfo("Device disconnect from server: ${device?.address}")
            }

            for ((_, listener) in listeners) {
                listener.onConnectionStateChange(device, status, newState)
            }
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)

            for ((_, listener) in listeners) {
                listener.onDescriptorReadRequest(device, requestId, offset, descriptor)
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)

            for ((_, listener) in listeners) {
                listener.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            }
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)

            for ((_, listener) in listeners) {
                listener.onExecuteWrite(device, requestId, execute)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)

            for ((_, listener) in listeners) {
                listener.onNotificationSent(device, status)
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)

            for ((_, listener) in listeners) {
                listener.onServiceAdded(status, service)
            }
        }
    }
}