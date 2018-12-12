package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class XYBluetoothReadCharacteristic(uuid: UUID) : XYBluetoothCharacteristic(uuid, PROPERTY_READ, PERMISSION_READ) {
    private val responders = HashMap<String, XYBluetoothReadCharacteristicResponder>()

    open fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest? {
        for ((_, responder) in responders) {
            val response = responder.onReadRequest(device, offset)

            if (response != null) {
                value = response.byteArray
                return response
            }
        }
        return null
    }

    fun clearResponders() {
        responders.clear()
    }

    fun waitForReadRequest(whatToRead: ByteArray?, deviceFilter: BluetoothDevice?) = GlobalScope.async {
        val readValue = whatToRead ?: value
        value = readValue
        val readRequest = suspendCoroutine<Any?> { cont ->
            val responderKey = "waitForReadRequest $readValue $deviceFilter"
            addResponder(responderKey, object : XYBluetoothReadCharacteristicResponder {
                override fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest? {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        removeResponder(responderKey)
                        cont.resume(null)
                        return XYBluetoothGattServer.XYReadRequest(readValue, 0)
                    }
                    return null
                }
            })
        }
    }

    fun addResponder(key: String, responder: XYBluetoothReadCharacteristicResponder) {
        responders[key] = responder
    }

    fun removeResponder(key: String) {
        responders.remove(key)
    }

    interface XYBluetoothReadCharacteristicResponder {
        fun onReadRequest(device: BluetoothDevice?, offset : Int): XYBluetoothGattServer.XYReadRequest?
    }
}