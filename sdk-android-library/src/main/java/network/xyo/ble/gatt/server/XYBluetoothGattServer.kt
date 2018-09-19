package network.xyo.ble.gatt.server

import android.bluetooth.*
import android.content.Context
import network.xyo.ble.gatt.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothGattServer(context: Context) : XYBluetoothBase(context) {
    protected val listeners = HashMap<String, BluetoothGattServerCallback>()
    private val services = HashMap<UUID, XYBluetoothService>()
    private var androidGattServer: BluetoothGattServer? = null

    fun startServer() : Boolean {
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
        synchronized(this) {
            androidGattServer?.close()
            androidGattServer = null
        }
    }

    protected fun addListener (key: String, listener : BluetoothGattServerCallback) {
        listeners[key] = listener
    }

    protected fun removeListener (key: String) {
        listeners.remove(key)
    }


    fun addService (service : XYBluetoothService) = asyncBle {
        if (androidGattServer == null) {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No Gatt Server"))
        }

        val addCallback = suspendCoroutine<Int> { cont ->
            addListener("addService", object : BluetoothGattServerCallback() {
                override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                    removeListener("addService")
                    cont.resume(status)
                }
            })

            androidGattServer?.addService(service)
        }

        services[service.uuid] = service

        return@asyncBle XYBluetoothResult(addCallback)
    }

    fun removeService (uuid : UUID) {
        if (androidGattServer != null) {
            val service = services[uuid] ?: return
            androidGattServer?.removeService(service)
            services.remove(uuid)
        }
    }

    private fun sendResponse (byteArray : ByteArray?, requestId : Int, device: BluetoothDevice?) {
        if (androidGattServer != null) {
            androidGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArray)
        }
    }

    fun waitForWrite (characteristic: BluetoothGattCharacteristic, deviceFilter: BluetoothDevice?) = asyncBle {
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

    fun waitForRead (characteristic: BluetoothGattCharacteristic, value : ByteArray, deviceFilter: BluetoothDevice?) = asyncBle {
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

    fun sendNotifaction (characteristic : BluetoothGattCharacteristic, confirm : Boolean, deviceToSend: BluetoothDevice?) = asyncBle {
        if (androidGattServer != null) {
            val error = suspendCoroutine<Int> { cont ->
                addListener("sendNotification $characteristic", object : BluetoothGattServerCallback() {
                    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                        if (device?.address == deviceToSend?.address) {
                            cont.resume(status)
                        }
                    }
                })

                androidGattServer?.notifyCharacteristicChanged(deviceToSend, characteristic, confirm)
            }

            return@asyncBle XYBluetoothResult(error)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No gatt server!"))
    }

    fun disconnect (deviceToDisconnectFrom: BluetoothDevice) = asyncBle {
        if (androidGattServer != null) {
            val connectionState = suspendCoroutine<Int> { cont ->
                addListener("disconnect $deviceToDisconnectFrom", object : BluetoothGattServerCallback() {
                    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                        if (deviceToDisconnectFrom.address == device?.address) {
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

    private var primaryCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = service.onBluetoothCharacteristicReadRequest(characteristic, device)
                if (readValue != null) {
                    sendResponse(readValue, requestId, device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            val service = services[characteristic?.service?.uuid]
            if (service != null && characteristic != null && device != null) {
                val readValue = service.onBluetoothChararisticWrite(characteristic, device, value)
                if (readValue == true) {
                    sendResponse(value, requestId, device)
                }
            }

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)

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

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)

            for ((_, listener) in listeners) {
                listener.onMtuChanged(device, mtu)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)

            for ((_, listener) in listeners) {
                listener.onNotificationSent(device, status)
            }
        }

        override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(device, txPhy, rxPhy, status)

            for ((_, listener) in listeners) {
                listener.onPhyRead(device, txPhy, rxPhy, status)
            }
        }

        override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(device, txPhy, rxPhy, status)

            for ((_, listener) in listeners) {
                listener.onPhyUpdate(device, txPhy, rxPhy, status)
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