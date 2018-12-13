package network.xyo.ble.gatt.server.responders

import android.bluetooth.BluetoothDevice
import network.xyo.ble.gatt.server.XYBluetoothCharacteristic
import network.xyo.ble.gatt.server.XYBluetoothGattServer
import java.nio.ByteBuffer
import kotlin.coroutines.resume

class XYStaticReadResponder (var value : ByteArray, val listener : XYStaticReadResponderListener?) : XYBluetoothCharacteristic.XYBluetoothReadCharacteristicResponder {
    private var lastTime = 0

    constructor(string: String, listener: XYStaticReadResponderListener?) : this(string.toByteArray(), listener)
    constructor(int: Int, listener: XYStaticReadResponderListener?) : this(ByteBuffer.allocate(4).putInt(int).array(), listener)
    constructor(byte : Byte, listener: XYStaticReadResponderListener?) : this(byteArrayOf(byte), listener)

    override fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest? {
        val size = value.size - offset
        val response = ByteArray(size)

        for (i in offset until value.size) {
            response[i - offset] = value[i]
        }

        if ((offset - lastTime) + offset > value.size) {
            listener?.onReadComplete()
        }

        lastTime = offset

        return XYBluetoothGattServer.XYReadRequest(response, Math.min(offset, value.size))
    }

    interface XYStaticReadResponderListener {
        fun onReadComplete ()
    }
}