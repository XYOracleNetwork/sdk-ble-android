package network.xyo.ble.generic.gatt.server

import android.bluetooth.BluetoothGattCharacteristic

@Suppress("unused")
enum class XYBluetoothCharacteristicProperties constructor(val value: Int) {
    PROPERTY_BROADCAST(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
    PROPERTY_INDICATE(BluetoothGattCharacteristic.PROPERTY_INDICATE),
    PROPERTY_NOTIFY(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
    PROPERTY_READ(BluetoothGattCharacteristic.PROPERTY_READ),
    PROPERTY_SIGNED_WRITE(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
    PROPERTY_WRITE(BluetoothGattCharacteristic.PROPERTY_WRITE),
    PROPERTY_WRITE_NO_RESPONSE(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
}
