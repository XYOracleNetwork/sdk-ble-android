package network.xyo.ble.gatt

import android.bluetooth.*
import android.content.Context
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothGattServer(context: Context) : XYBluetoothBase(context) {
    protected val listeners = HashMap<String, BluetoothGattServerCallback>()
    private val services = HashMap<UUID, XYBluetoothService>()
    private var gattServer: BluetoothGattServer? = null

    fun startServer() : Boolean {
        var result = false
        synchronized(this) {
            if (gattServer == null) {
                gattServer = bluetoothManager?.openGattServer(context, primaryCallback)
                if (gattServer != null) {
                    result = true
                }
            }
        }
        return result
    }

    fun stopServer() {
        synchronized(this) {
            gattServer?.close()
            gattServer = null
        }
    }

    protected fun addListener (key: String, listener : BluetoothGattServerCallback) {
        listeners[key] = listener
    }

    protected fun removeListener (key: String) {
        listeners.remove(key)
    }


    private fun addService (service : XYBluetoothService) = asyncBle {
        if (gattServer == null) {
            return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No Gatt Server"))
        }

        val addCallback = suspendCoroutine<Int> { cont ->
             addListener("addService", object : BluetoothGattServerCallback() {
                 override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                     removeListener("addService")
                     cont.resume(status)
                 }
             })
        }

        services[service.uuid] = service

        return@asyncBle XYBluetoothResult(addCallback)
    }

    private fun removeService (uuid : UUID) {
        if (gattServer != null) {
            val service = services[uuid] ?: return
            gattServer?.removeService(service)
            services.remove(uuid)
        }
    }

    private fun sendResponse (byteArray : ByteArray?, requestId : Int, device: BluetoothDevice?) {
        if (gattServer != null) {
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArray)
        }
    }

    fun waitForWrite (characteristic: BluetoothGattCharacteristic, deviceFilter : BluetoothDevice?) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForWrite $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                    if ((device?.address == deviceFilter?.address) || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponse(value, requestId, device)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    fun waitForRead (characteristic: BluetoothGattCharacteristic, deviceFilter : BluetoothDevice?, value : ByteArray) = asyncBle {
        val response = suspendCoroutine<ByteArray?> { cont ->
            addListener("waitForRead $characteristic", object : BluetoothGattServerCallback() {
                override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
                    if ((device?.address == deviceFilter?.address) || deviceFilter == null) {
                        characteristic?.value = value
                        sendResponse(value, requestId, device)
                        cont.resume(value)
                    }
                }
            })
        }

        return@asyncBle XYBluetoothResult(response)
    }

    fun sendNotifaction (deviceToNotify : BluetoothDevice, characteristic : BluetoothGattCharacteristic, confirm : Boolean) = asyncBle {
        if (gattServer != null) {
            val error = suspendCoroutine<Int> { cont ->
                addListener("sendNotification", object : BluetoothGattServerCallback() {
                    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                        if (device?.address == deviceToNotify.address) {
                            cont.resume(status)
                        }
                    }
                })

                gattServer?.notifyCharacteristicChanged(deviceToNotify, characteristic, confirm)
            }

            return@asyncBle XYBluetoothResult(error)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No gatt server!"))
    }

    fun disconnect (deviceToDisconnectFrom: BluetoothDevice) = asyncBle {
        if (gattServer != null) {
            val connectionState = suspendCoroutine<Int> { cont ->
                addListener("disconnect", object : BluetoothGattServerCallback() {
                    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                        if (deviceToDisconnectFrom.address == device?.address) {
                            cont.resume(newState)
                        }
                    }
                })

                gattServer?.cancelConnection(deviceToDisconnectFrom)
            }

            return@asyncBle XYBluetoothResult(connectionState)
        }
        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No gatt server!"))
    }

    private var primaryCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            for ((_, listener) in listeners) {
                listener.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

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