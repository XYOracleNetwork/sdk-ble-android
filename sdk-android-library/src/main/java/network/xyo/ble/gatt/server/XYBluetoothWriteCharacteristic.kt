package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.experimental.async
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothWriteCharacteristic (uuid : UUID) : XYBluetoothCharacteristic(uuid, PROPERTY_WRITE, PERMISSION_WRITE) {
    private val responders = HashMap<String, XYBluetoothWriteCharacteristicResponder>()

    open fun writeChecker (byteArray: ByteArray?) : Boolean {
        return true
    }

    open fun onWriteRequest (writeRequestValue : ByteArray?, device : BluetoothDevice?) : Boolean? {
        for ((_, responder) in responders) {
            val canWrite = responder.onWriteRequest(writeRequestValue, device)

            if (canWrite != null) {
                if (canWrite == true) {
                    value = writeRequestValue
                    return true
                }
                return false
            }
        }
        return null
    }

    fun waitForWriteRequest (deviceFilter : BluetoothDevice?) = async {
        val writeRequest = suspendCoroutine<Any?> { cont ->
            val responderKey = "waitForWriteRequest $deviceFilter"
            addResponder(responderKey, object : XYBluetoothWriteCharacteristicResponder {
                override fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean? {
                    if (deviceFilter?.address == device?.address || deviceFilter == null) {
                        val canWrite = writeChecker(writeRequestValue)
                        if (canWrite) {
                            removeResponder(responderKey)
                            cont.resume(writeRequestValue)
                        }
                        return canWrite
                    }
                    return null
                }
            })
        }
    }

    fun addResponder(key : String, responder : XYBluetoothWriteCharacteristicResponder) {
        responders[key] = responder
    }

    fun removeResponder(key : String) {
        responders.remove(key)
    }

    interface XYBluetoothWriteCharacteristicResponder {
        fun onWriteRequest(writeRequestValue : ByteArray?, device : BluetoothDevice?) : Boolean?
    }
}