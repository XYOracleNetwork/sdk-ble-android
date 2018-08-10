package network.xyo.ble.gatt

import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.content.Context

open class XYBluetoothGattServer(context: Context)
    : XYBluetoothBase(
        context
    ) {

    var gattServer: BluetoothGattServer? = null

    private var callback = object : BluetoothGattServerCallback() {

    }

    fun startServer() : Boolean {
        var result = false
        synchronized(this) {
            if (gattServer == null) {
                gattServer = bluetoothManager?.openGattServer(context, callback)
                if (gattServer != null) {
                    result = true
                }
            }
        }
        return result
    }

    fun stopServer() {
        synchronized(this) {
            gattServer?.close()
            gattServer = null
        }
    }

}