package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.experimental.async
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.experimental.suspendCoroutine

open class XYBluetoothWriteCharacteristic (uuid : UUID) : XYBluetoothCharacteristic(uuid, PROPERTY_WRITE, PERMISSION_WRITE) {
    private val listeners = HashMap<String, XYBluetoothWriteCharacteristicListener>()

    open fun writeChecker (byteArray: ByteArray?) : Boolean {
        return true
    }

    open fun onWriteRequest (writeRequestValue : ByteArray?, device : BluetoothDevice?) : Boolean? {
        for ((_, listener) in listeners) {
            val canWrite = listener.onWriteRequest(writeRequestValue, device)

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
        val listenerKey = "waitForWriteRequest $deviceFilter"
        val writeRequest = suspendCoroutine<Any?> { cont ->
            addListener(listenerKey, object : XYBluetoothWriteCharacteristicListener {
                override fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean? {
                    if (deviceFilter?.address == device?.address || deviceFilter == null) {
                        val canWrite = writeChecker(writeRequestValue)
                        if (canWrite) {
                            cont.resume(writeRequestValue)
                        }
                        return canWrite
                    }
                    return null
                }
            })
        }
    }

    fun addListener(key : String, listener : XYBluetoothWriteCharacteristicListener) {
        listeners[key] = listener
    }

    fun removeListener(key : String) {
        listeners.remove(key)
    }

    interface XYBluetoothWriteCharacteristicListener {
        fun onWriteRequest(writeRequestValue : ByteArray?, device : BluetoothDevice?) : Boolean?
    }
}