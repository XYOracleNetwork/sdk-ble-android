package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.*
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

class XYBluetoothGattWriteDescriptor(val gatt: XYThreadSafeBluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 15000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    suspend fun start(descriptorToWrite: BluetoothGattDescriptor) = GlobalScope.async {
        log.info("writeDescriptor")
        val listenerName = "XYBluetoothGattWriteDescriptor${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: ByteArray? = null

        try {
            withTimeout(_timeout) {
                value = suspendCancellableCoroutine { cont ->
                    val listener = object : BluetoothGattCallback() {
                        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                            log.info("onDescriptorWrite: $status")
                            super.onDescriptorWrite(gatt, descriptor, status)
                            // since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (descriptorToWrite == descriptor) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    gattCallback.removeListener(listenerName)

                                    val idempotent = cont.tryResume(descriptorToWrite.value)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                } else {
                                    error = XYBluetoothResultErrorCode.DescriptorWriteFailed
                                    gattCallback.removeListener(listenerName)

                                    val idempotent = cont.tryResume(null)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                }
                            }
                        }

                        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                            log.info("onConnectionStateChange")
                            super.onConnectionStateChange(gatt, status, newState)
                            if (newState != BluetoothGatt.STATE_CONNECTED) {
                                error = XYBluetoothResultErrorCode.Disconnected
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
                        if (gatt.writeDescriptor(descriptorToWrite) != true) {
                            error = XYBluetoothResultErrorCode.DescriptorWriteFailedToStart
                            gattCallback.removeListener(listenerName)

                            val idempotent = cont.tryResume(null)
                            idempotent?.let {
                                cont.completeResume(it)
                            }
                        }
                    }
                }
            }
        } catch (ex: TimeoutCancellationException) {
            error = XYBluetoothResultErrorCode.Timeout
            gattCallback.removeListener(listenerName)
            log.error(ex)
        }

        return@async XYBluetoothResult(value, error)
    }.await()

    companion object : XYBase()
}
