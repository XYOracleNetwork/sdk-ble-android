package network.xyo.ble.generic.services

import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID
import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult

@Suppress("unused")
open class Characteristic(val service: Service, val uuid: UUID, val name: String) : XYBase() {
    suspend fun enableNotify(enable: Boolean) = service.enableNotify(uuid, enable)
}

@Suppress("unused")
class ByteCharacteristic(service: Service, uuid: UUID, name: String, val offset: Int = 0) : Characteristic(service, uuid, name) {
    suspend fun get() = service.readByte(uuid, offset)
    suspend fun set(value: UByte): XYBluetoothResult<UByte> {
        return service.writeByte(uuid, value, offset)
    }
}

@Suppress("unused")
class ShortCharacteristic(service: Service, uuid: UUID, name: String, val offset: Int = 0) : Characteristic(service, uuid, name) {
    suspend fun get() = service.readShort(uuid, offset)
    suspend fun set(value: UShort): XYBluetoothResult<UShort> {
        return service.writeShort(uuid, value, offset)
    }
}

class IntCharacteristic(service: Service, uuid: UUID, name: String, val offset: Int = 0) : Characteristic(service, uuid, name) {
    suspend fun get() = service.readInt(uuid, offset)
    suspend fun set(value: UInt): XYBluetoothResult<UInt> {
        return service.writeInt(uuid, value, offset)
    }
}

class StringCharacteristic(service: Service, uuid: UUID, name: String) : Characteristic(service, uuid, name) {
    suspend fun get() = service.readString(uuid)
    suspend fun set(value: String) = service.writeString(uuid, value)
}

class BytesCharacteristic(service: Service, uuid: UUID, name: String) : Characteristic(service, uuid, name) {
    suspend fun get() = service.readBytes(uuid)
    suspend fun set(value: ByteArray) = service.writeBytes(uuid, value)
}

@Suppress("unused")
abstract class Service(val device: XYBluetoothDevice) : XYBase() {

    abstract val serviceUuid: UUID
    val characteristics = HashMap<UUID, Characteristic>()

    suspend fun readByte(characteristic: UUID, offset: Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicByte(
                serviceUuid,
                characteristic,
                offset
        )
    }

    suspend fun writeByte(characteristic: UUID, value: UByte, offset: Int = 0) = device.connection {
        log.info("writeInt: connection")
        return@connection device.findAndWriteCharacteristicByte(
                serviceUuid,
                characteristic,
                value,
                offset
        )
    }

    suspend fun readShort(characteristic: UUID, offset: Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicShort(
                serviceUuid,
                characteristic,
                offset
        )
    }

    suspend fun writeShort(characteristic: UUID, value: UShort, offset: Int = 0) = device.connection {
        log.info("writeInt: connection")
        return@connection device.findAndWriteCharacteristicShort(
                serviceUuid,
                characteristic,
                value,
                offset
        )
    }

    suspend fun readInt(characteristic: UUID, offset: Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicInt(
                serviceUuid,
                characteristic,
                offset
        )
    }

    suspend fun writeInt(characteristic: UUID, value: UInt, offset: Int = 0) = device.connection {
        log.info("writeInt: connection")
        return@connection device.findAndWriteCharacteristicInt(
                serviceUuid,
                characteristic,
                value,
                offset
        )
    }

    suspend fun readFloat(characteristic: UUID, formatType: Int = BluetoothGattCharacteristic.FORMAT_FLOAT, offset: Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicFloat(
                serviceUuid,
                characteristic,
                formatType,
                offset
        )
    }

    suspend fun writeFloat(characteristic: UUID, mantissa: Int, exponent: Int, formatType: Int = BluetoothGattCharacteristic.FORMAT_FLOAT, offset: Int = 0) = device.connection {
        return@connection device.findAndWriteCharacteristicFloat(
                serviceUuid,
                characteristic,
                mantissa,
                exponent,
                formatType,
                offset
        )
    }

    suspend fun readString(characteristic: UUID, offset: Int = 0) = device.connection {
        return@connection device.findAndReadCharacteristicString(
                serviceUuid,
                characteristic,
                offset
        )
    }

    suspend fun writeString(characteristic: UUID, value: String) = device.connection {
        return@connection device.findAndWriteCharacteristic(
                serviceUuid,
                characteristic,
                value
        )
    }

    suspend fun enableNotify(characteristic: UUID, enabled: Boolean) = device.connection {
        return@connection device.findAndWriteCharacteristicNotify(
                serviceUuid,
                characteristic,
                enabled
        )
    }

    suspend fun readBytes(characteristic: UUID) = device.connection {
        return@connection device.findAndReadCharacteristicBytes(
                serviceUuid,
                characteristic
        )
    }

    suspend fun writeBytes(characteristic: UUID, bytes: ByteArray) = device.connection {
        return@connection device.findAndWriteCharacteristic(
                serviceUuid,
                characteristic,
                bytes
        )
    }
}
