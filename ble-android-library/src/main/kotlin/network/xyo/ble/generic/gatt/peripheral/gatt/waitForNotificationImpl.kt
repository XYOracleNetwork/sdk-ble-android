package network.xyo.ble.generic.gatt.peripheral.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import java.util.*
import kotlin.coroutines.resume

suspend fun waitForNotificationImpl(characteristicToWaitFor: UUID, callback: XYBluetoothGattCallback): XYBluetoothResult<Any?> {
    return suspendCancellableCoroutine { cont ->
        val listenerName = "waitForNotification${SystemClock.currentThreadTimeMillis()}"
        val listener = object : BluetoothGattCallback() {
            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                if (characteristicToWaitFor == characteristic?.uuid) {
                    callback.removeListener(listenerName)
                    cont.resume(XYBluetoothResult(null, XYBluetoothResultErrorCode.None))
                }
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState != BluetoothGatt.STATE_CONNECTED) {
                    callback.removeListener(listenerName)
                    cont.resume(XYBluetoothResult(null, XYBluetoothResultErrorCode.Disconnected))
                }
            }
        }

        callback.addListener(listenerName, listener)
    }
}
