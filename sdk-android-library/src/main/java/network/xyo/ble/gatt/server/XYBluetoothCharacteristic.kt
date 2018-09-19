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
}