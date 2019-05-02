package network.xyo.ble.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.*
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.gatt.peripheral.XYThreadSafeBluetoothGatt
import network.xyo.core.XYBase

class XYBluetoothGattWriteCharacteristic(val gatt: XYThreadSafeBluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 15000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    fun start(characteristicToWrite: BluetoothGattCharacteristic) = GlobalScope.async {
        log.info("writeCharacteristic")
        val listenerName = "XYBluetoothGattWriteCharacteristic${hashCode()}"
        var error: XYBluetoothError? = null
        var value: ByteArray? = null

        try {
            withTimeoutOrNull(_timeout) {
                value = suspendCancellableCoroutine { cont ->
                    val listener = object : BluetoothGattCallback() {
                        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                            log.info("onCharacteristicWrite: $status")
                            super.onCharacteristicWrite(gatt, characteristic, status)
                            //since it is always possible to have a rogue callback from a previously timedout call,
                            //make sure it is the one we are looking for
                            if (characteristicToWrite.uuid == characteristic?.uuid) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    gattCallback.removeListener(listenerName)
                                    val idempotent = cont.tryResume(characteristicToWrite.value)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                } else {
                                    error = XYBluetoothError("writeCharacteristic: onCharacteristicWrite failed: $status")
                                    gattCallback.removeListener(listenerName)
                                    if (!isActive) {
                                        return
                                    }

                                    val idempotent = cont.tryResume(null)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                }
                            }
                        }

                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            log.info("onCharacteristicWrite")
                            super.onConnectionStateChange(gatt, status, newState)
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothError("writeCharacteristic: connection dropped")
                                gattCallback.removeListener(listenerName)
                                if (!isActive) {
                                    return
                                }

                                val idempotent = cont.tryResume(null)
                                idempotent?.let {
                                    cont.completeResume(it)
                                }
                            }
                        }
                    }
                    gattCallback.addListener(listenerName, listener)
                    GlobalScope.launch {
                        val writeStarted = gatt.writeCharacteristic(characteristicToWrite).await()
                        if (writeStarted != true) {
                            error = XYBluetoothError("writeCharacteristic: gatt.writeCharacteristic failed to start")
                            gattCallback.removeListener(listenerName)
                            if (!isActive) {
                                return@launch
                            }

                            val idempotent = cont.tryResume(null)
                            idempotent?.let {
                                cont.completeResume(it)
                            }
                        }
                    }
                }
            }
        } catch (ex: TimeoutCancellationException) {
            error = XYBluetoothError("start: Timeout")
            gattCallback.removeListener(listenerName)
            XYBluetoothGattDiscover.log.error(ex)
        }

        return@async XYBluetoothResult(value, error)
    }

    companion object : XYBase()
}