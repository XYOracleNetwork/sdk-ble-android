package network.xyo.ble.generic.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothReadResponder
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothWriteResponder

@Suppress("unused")
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
}
