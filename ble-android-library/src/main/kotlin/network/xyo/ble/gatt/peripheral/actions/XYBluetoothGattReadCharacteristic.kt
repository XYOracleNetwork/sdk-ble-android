package network.xyo.ble.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.gatt.peripheral.XYThreadSafeBluetoothGatt
import network.xyo.base.XYBase

class XYBluetoothGattReadCharacteristic(val gatt: XYThreadSafeBluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 15000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }


    fun start(characteristicToRead: BluetoothGattCharacteristic) = GlobalScope.async {
        log.info("readCharacteristic")
        val listenerName = "XYBluetoothGattReadCharacteristic${hashCode()}"
        var error: XYBluetoothError? = null
        val value: BluetoothGattCharacteristic?

        value = suspendCancellableCoroutine { cont ->
            val listener = object : BluetoothGattCallback() {


                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    super.onCharacteristicRead(gatt, characteristic, status)
                    if (characteristicToRead == characteristic) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            gattCallback.removeListener(listenerName)

                            val idempotent = cont.tryResume(characteristic)
                            idempotent?.let {
                                cont.completeResume(it)
                            }
                        } else {
                            error = XYBluetoothError("readCharacteristic: onCharacteristicRead failed: $status")
                            gattCallback.removeListener(listenerName)

                            val idempotent = cont.tryResume(null)
                            idempotent?.let {
                                cont.completeResume(it)
                            }
                        }
                    }
                }

                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    if (newState != BluetoothGatt.STATE_CONNECTED) {
                        error = XYBluetoothError("readCharacteristic: connection dropped")
                        gattCallback.removeListener(listenerName)

                        val idempotent = cont.tryResume(null)
                        idempotent?.let {
                            cont.completeResume(it)
                        }
                    }
                }
            }
            gattCallback.addListener(listenerName, listener)
            GlobalScope.launch {
                if (gatt.readCharacteristic(characteristicToRead).await() != true) {
                    error = XYBluetoothError("readCharacteristic: gatt.readCharacteristic failed to start")
                    gattCallback.removeListener(listenerName)

                    val idempotent = cont.tryResume(null)
                    idempotent?.let {
                        cont.completeResume(it)
                    }
                }
            }
        }

        return@async XYBluetoothResult(value, error)
    }

    companion object : XYBase()
}