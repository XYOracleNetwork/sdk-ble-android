package network.xyo.ble.generic.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import network.xyo.base.XYBase
import java.lang.ref.WeakReference
import java.util.*

open class XYThreadSafeBluetoothGatt(bluetoothGatt: BluetoothGatt?): XYBase() {

    private val gattRef = WeakReference<BluetoothGatt>(bluetoothGatt)
    private val gatt: BluetoothGatt?
        get() {
            return gattRef.get()
        }

    suspend fun close() = asyncBle {
        gatt?.disconnect()
        gatt?.close()
    }

    suspend fun disconnect() = asyncBle {
        gatt?.disconnect()
    }

    suspend fun connect() = asyncBle {
        return@asyncBle gatt?.connect()
    }

    @TargetApi(26)
    suspend fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) = asyncBle {
        return@asyncBle gatt?.setPreferredPhy(txPhy, rxPhy, phyOptions)
    }

    @TargetApi(26)
    suspend fun readPhy() = asyncBle {
        return@asyncBle gatt?.readPhy()
    }

    suspend fun discoverServices() = asyncBle {
        return@asyncBle gatt?.discoverServices()
    }

    val services: List<BluetoothGattService>?
        get() {
            return gatt?.services
        }

    fun getService(uuid: UUID): BluetoothGattService? {
        return gatt?.getService(uuid)
    }

    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic) = asyncBle {
        return@asyncBle gatt?.readCharacteristic(characteristic)
    }

    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) = asyncBle {
        return@asyncBle gatt?.writeCharacteristic(characteristic)
    }

    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor) = asyncBle {
        return@asyncBle gatt?.readDescriptor(descriptor)
    }

    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor) = asyncBle {
        return@asyncBle gatt?.writeDescriptor(descriptor)
    }

    suspend fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) = asyncBle {
        return@asyncBle gatt?.setCharacteristicNotification(characteristic, enable)
    }

    suspend fun readRemoteRssi() = asyncBle {
        return@asyncBle gatt?.readRemoteRssi()
    }

    suspend fun requestMtu(mtu: Int) = asyncBle {
        return@asyncBle gatt?.requestMtu(mtu)
    }
}