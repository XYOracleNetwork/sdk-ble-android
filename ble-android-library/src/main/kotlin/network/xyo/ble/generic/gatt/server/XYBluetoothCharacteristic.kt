package network.xyo.ble.generic.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.base.XYLogging
import network.xyo.ble.generic.gatt.peripheral.ble
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothReadResponder
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothWriteResponder

@Suppress("unused")
open class XYBluetoothCharacteristic(uuid: UUID, properties: Int, permissions: Int) : BluetoothGattCharacteristic(uuid, properties, permissions) {
    private val listeners = HashMap<String, XYBluetoothCharacteristicListener>()
    private val readResponders = HashMap<String, XYBluetoothReadResponder>()
    private val writeResponders = HashMap<String, XYBluetoothWriteResponder>()

    interface XYBluetoothCharacteristicListener {
        fun onChange()
    }

    override fun setValue(value: ByteArray?): Boolean {
        onChange()
        return super.setValue(value)
    }

    override fun setValue(value: String?): Boolean {
        onChange()
        return super.setValue(value)
    }

    override fun setValue(value: Int, formatType: Int, offset: Int): Boolean {
        onChange()
        return super.setValue(value, formatType, offset)
    }

    override fun setValue(mantissa: Int, exponent: Int, formatType: Int, offset: Int): Boolean {
        onChange()
        return super.setValue(mantissa, exponent, formatType, offset)
    }

    private fun onChange() {
        for ((_, listener) in listeners) {
            listener.onChange()
        }
    }

    fun addListener(key: String, listener: XYBluetoothCharacteristicListener) {
        listeners[key] = listener
    }

    fun removeListener(key: String) {
        listeners.remove(key)
    }

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

    suspend fun waitForWriteRequest(deviceFilter: BluetoothDevice?) = ble.async {
        log.info("Arie: waitForWriteRequest")
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
    }.await()

    suspend fun waitForReadRequest(whatToRead: ByteArray?, deviceFilter: BluetoothDevice?) = ble.async {
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
    }.await()

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

    companion object {
        val log = XYLogging(this)
    }
}
