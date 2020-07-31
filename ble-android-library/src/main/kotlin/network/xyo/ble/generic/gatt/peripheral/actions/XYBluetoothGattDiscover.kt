package network.xyo.ble.generic.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.*
import network.xyo.base.XYBase
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.generic.gatt.peripheral.XYThreadSafeBluetoothGatt

class XYBluetoothGattDiscover(val gatt: XYThreadSafeBluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 1500000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    var services: List<BluetoothGattService>? = null

    suspend fun start() = GlobalScope.async {
        log.info("discover")
        val listenerName = "XYBluetoothGattDiscover${hashCode()}"
        var error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
        var value: List<BluetoothGattService>? = null

        val services = this@XYBluetoothGattDiscover.services ?: gatt.services

        if (services?.isEmpty() == false) {
            log.info("discover: Returning previous discover: ${services.size}")
            value = services
        } else {
            try {
                withTimeout(_timeout) {
                    value = suspendCancellableCoroutine { cont ->
                        log.info("discover: Doing real discover")
                        val listener = object : BluetoothGattCallback() {
                            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                                super.onServicesDiscovered(gatt, status)
                                gattCallback.removeListener(listenerName)
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    error = XYBluetoothResultErrorCode.ServiceDiscoveryFailed

                                    val idempotent = cont.tryResume(null)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                } else {
                                    // success - send back the services
                                    log.info("discover: Returning new services")
                                    this@XYBluetoothGattDiscover.services = gatt?.services

                                    val idempotent = cont.tryResume(this@XYBluetoothGattDiscover.services)
                                    idempotent?.let {
                                        cont.completeResume(it)
                                    }
                                }
                            }

                            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
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
                            val discoverStarted = gatt.discoverServices()
                            if (discoverStarted != true) {
                                error = XYBluetoothResultErrorCode.DiscoverServicesFailedToStart
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
        }
        return@async XYBluetoothResult(value, error)
    }.await()

    companion object : XYBase()
}
