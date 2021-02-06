package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.*
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

class XYBluetoothGattReadCharacteristic(
        gatt: XYThreadSafeBluetoothGatt,
        gattCallback: XYBluetoothGattCallback,
        timeout: Long = 15000L)
    : XYBluetoothGattAction<BluetoothGattCharacteristic>(gatt, gattCallback, timeout) {

    suspend fun start(characteristicToRead: BluetoothGattCharacteristic): XYBluetoothResult<BluetoothGattCharacteristic?> {
        log.info("readCharacteristic")
        val listenerName = "XYBluetoothGattReadCharacteristic${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None

        val value: BluetoothGattCharacteristic? = suspendCancellableCoroutine { cont ->
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
                if (gatt.readCharacteristic(characteristicToRead).value != true) {
                    error = XYBluetoothResultErrorCode.ReadCharacteristicFailedToStart
                    gattCallback.removeListener(listenerName)

                    completeStartCoroutine(cont)
                }
            }
        }

        return XYBluetoothResult(value, error)
    }
}
