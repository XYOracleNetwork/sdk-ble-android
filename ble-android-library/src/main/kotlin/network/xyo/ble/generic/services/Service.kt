package network.xyo.ble.services

import android.bluetooth.BluetoothGattCharacteristic
import network.xyo.base.XYBase
import network.xyo.ble.devices.XYBluetoothDevice
import java.util.*

abstract class Service(val device: XYBluetoothDevice) : XYBase() {

    abstract val serviceUuid : UUID
    val characteristics =  HashMap<UUID, Characteristic>()

    open class Characteristic(val service: Service, val uuid:UUID, val name:String) : XYBase() {
        suspend fun enableNotify(enable: Boolean) = service.enableNotify(uuid, enable)
    }

    class IntegerCharacteristic(service: Service, uuid:UUID, name:String, private val formatType: Int = BluetoothGattCharacteristic.FORMAT_UINT8, val offset:Int = 0) : Characteristic(service, uuid, name) {
        suspend fun get() = service.readInt(uuid, formatType, offset)
        suspend fun set(value: Int) = service.writeInt(uuid, value, formatType, offset)
    }

    class FloatCharacteristic(service: Service, uuid:UUID, name:String, private val formatType: Int = BluetoothGattCharacteristic.FORMAT_FLOAT, val offset:Int = 0) : Characteristic(service, uuid, name) {
        suspend fun get() = service.readFloat(uuid, formatType, offset)
        suspend fun set(mantissa: Int, exponent: Int) = service.writeFloat(uuid, mantissa, exponent, formatType, offset)
    }

    class StringCharacteristic(service: Service, uuid:UUID, name:String) : Characteristic(service, uuid, name) {
        suspend fun get() = service.readString(uuid)
        suspend fun set(value: String) = service.writeString(uuid, value)
    }

    class BytesCharacteristic(service: Service, uuid:UUID, name:String) : Characteristic(service, uuid, name) {
        suspend fun get() = service.readBytes(uuid)
        suspend fun set(value: ByteArray) = service.writeBytes(uuid, value)
    }

    private suspend fun readInt(characteristic: UUID, formatType: Int = BluetoothGattCharacteristic.FORMAT_UINT8, offset:Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicInt(
                serviceUuid,
                characteristic,
                formatType,
                offset
        )
    }

    private suspend fun writeInt(characteristic: UUID, value: Int, formatType: Int = BluetoothGattCharacteristic.FORMAT_UINT8, offset:Int = 0) = device.connection {
        log.info("writeInt: connection")
        return@connection device.findAndWriteCharacteristic(
                serviceUuid,
                characteristic,
                value,
                formatType,
                offset
        )
    }

    private suspend fun readFloat(characteristic: UUID, formatType: Int = BluetoothGattCharacteristic.FORMAT_FLOAT, offset:Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicFloat(
                serviceUuid,
                characteristic,
                formatType,
                offset
        )
    }

    private suspend fun writeFloat(characteristic: UUID, mantissa: Int, exponent: Int, formatType: Int = BluetoothGattCharacteristic.FORMAT_FLOAT, offset:Int = 0) = device.connection {
        return@connection device.findAndWriteCharacteristicFloat(
                serviceUuid,
                characteristic,
                mantissa,
                exponent,
                formatType,
                offset
        )
    }

    private suspend fun readString(characteristic: UUID, offset:Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicString(
                serviceUuid,
                characteristic,
                offset
        )
    }

    private suspend fun writeString(characteristic: UUID, value: String) = device.connection {
        return@connection  device.findAndWriteCharacteristic(
                serviceUuid,
                characteristic,
                value
        )
    }

    private suspend fun enableNotify(characteristic: UUID, enabled: Boolean) = device.connection {
        return@connection device.findAndWriteCharacteristicNotify(
                serviceUuid,
                characteristic,
                enabled
        )
    }

    private suspend fun readBytes(characteristic: UUID) = device.connection {
        return@connection  device.findAndReadCharacteristicBytes(
                serviceUuid,
                characteristic
        )
    }

    private suspend fun writeBytes(characteristic: UUID, bytes: ByteArray) = device.connection {
        return@connection device.findAndWriteCharacteristic(
                serviceUuid,
                characteristic,
                bytes
        )
    }
}