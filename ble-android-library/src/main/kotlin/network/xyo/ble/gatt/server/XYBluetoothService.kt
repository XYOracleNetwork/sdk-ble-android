package network.xyo.ble.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*
import kotlin.collections.HashMap

open class XYBluetoothService (uuid: UUID, serviceType : Int) : BluetoothGattService(uuid, serviceType)