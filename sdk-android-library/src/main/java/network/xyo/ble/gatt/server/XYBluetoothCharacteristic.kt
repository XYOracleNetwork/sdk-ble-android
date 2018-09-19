package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.*
import kotlin.collections.HashMap

open class XYBluetoothCharacteristic(uuid: UUID, properties : Int, permissions : Int) : BluetoothGattCharacteristic(uuid, properties, permissions) {
    private val descreptors = HashMap<UUID, BluetoothGattDescriptor>()
}