package network.xyo.ble.gatt

import android.bluetooth.*
import android.content.Context
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothGattServer(context: Context) : XYBluetoothBase(context) {
    protected val listeners = HashMap<String, BluetoothGattServerCallback>()
    private val services = HashMap<String, XYBluetoothService>()
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


    private fun addService (key : String, service : XYBluetoothService) = asyncBle {
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

        services[key] = service

        return@asyncBle XYBluetoothResult(addCallback)
    }

    private fun removeService (key : String) {
        if (gattServer != null) {
            val service = services[key] ?: return
            gattServer?.removeService(service)
            services.remove(key)
        }
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