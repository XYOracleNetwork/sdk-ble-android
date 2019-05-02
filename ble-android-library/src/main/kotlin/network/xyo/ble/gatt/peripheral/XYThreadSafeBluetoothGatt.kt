package network.xyo.ble.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.Deferred
import network.xyo.core.XYBase
import java.lang.ref.WeakReference
import java.util.*

open class XYThreadSafeBluetoothGatt(bluetoothGatt: BluetoothGatt?): XYBase() {

    private val gattRef = WeakReference<BluetoothGatt>(bluetoothGatt)
    private val gatt: BluetoothGatt?
        get() {
            return gattRef.get()
        }

    fun close() = asyncBle {
        gatt?.disconnect()
        gatt?.close()
    }

    fun disconnect() = asyncBle {
        gatt?.disconnect()
    }

    fun connect() = asyncBle {
        return@asyncBle gatt?.connect()
    }

    @TargetApi(26)
    fun setPreferredPhy(txPhy: Int, rxPhy: Int, phyOptions: Int) = asyncBle {
        return@asyncBle gatt?.setPreferredPhy(txPhy, rxPhy, phyOptions)
    }

    @TargetApi(26)
    fun readPhy() = asyncBle {
        return@asyncBle gatt?.readPhy()
    }

    fun discoverServices() = asyncBle {
        return@asyncBle gatt?.discoverServices()
    }

    val services: List<BluetoothGattService>?
        get() {
            return gatt?.services
        }

    fun getService(uuid: UUID): BluetoothGattService? {
        return gatt?.getService(uuid)
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) = asyncBle {
        return@asyncBle gatt?.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) = asyncBle {
        return@asyncBle gatt?.writeCharacteristic(characteristic)
    }

    fun readDescriptor(descriptor: BluetoothGattDescriptor) = asyncBle {
        return@asyncBle gatt?.readDescriptor(descriptor)
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor) = asyncBle {
        return@asyncBle gatt?.writeDescriptor(descriptor)
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) = asyncBle {
        return@asyncBle gatt?.setCharacteristicNotification(characteristic, enable)
    }

    fun readRemoteRssi() = asyncBle {
        return@asyncBle gatt?.readRemoteRssi()
    }

    fun requestMtu(mtu: Int) = asyncBle {
        return@asyncBle gatt?.requestMtu(mtu)
    }
}