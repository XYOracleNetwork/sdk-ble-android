package network.xyo.ble.services.dialog

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.services.Service
import java.util.*

class SpotaService(device: XYBluetoothDevice) : Service(device) {

    override val serviceUuid: UUID
        get() {
            return uuid
        }

    init {
        characteristics[Characteristics.MEM_DEV.uuid] = ByteCharacteristic(this, Characteristics.MEM_DEV.uuid, "Memory Dev", BluetoothGattCharacteristic.FORMAT_UINT32)
        characteristics[Characteristics.GPIO_MAP.uuid] = ByteCharacteristic(this, Characteristics.MEM_DEV.uuid, "GPIO Map", BluetoothGattCharacteristic.FORMAT_UINT32)
        characteristics[Characteristics.MEM_INFO.uuid] = ByteCharacteristic(this, Characteristics.MEM_DEV.uuid, "Memory Info", BluetoothGattCharacteristic.FORMAT_UINT32)
        characteristics[Characteristics.PATCH_LEN.uuid] = ByteCharacteristic(this, Characteristics.MEM_DEV.uuid, "Patch Length", BluetoothGattCharacteristic.FORMAT_UINT16)
        characteristics[Characteristics.PATCH_DATA.uuid] = BytesCharacteristic(this, Characteristics.MEM_DEV.uuid, "Patch Data")
        characteristics[Characteristics.SERV_STATUS.uuid] = ByteCharacteristic(this, Characteristics.MEM_DEV.uuid, "Service Status", BluetoothGattCharacteristic.FORMAT_UINT32)
    }

    val spotaMemDev: IntCharacteristic
        get() {
            return characteristics[Characteristics.MEM_DEV.uuid] as IntCharacteristic
        }

    val spotaGpioMap: IntCharacteristic
        get() {
            return characteristics[Characteristics.GPIO_MAP.uuid] as IntCharacteristic
        }

    val spotaMemInfo: ByteCharacteristic
        get() {
            return characteristics[Characteristics.MEM_INFO.uuid] as ByteCharacteristic
        }

    val spotaPatchLen: IntCharacteristic
        get() {
            return characteristics[Characteristics.PATCH_LEN.uuid] as IntCharacteristic
        }

    val spotaPatchData: BytesCharacteristic
        get() {
            return characteristics[Characteristics.PATCH_DATA.uuid] as BytesCharacteristic
        }

    val spotaServStatus: ByteCharacteristic
        get() {
            return characteristics[Characteristics.SERV_STATUS.uuid] as ByteCharacteristic
        }


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
