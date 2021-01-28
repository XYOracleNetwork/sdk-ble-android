package network.xyo.ble.generic.gatt.server.responders

import android.bluetooth.BluetoothDevice
import java.nio.ByteBuffer
import kotlin.math.min
import network.xyo.ble.generic.gatt.server.XYBluetoothGattServer

@Suppress("unused")
class XYStaticReadResponder(var value: ByteArray, val listener: XYStaticReadResponderListener?) : XYBluetoothReadResponder {
    private var lastTime = 0

    constructor(string: String, listener: XYStaticReadResponderListener?) : this(string.toByteArray(), listener)
    constructor(int: Int, listener: XYStaticReadResponderListener?) : this(ByteBuffer.allocate(4).putInt(int).array(), listener)
    constructor(byte: Byte, listener: XYStaticReadResponderListener?) : this(byteArrayOf(byte), listener)

    override fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest {
        // todo connection MTU
        val size = min((value.size - offset), 26)
        val response = ByteArray(size)

        for (i in offset until size) {
            response[i - offset] = value[i]
        }

        if ((offset - lastTime) + offset > value.size) {
            listener?.onReadComplete()
        }

        lastTime = offset

        return XYBluetoothGattServer.XYReadRequest(response, min(offset, value.size))
    }

    interface XYStaticReadResponderListener {
        fun onReadComplete()
    }
}
