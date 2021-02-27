package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.os.SystemClock
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.actions.XYBluetoothGattConnect
import kotlin.coroutines.resume

suspend fun requestMtuImpl(connection: XYBluetoothGattConnect, mtu: Int, callback: XYBluetoothGattCallback): XYBluetoothResult<Int> {
    return suspendCancellableCoroutine { cont ->
        val key = "$mtu requestMtu ${SystemClock.currentThreadTimeMillis()}"

        callback.addListener(key, object : BluetoothGattCallback() {
            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                super.onMtuChanged(gatt, mtu, status)

                callback.removeListener(key)

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    cont.resume(XYBluetoothResult(mtu))
                    return
                }

                cont.resume(XYBluetoothResult(XYBluetoothResultErrorCode.Unknown))
            }
        })

        GlobalScope.launch {
            if (connection.gatt?.requestMtu(mtu)?.value != true) {
                cont.resume(XYBluetoothResult(XYBluetoothResultErrorCode.Unknown))
            }
        }
    }
}
