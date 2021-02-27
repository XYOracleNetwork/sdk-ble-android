package network.xyo.ble.generic.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.lang.ref.WeakReference
import java.util.UUID
import network.xyo.base.XYBase
import network.xyo.ble.debug
import network.xyo.ble.generic.gatt.peripheral.exceptions.NoGattRuntimeException

@Suppress("unused")
open class ThreadSafeBluetoothGattWrapper(bluetoothGatt: BluetoothGatt?) : XYBase() {

    private val gattRef = WeakReference<BluetoothGatt>(bluetoothGatt)
    private val gatt: BluetoothGatt?
        get() {
            return gattRef.get()
        }

    val services: List<BluetoothGattService>?
        get() {
            return gatt?.services ?: run {
                debug {
                    throw NoGattRuntimeException()
                }
                null
            }
        }

    fun service(uuid: UUID): BluetoothGattService? {
        return gatt?.getService(uuid) ?: run {
            debug {
                throw NoGattRuntimeException()
            }
            null
        }
    }

    suspend fun close() = bleAsync {
        val result = XYBluetoothResult<Unit>()
        gatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun disconnect() = bleAsync {
        val result = XYBluetoothResult<Unit>()
        gatt?.disconnect() ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun connect() = bleAsync {
        val result = XYBluetoothResult<Unit>()
        gatt?.connect() ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    @TargetApi(26)
    suspend fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) = bleAsync {
        val result = XYBluetoothResult<Unit>()
        gatt?.setPreferredPhy(txPhy, rxPhy, phyOptions) ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    @TargetApi(26)
    suspend fun readPhy() = bleAsync {
        val result = XYBluetoothResult<Unit>()
        gatt?.readPhy() ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun discoverServices() = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.discoverServices()
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.readCharacteristic(characteristic)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.writeCharacteristic(characteristic)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.readDescriptor(descriptor)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.writeDescriptor(descriptor)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.setCharacteristicNotification(characteristic, enable)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun readRemoteRssi() = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.readRemoteRssi()
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()

    suspend fun requestMtu(mtu: Int) = bleAsync {
        val result = XYBluetoothResult<Boolean>()
        gatt?.let { gatt ->
            result.value = gatt.requestMtu(mtu)
        } ?: run {
            result.error = XYBluetoothResultErrorCode.NoGatt
        }
        return@bleAsync result
    }.await()
}
