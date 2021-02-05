package network.xyo.ble.generic.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.lang.ref.WeakReference
import java.util.UUID
import network.xyo.base.XYBase

@Suppress("unused")
open class XYThreadSafeBluetoothGatt(bluetoothGatt: BluetoothGatt?) : XYBase() {

    private val gattRef = WeakReference<BluetoothGatt>(bluetoothGatt)
    private val gatt: BluetoothGatt?
        get() {
            return gattRef.get()
        }

    suspend fun close() = bleAsync {
        gatt?.disconnect()
        gatt?.close()
        return@bleAsync XYBluetoothResult(true)
    }.await()

    suspend fun disconnect() = bleAsync {
        gatt?.disconnect()
        return@bleAsync XYBluetoothResult(true)
    }.await()

    suspend fun connect() = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.connect())
    }.await()

    @TargetApi(26)
    suspend fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.setPreferredPhy(txPhy, rxPhy, phyOptions))
    }.await()

    @TargetApi(26)
    suspend fun readPhy() = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.readPhy())
    }.await()

    suspend fun discoverServices() = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.discoverServices())
    }.await()

    val services: List<BluetoothGattService>?
        get() {
            return gatt?.services
        }

    fun getService(uuid: UUID): BluetoothGattService? {
        return gatt?.getService(uuid)
    }

    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.readCharacteristic(characteristic))
    }.await()

    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.writeCharacteristic(characteristic))
    }.await()

    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.readDescriptor(descriptor))
    }.await()

    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.writeDescriptor(descriptor))
    }.await()

    suspend fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.setCharacteristicNotification(characteristic, enable))
    }.await()

    suspend fun readRemoteRssi() = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.readRemoteRssi())
    }.await()

    suspend fun requestMtu(mtu: Int) = bleAsync {
        return@bleAsync XYBluetoothResult(gatt?.requestMtu(mtu))
    }.await()
}
