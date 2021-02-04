package network.xyo.ble.generic.gatt.server

import android.bluetooth.BluetoothGattCharacteristic

@Suppress("unused")
enum class XYBluetoothCharacteristicPermissions constructor(val value: Int) {
    PERMISSION_WRITE(BluetoothGattCharacteristic.PERMISSION_WRITE),
    PERMISSION_READ_ENCRYPTED(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED),
    PERMISSION_READ_ENCRYPTED_MITM(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM),
    PERMISSION_WRITE_ENCRYPTED(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED),
    PERMISSION_READ(BluetoothGattCharacteristic.PERMISSION_READ),
    PERMISSION_WRITE_ENCRYPTED_MITM(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM),
    PERMISSION_WRITE_SIGNED(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED),
    PERMISSION_WRITE_SIGNED_MITM(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM)
}
