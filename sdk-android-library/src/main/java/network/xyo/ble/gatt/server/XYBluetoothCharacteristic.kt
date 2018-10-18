package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.*
import kotlin.collections.HashMap

open class XYBluetoothCharacteristic(uuid: UUID, properties : Int, permissions : Int) : BluetoothGattCharacteristic(uuid, properties, permissions) {
    private val listeners = HashMap<String, XYBluetoothCharacteristicListener>()
    private val descriptors = HashMap<UUID, BluetoothGattDescriptor>()

    protected fun onChange () {
        for ((_, listener) in listeners) {
            listener.onChange()
        }
    }

    fun addListiner(key : String, listener: XYBluetoothCharacteristicListener) {
        listeners[key] = listener
    }

    fun removeListiner (key: String) {
        listeners.remove(key)
    }

    interface XYBluetoothCharacteristicListener {
        fun onChange()
    }

    companion object {
        enum class Properties constructor(val value: Int) {
            PROPERTY_BROADCAST(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
            PROPERTY_INDICATE(BluetoothGattCharacteristic.PROPERTY_INDICATE),
            PROPERTY_NOTIFY(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
            PROPERTY_READ(BluetoothGattCharacteristic.PROPERTY_READ),
            PROPERTY_SIGNED_WRITE(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
            PROPERTY_WRITE(BluetoothGattCharacteristic.PROPERTY_WRITE),
            PROPERTY_WRITE_NO_RESPONSE(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
        }

        enum class Permissions constructor(val value: Int) {
            PERMISSION_WRITE(BluetoothGattCharacteristic.PERMISSION_WRITE),
            PERMISSION_READ_ENCRYPTED(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED),
            PERMISSION_READ_ENCRYPTED_MITM(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM),
            PERMISSION_WRITE_ENCRYPTED(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED),
            PERMISSION_READ(BluetoothGattCharacteristic.PERMISSION_READ),
            PERMISSION_WRITE_ENCRYPTED_MITM(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM),
            PERMISSION_WRITE_SIGNED(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED),
            PERMISSION_WRITE_SIGNED_MITM(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM)
        }
    }
}