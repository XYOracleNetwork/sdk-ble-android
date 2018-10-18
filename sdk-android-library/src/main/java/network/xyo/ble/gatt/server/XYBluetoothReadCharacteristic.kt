package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.experimental.async
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothReadCharacteristic (uuid : UUID) : XYBluetoothCharacteristic(uuid, PROPERTY_READ, PERMISSION_READ) {
    private val responders = HashMap<String, XYBluetoothReadCharacteristicResponder>()

    open fun onReadRequest (device: BluetoothDevice?) : ByteArray? {
        for ((_, responder) in responders) {
            val response = responder.onReadRequest(device)

            if (response != null) {
                value = response
                return response
            }
        }
        return null
    }

    fun clearResponders () {
        responders.clear()
    }

    fun waitForReadRequest (whatToRead : ByteArray?, deviceFilter : BluetoothDevice?) = async {
        val readValue = whatToRead ?: value
        value = readValue
        val readRequest = suspendCoroutine<Any?> { cont ->
            val responderKey = "waitForReadRequest $readValue $deviceFilter"
            addResponder(responderKey, object : XYBluetoothReadCharacteristicResponder {
                override fun onReadRequest(device: BluetoothDevice?): ByteArray? {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        removeResponder(responderKey)
                        cont.resume(null)
                        return readValue
                    }
                    return null
                }
            })
        }
    }

    fun addResponder (key : String, responder : XYBluetoothReadCharacteristicResponder) {
        responders[key] = responder
    }

    fun removeResponder (key : String) {
        responders.remove(key)
    }

    interface XYBluetoothReadCharacteristicResponder {
        fun onReadRequest (device : BluetoothDevice?) : ByteArray?
    }
}