package network.xyo.ble.generic.gatt.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Handler
import java.util.UUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

open class XYBluetoothGattClient protected constructor(
    context: Context,
    device: BluetoothDevice?,
    autoConnect: Boolean,
    callback: XYBluetoothGattCallback?,
    transport: Int?,
    phy: Int?,
    handler: Handler?
) : XYBluetoothGatt(context, device, autoConnect, callback, transport, phy, handler) {

    open fun updateBluetoothDevice(device: BluetoothDevice?) {
        if (device?.address != this.device?.address || this.device == null) {
            this.device = device
        }
    }

    open suspend fun readCharacteristicInt(characteristicToRead: BluetoothGattCharacteristic, formatType: Int, offset: Int): XYBluetoothResult<Int> {
        log.info("readCharacteristicInt")
        val readResult = readCharacteristic(characteristicToRead)
        var value: Int? = null
        val error = readResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            value = readResult.value?.getIntValue(formatType, offset)
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    open suspend fun readCharacteristicString(characteristicToRead: BluetoothGattCharacteristic, offset: Int): XYBluetoothResult<String> {
        log.info("readCharacteristicString")
        val readResult = readCharacteristic(characteristicToRead)
        var value: String? = null
        val error = readResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            value = readResult.value?.getStringValue(offset)
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    open suspend fun readCharacteristicFloat(characteristicToRead: BluetoothGattCharacteristic, formatType: Int, offset: Int): XYBluetoothResult<Float> {
        log.info("readCharacteristicFloat")
        val readResult = readCharacteristic(characteristicToRead)
        var value: Float? = null
        val error = readResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            value = readResult.value?.getFloatValue(formatType, offset)
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    open suspend fun readCharacteristicBytes(characteristicToRead: BluetoothGattCharacteristic): XYBluetoothResult<ByteArray> {
        log.info("readCharacteristicBytes")
        val readResult = readCharacteristic(characteristicToRead)
        var value: ByteArray? = null
        val error = readResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            value = readResult.value?.value
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicInt(service: UUID, characteristic: UUID, formatType: Int, offset: Int = 0): XYBluetoothResult<Int> {
        log.info("findAndReadCharacteristicInt")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: Int? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicInt(characteristicToRead, formatType, offset)
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicByte(service: UUID, characteristic: UUID, offset: Int = 0): XYBluetoothResult<UByte> {
        log.info("findAndReadCharacteristicInt")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: UByte? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicInt(characteristicToRead, BluetoothGattCharacteristic.FORMAT_UINT8, offset)
                error = readResult.error
                value = readResult.value?.toUByte()
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicShort(service: UUID, characteristic: UUID, offset: Int = 0): XYBluetoothResult<UShort> {
        log.info("findAndReadCharacteristicInt")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: UShort? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicInt(characteristicToRead, BluetoothGattCharacteristic.FORMAT_UINT16, offset)
                error = readResult.error
                value = readResult.value?.toUShort()
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicFloat(service: UUID, characteristic: UUID, formatType: Int, offset: Int = 0): XYBluetoothResult<Float> {
        log.info("findAndReadCharacteristicFloat")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: Float? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicFloat(characteristicToRead, formatType, offset)
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicString(service: UUID, characteristic: UUID, offset: Int = 0): XYBluetoothResult<String> {
        log.info("findAndReadCharacteristicString")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: String? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicString(characteristicToRead, offset)
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndReadCharacteristicBytes(service: UUID, characteristic: UUID): XYBluetoothResult<ByteArray> {
        log.info("findAndReadCharacteristicString")
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToRead = findResult.value
        var value: ByteArray? = null
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToRead == null) {
                error = XYBluetoothResultErrorCode.NullValue
            } else {
                val readResult = readCharacteristicBytes(characteristicToRead)
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite: Int, formatType: Int, offset: Int): XYBluetoothResult<Int> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: Int? = null
        val findResult = findCharacteristic(service, characteristic)
        log.info("findAndWriteCharacteristic: Found")
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            log.info("findAndWriteCharacteristic: $characteristicToWrite")
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite, formatType, offset)
                log.info("findAndWriteCharacteristic: Set")
                val writeResult = writeCharacteristic(characteristicToWrite)
                log.info("findAndWriteCharacteristic: Write Complete: $writeResult")
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristicByte(service: UUID, characteristic: UUID, valueToWrite: UByte, offset: Int): XYBluetoothResult<UByte> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: UByte? = null
        val findResult = findCharacteristic(service, characteristic)
        log.info("findAndWriteCharacteristic: Found")
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            log.info("findAndWriteCharacteristic: $characteristicToWrite")
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite.toInt(), BluetoothGattCharacteristic.FORMAT_UINT8, offset)
                log.info("findAndWriteCharacteristic: Set")
                val writeResult = writeCharacteristic(characteristicToWrite)
                log.info("findAndWriteCharacteristic: Write Complete: $writeResult")
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristicFloat(service: UUID, characteristic: UUID, mantissa: Int, exponent: Int, formatType: Int, offset: Int): XYBluetoothResult<ByteArray> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristicFloat")
        var value: ByteArray? = null
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(mantissa, exponent, formatType, offset)
                val writeResult = writeCharacteristic(characteristicToWrite)
                value = writeResult.value
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristicShort(service: UUID, characteristic: UUID, valueToWrite: UShort, offset: Int): XYBluetoothResult<UShort> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: UShort? = null
        val findResult = findCharacteristic(service, characteristic)
        log.info("findAndWriteCharacteristic: Found")
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            log.info("findAndWriteCharacteristic: $characteristicToWrite")
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite.toInt(), BluetoothGattCharacteristic.FORMAT_UINT16, offset)
                log.info("findAndWriteCharacteristic: Set")
                val writeResult = writeCharacteristic(characteristicToWrite)
                log.info("findAndWriteCharacteristic: Write Complete: $writeResult")
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristicInt(service: UUID, characteristic: UUID, valueToWrite: UInt, offset: Int): XYBluetoothResult<UInt> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: UInt? = null
        val findResult = findCharacteristic(service, characteristic)
        log.info("findAndWriteCharacteristic: Found")
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            log.info("findAndWriteCharacteristic: $characteristicToWrite")
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite.toInt(), BluetoothGattCharacteristic.FORMAT_UINT32, offset)
                log.info("findAndWriteCharacteristic: Set")
                val writeResult = writeCharacteristic(characteristicToWrite)
                log.info("findAndWriteCharacteristic: Write Complete: $writeResult")
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite: String): XYBluetoothResult<String> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: String? = null
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite)
                val writeResult = writeCharacteristic(characteristicToWrite)
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristic(
        service: UUID,
        characteristic: UUID,
        bytes: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ): XYBluetoothResult<ByteArray> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var value: ByteArray? = null
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToWrite != null) {
                characteristicToWrite.value = bytes
                val writeResult = writeCharacteristic(
                        characteristicToWrite = characteristicToWrite,
                        writeType = writeType)
                value = writeResult.value
                error = writeResult.error
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    suspend fun findAndWriteCharacteristicNotify(service: UUID, characteristic: UUID, enable: Boolean): XYBluetoothResult<Boolean> {
        // this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristicNotify")
        var value: Boolean? = null
        val findResult = findCharacteristic(service, characteristic)
        val characteristicToWrite = findResult.value
        var error = findResult.error
        if (error == XYBluetoothResultErrorCode.None) {
            if (characteristicToWrite != null) {
                val descriptor = characteristicToWrite.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val setResult = setCharacteristicNotify(characteristicToWrite, enable)
                value = setResult.value
                error = setResult.error
                if (setResult.error == XYBluetoothResultErrorCode.None) {
                    var retries = 5
                    while (retries > 0) {
                        val writeResult = writeDescriptor(descriptor)
                        error = writeResult.error
                        retries--
                    }
                }
            } else {
                error = XYBluetoothResultErrorCode.NullValue
            }
        }

        if (error != XYBluetoothResultErrorCode.None) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return XYBluetoothResult(value, error)
    }

    companion object {
        val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
