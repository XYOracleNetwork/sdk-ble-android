package network.xyo.ble.services.dialog

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.services.Service
import java.util.*

class SpotaService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    val SPOTA_MEM_DEV = IntegerCharacteristic(this, Characteristics.MEM_DEV.uuid, BluetoothGattCharacteristic.FORMAT_UINT32)     //32
    val SPOTA_GPIO_MAP = IntegerCharacteristic(this, Characteristics.GPIO_MAP.uuid, BluetoothGattCharacteristic.FORMAT_UINT32)   //32
    val SPOTA_MEM_INFO = IntegerCharacteristic(this, Characteristics.MEM_INFO.uuid, BluetoothGattCharacteristic.FORMAT_UINT32)   //32
    val SPOTA_PATCH_LEN = IntegerCharacteristic(this, Characteristics.PATCH_LEN.uuid, BluetoothGattCharacteristic.FORMAT_UINT16) //16
    val SPOTA_PATCH_DATA = BytesCharacteristic(this, Characteristics.PATCH_DATA.uuid ) //TODO- correct format?
    val SPOTA_SERV_STATUS = IntegerCharacteristic(this, Characteristics.SERV_STATUS.uuid, BluetoothGattCharacteristic.FORMAT_UINT32) //TODO- correct format?

    companion object {

        val uuid: UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb")

        enum class Characteristics(val uuid: UUID) {
            MEM_DEV(UUID.fromString(            "8082caa8-41a6-4021-91c6-56f9b954cc34")),
            GPIO_MAP(UUID.fromString(           "724249f0-5eC3-4b5f-8804-42345af08651")),
            MEM_INFO(UUID.fromString(           "6c53db25-47a1-45fe-a022-7c92fb334fd4")),
            PATCH_LEN(UUID.fromString(          "9d84b9a3-000c-49d8-9183-855b673fda31")),
            PATCH_DATA(UUID.fromString(         "457871e8-d516-4ca1-9116-57d0b17b9cb2")),
            SERV_STATUS(UUID.fromString(        "5f78df94-798c-46f5-990a-b3eb6a065c88"))
        }
    }
}

//SPOTA_SERV_STATUS - spotaNotification characteristicId
//uuid - serviceId