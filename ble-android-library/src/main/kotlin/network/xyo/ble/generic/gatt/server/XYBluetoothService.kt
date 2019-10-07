package network.xyo.ble.generic.gatt.server

import android.bluetooth.BluetoothGattService
import java.util.UUID

open class XYBluetoothService(uuid: UUID, serviceType: Int) : BluetoothGattService(uuid, serviceType)
