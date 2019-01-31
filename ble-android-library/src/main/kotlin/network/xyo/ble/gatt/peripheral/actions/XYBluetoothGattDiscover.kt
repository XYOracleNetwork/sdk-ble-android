package network.xyo.ble.gatt.peripheral.actions

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothGattCallback
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.gatt.peripheral.asyncBle
import network.xyo.core.XYBase
import kotlin.coroutines.resume

class XYBluetoothGattDiscover(val gatt: BluetoothGatt, val gattCallback: XYBluetoothGattCallback) {

    private var _timeout = 5000L

    fun timeout(timeout: Long) {
        _timeout = timeout
    }

    var listenerName = "XYBluetoothGattDiscover${hashCode()}"

    fun start() = asyncBle {
        log.info("discover")
        var error: XYBluetoothError? = null
        var value: List<BluetoothGattService>? = null

        if (gatt.services != null && gatt.services.size > 0) {
            value = gatt.services
        } else {
            try {
                withTimeout(_timeout) {
                    value = suspendCancellableCoroutine { cont ->
                        log.info("discover: Doing real discover")
                        val listener = object : BluetoothGattCallback() {
                            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                                super.onServicesDiscovered(gatt, status)
                                assert(this@XYBluetoothGattDiscover.gatt == gatt)
                                gattCallback.removeListener(listenerName)
                                if (status != BluetoothGatt.GATT_SUCCESS) {
                                    error = XYBluetoothError("discover: discoverStatus: $status")
                                    cont.resume(null)
                                } else {
                                    //success - send back the services
                                    cont.resume(this@XYBluetoothGattDiscover.gatt.services)
                                }
                            }

                            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                                super.onConnectionStateChange(gatt, status, newState)
                                assert(this@XYBluetoothGattDiscover.gatt == gatt)
                                if (newState != BluetoothGatt.STATE_CONNECTED) {
                                    error = XYBluetoothError("asyncDiscover: connection dropped")
                                    gattCallback.removeListener(listenerName)
                                    cont.resume(null)
                                }
                            }
                        }
                        gattCallback.addListener(listenerName, listener)
                        if (!gatt.discoverServices()) {
                            error = XYBluetoothError("start: gatt.discoverServices failed to start")
                            gattCallback.removeListener(listenerName)
                            cont.resume(null)
                        }
                        /*if (gatt.connectionState != XYBluetoothGattBase.ConnectionState.Connected) {
                            error = XYBluetoothError("discover: connection dropped 2: $xyGatt.connectionState")
                            listenerName?.let {
                                xyGatt.removeGattListener(it)
                                listenerName = null
                            }
                            cont.resume(null)
                        }*/
                    }
                }
            } catch (ex: TimeoutCancellationException) {
                error = XYBluetoothError("start: Timeout")
                gattCallback.removeListener(listenerName)
                log.error(ex)
            }
        }
        return@asyncBle XYBluetoothResult(value, error)
    }

    companion object: XYBase()
}