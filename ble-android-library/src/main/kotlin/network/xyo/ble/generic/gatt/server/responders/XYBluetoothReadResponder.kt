package network.xyo.ble.generic.gatt.server.responders

import android.bluetooth.BluetoothDevice
import network.xyo.ble.generic.gatt.server.XYBluetoothGattServer

interface XYBluetoothReadResponder {
    fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest?
}

interface XYBluetoothWriteResponder {
    fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean?
}
