package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.ble.gatt.server.responders.XYBluetoothReadResponder
import network.xyo.ble.gatt.server.responders.XYBluetoothWriteResponder
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class XYBluetoothDescriptor(uuid: UUID, permissions: Int) : BluetoothGattDescriptor(uuid, permissions) {
    private val readResponders = HashMap<String, XYBluetoothReadResponder>()
    private val writeResponders = HashMap<String, XYBluetoothWriteResponder>()

    open fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest? {
        for ((_, responder) in readResponders) {
            val response = responder.onReadRequest(device, offset)

            if (response != null) {
                value = response.byteArray
                return response
            }
        }
        return null
    }

    open fun writeChecker(byteArray: ByteArray?): Boolean {
        return true
    }

    open fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean? {
        for ((_, responder) in writeResponders) {
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


    fun waitForWriteRequest(deviceFilter: BluetoothDevice?) = GlobalScope.async {
        return@async suspendCoroutine<ByteArray?> { cont ->
            val responderKey = "waitForWriteRequest $deviceFilter"
            addWriteResponder(responderKey, object : XYBluetoothWriteResponder {
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

    fun waitForReadRequest(whatToRead: ByteArray?, deviceFilter: BluetoothDevice?) = GlobalScope.async {
        val readValue = whatToRead ?: value
        value = readValue
        return@async suspendCoroutine<Any?> { cont ->
            val responderKey = "waitForReadRequest $readValue $deviceFilter"
            addReadResponder(responderKey, object : XYBluetoothReadResponder {
                override fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest? {
                    if (device?.address == deviceFilter?.address || deviceFilter == null) {
                        removeReadResponder(responderKey)
                        cont.resume(null)
                        return XYBluetoothGattServer.XYReadRequest(readValue, 0)
                    }
                    return null
                }
            })
        }
    }

    fun addWriteResponder(key: String, responder: XYBluetoothWriteResponder) {
        writeResponders[key] = responder
    }

    fun removeResponder(key: String) {
        writeResponders.remove(key)
    }

    fun clearWriteResponders() {
        writeResponders.clear()
    }

    fun clearReadResponders() {
        readResponders.clear()
    }

    fun addReadResponder(key: String, responder: XYBluetoothReadResponder) {
        readResponders[key] = responder
    }

    fun removeReadResponder(key: String) {
        readResponders.remove(key)
    }
}