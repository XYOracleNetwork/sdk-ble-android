package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.*
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.ThreadSafeBluetoothGattWrapper

class XYBluetoothGattWriteDescriptor(
    gatt: ThreadSafeBluetoothGattWrapper,
    gattCallback: XYBluetoothGattCallback,
    timeout: Long = 15000L
) : XYBluetoothGattAction<ByteArray>(gatt, gattCallback, timeout) {

    suspend fun start(descriptorToWrite: BluetoothGattDescriptor): XYBluetoothResult<ByteArray> {
        log.info("writeDescriptor")
        val listenerName = "XYBluetoothGattWriteDescriptor${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: ByteArray? = null

        try {
            withTimeout(timeout) {
                value = suspendCancellableCoroutine { cont ->
                    val listener = object : BluetoothGattCallback() {
                        override fun onDescriptorWrite(
                            gatt: BluetoothGatt?,
                            descriptor: BluetoothGattDescriptor?,
                            status: Int
                        ) {
                            log.info("onDescriptorWrite: $status")
                            super.onDescriptorWrite(gatt, descriptor, status)
                            // since it is always possible to have a rogue callback, make sure it is the one we are looking for
                            if (descriptorToWrite == descriptor) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    gattCallback.removeListener(listenerName)

                                    completeStartCoroutine(cont, descriptorToWrite.value)
                                } else {
                                    error = XYBluetoothResultErrorCode.DescriptorWriteFailed
                                    gattCallback.removeListener(listenerName)

                                    completeStartCoroutine(cont)
                                }
                            }
                        }

                        override fun onConnectionStateChange(
                            gatt: BluetoothGatt?,
                            status: Int,
                            newState: Int
                        ) {
                            log.info("onConnectionStateChange")
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
                        if (gatt.writeDescriptor(descriptorToWrite).value != true) {
                            error = XYBluetoothResultErrorCode.DescriptorWriteFailedToStart
                            gattCallback.removeListener(listenerName)

                            completeStartCoroutine(cont)
                        }
                    }
                }
            }
        } catch (ex: TimeoutCancellationException) {
            error = XYBluetoothResultErrorCode.Timeout
            gattCallback.removeListener(listenerName)
            log.error(ex)
        }

        return XYBluetoothResult(value, error)
    }
}
