package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.experimental.async
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothReadCharacteristic (uuid : UUID) : XYBluetoothCharacteristic(uuid, PROPERTY_READ, PERMISSION_READ) {
    private val listeners = HashMap<String, XYBluetoothReadCharacteristicListener>()

    open fun onReadRequest (device: BluetoothDevice?) : ByteArray? {
        for ((_, listener) in listeners) {
            val response = listener.onReadRequest(device)

            if (response != null) {
                return response
            }
        }
        return null
    }

    fun waitForReadRequest (whatToRead : ByteArray?, deviceFilter : BluetoothDevice?) = async {
        val readValue = whatToRead ?: value
        value = readValue
        val listenerKey = "waitForReadRequest $readValue $deviceFilter"

        val readRequest = suspendCoroutine<Any?> { cont ->
            addListener(listenerKey, object : XYBluetoothReadCharacteristicListener {
                override fun onReadRequest(device: BluetoothDevice?): ByteArray? {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        cont.resume(null)
                        return readValue
                    }
                    return null
                }
            })
        }

        removeListener(listenerKey)
    }

    fun addListener (key : String, listener : XYBluetoothReadCharacteristicListener) {
        listeners[key] = listener
    }

    fun removeListener (key : String) {
        listeners.remove(key)
    }

    interface XYBluetoothReadCharacteristicListener {
        fun onReadRequest (device : BluetoothDevice?) : ByteArray?
    }
}