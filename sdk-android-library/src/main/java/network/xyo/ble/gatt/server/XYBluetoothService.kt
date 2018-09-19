package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import network.xyo.ble.gatt.server.XYBluetoothGattServer
import java.util.*
import kotlin.collections.HashMap

open class XYBluetoothService (uuid: UUID, serviceType : Int) : BluetoothGattService(uuid, serviceType) {
    val characteristics = HashMap<UUID, BluetoothGattCharacteristic>()

    override fun addCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        characteristics[characteristic.uuid] = characteristic
        return super.addCharacteristic(characteristic)
    }

    open fun onBluetoothChararisticWrite (characteristic: BluetoothGattCharacteristic, device: BluetoothDevice?, value : ByteArray?) : Boolean? {
        val characteristicHandler = characteristics[characteristic.uuid]
        if (characteristicHandler is XYBluetoothWriteCharacteristic) {
            return characteristicHandler.onWriteRequest(value, device)
        }
        return null
    }

    open fun onBluetoothCharacteristicReadRequest (characteristic: BluetoothGattCharacteristic, device: BluetoothDevice?) : ByteArray? {
        val characteristicHandler = characteristics[characteristic.uuid]
        if (characteristicHandler is XYBluetoothReadCharacteristic) {
            return characteristicHandler.onReadRequest(device)
        }
        return null
    }
}