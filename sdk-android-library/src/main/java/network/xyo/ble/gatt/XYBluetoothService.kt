package network.xyo.ble.gatt

import android.bluetooth.BluetoothGattService
import java.util.*

abstract class XYBluetoothService (uuid: UUID, serviceType : Int) : BluetoothGattService(uuid, serviceType) {


}