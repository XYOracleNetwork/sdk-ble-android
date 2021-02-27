package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.*
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.ThreadSafeBluetoothGattWrapper

class XYBluetoothGattDiscover(
    gatt: ThreadSafeBluetoothGattWrapper,
    gattCallback: XYBluetoothGattCallback,
    timeout: Long = 1500000L)
    : XYBluetoothGattAction<List<BluetoothGattService>>(gatt, gattCallback, timeout) {

    suspend fun start(): XYBluetoothResult<List<BluetoothGattService>> {
        log.info("discover")
        val listenerName = "XYBluetoothGattDiscover${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: List<BluetoothGattService>? = gatt.services

        if (value?.isEmpty() == false) {
            log.info("discover: Returning previous discover: ${value.size}")
        } else {
            try {
                withTimeout(timeout) {
                    value = suspendCancellableCoroutine { cont ->
                        log.info("discover: Doing real discover")
                        val listener = object : BluetoothGattCallback() {
                            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                                super.onServicesDiscovered(gatt, status)
                                gattCallback.removeListener(listenerName)
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    error = XYBluetoothResultErrorCode.ServiceDiscoveryFailed

                                    completeStartCoroutine(cont)
                                } else {
                                    // success - send back the services
                                    log.info("discover: Returning new services")
                                    this@XYBluetoothGattDiscover.value = gatt?.services
                                    completeStartCoroutine(cont, this@XYBluetoothGattDiscover.value)
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
                            val discoverStarted = gatt.discoverServices().value
                            if (discoverStarted != true) {
                                error = XYBluetoothResultErrorCode.DiscoverServicesFailedToStart
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
        }
        return XYBluetoothResult(value, error)
    }
}
