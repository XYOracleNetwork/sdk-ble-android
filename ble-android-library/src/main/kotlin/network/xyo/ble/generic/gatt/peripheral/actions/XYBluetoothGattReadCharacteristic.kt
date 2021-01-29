package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.*
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

class XYBluetoothGattReadCharacteristic(val gatt: XYThreadSafeBluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 15000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    fun completeStartCoroutine(cont: CancellableContinuation<BluetoothGattCharacteristic?>, value: BluetoothGattCharacteristic? = null) {
        GlobalScope.launch {
            val idempotent = cont.tryResume(value)
            idempotent?.let { token ->
                cont.completeResume(token)
            }
        }
    }

    suspend fun start(characteristicToRead: BluetoothGattCharacteristic): XYBluetoothResult<BluetoothGattCharacteristic?> {
        log.info("readCharacteristic")
        val listenerName = "XYBluetoothGattReadCharacteristic${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        val value: BluetoothGattCharacteristic?

        value = suspendCancellableCoroutine { cont ->
            val listener = object : BluetoothGattCallback() {

                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    super.onCharacteristicRead(gatt, characteristic, status)
                    if (characteristicToRead == characteristic) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            gattCallback.removeListener(listenerName)

                            completeStartCoroutine(cont, characteristic)
                        } else {
                            error = XYBluetoothResultErrorCode.CharacteristicReadFailed
                            gattCallback.removeListener(listenerName)

                            completeStartCoroutine(cont)
                        }
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (newState != BluetoothGatt.STATE_CONNECTED) {
                        error = XYBluetoothResultErrorCode.Disconnected
                        gattCallback.removeListener(listenerName)

                        completeStartCoroutine(cont)
                    }
                }
            }
            gattCallback.addListener(listenerName, listener)
            GlobalScope.launch {
                if (gatt.readCharacteristic(characteristicToRead) != true) {
                    error = XYBluetoothResultErrorCode.ReadCharacteristicFailedToStart
                    gattCallback.removeListener(listenerName)

                    completeStartCoroutine(cont)
                }
            }
        }

        return XYBluetoothResult(value, error)
    }

    companion object : XYBase()
}
