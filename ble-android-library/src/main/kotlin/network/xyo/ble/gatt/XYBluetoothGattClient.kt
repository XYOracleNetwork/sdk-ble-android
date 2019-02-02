package network.xyo.ble.gatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Handler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.ble.gatt.peripheral.*
import unsigned.toBigInt
import java.util.*
import kotlin.math.absoluteValue

open class XYBluetoothGattClient protected constructor(
        context:Context,
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

    fun readCharacteristicInt(characteristicToRead: BluetoothGattCharacteristic, formatType:Int, offset:Int) = GlobalScope.async {
        log.info("readCharacteristicInt")
        val readResult = readCharacteristic(characteristicToRead).await()
        var value: Int? = null
        val error = readResult.error
        if (error == null) {
            value = readResult.value?.getIntValue(formatType, offset)
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun readCharacteristicString(characteristicToRead: BluetoothGattCharacteristic, offset: Int) = GlobalScope.async {
        log.info("readCharacteristicString")
        val readResult = readCharacteristic(characteristicToRead).await()
        var value: String? = null
        val error = readResult.error
        if (error == null) {
            value = readResult.value?.getStringValue(offset)
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun readCharacteristicFloat(characteristicToRead: BluetoothGattCharacteristic, formatType:Int, offset:Int) = GlobalScope.async {
        log.info("readCharacteristicFloat")
        val readResult = readCharacteristic(characteristicToRead).await()
        var value: Float? = null
        val error = readResult.error
        if (error == null) {
            value = readResult.value?.getFloatValue(formatType, offset)
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun readCharacteristicBytes(characteristicToRead: BluetoothGattCharacteristic) = GlobalScope.async {
        log.info("readCharacteristicBytes")
        val readResult = readCharacteristic(characteristicToRead).await()
        var value: ByteArray? = null
        val error = readResult.error
        if (error == null) {
            value = readResult.value?.value
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun findAndReadCharacteristicInt(service: UUID, characteristic: UUID, formatType:Int, offset:Int = 0) = GlobalScope.async {
        log.info("findAndReadCharacteristicInt")
        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToRead = findResult.value
        var value: Int? = null
        var error = findResult.error
        if (error == null) {
            if (characteristicToRead == null) {
                error = XYBluetoothError("Null Value")
            } else {
                val readResult = readCharacteristicInt(characteristicToRead, formatType, offset).await()
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun findAndReadCharacteristicFloat(service: UUID, characteristic: UUID, formatType:Int, offset:Int = 0) = GlobalScope.async {
        log.info("findAndReadCharacteristicFloat")
        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToRead = findResult.value
        var value: Float? = null
        var error = findResult.error
        if (error == null) {
            if (characteristicToRead == null) {
                error = XYBluetoothError("Null Value")
            } else {
                val readResult = readCharacteristicFloat(characteristicToRead, formatType, offset).await()
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun findAndReadCharacteristicString(service: UUID, characteristic: UUID, offset:Int = 0) = GlobalScope.async {
        log.info("findAndReadCharacteristicString")
        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToRead = findResult.value
        var value: String? = null
        var error = findResult.error
        if (error == null) {
            if (characteristicToRead == null) {
                error = XYBluetoothError("Null Value")
            } else {
                val readResult = readCharacteristicString(characteristicToRead, offset).await()
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun findAndReadCharacteristicBytes(service: UUID, characteristic: UUID) = GlobalScope.async {
        log.info("findAndReadCharacteristicString")
        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToRead = findResult.value
        var value: ByteArray? = null
        var error = findResult.error
        if (error == null) {
            if (characteristicToRead == null) {
                error = XYBluetoothError("Null Value")
            } else {
                val readResult = readCharacteristicBytes(characteristicToRead).await()
                error = readResult.error
                value = readResult.value
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        return@async XYBluetoothResult(value, error)
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:Int, formatType:Int, offset:Int) = GlobalScope.async {
        //this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var error: XYBluetoothError? = null
        var value: Int? = null

        val findResult = findCharacteristic(service, characteristic).await()
        log.info("findAndWriteCharacteristic: Found")
        val characteristicToWrite = findResult.value
        if (findResult.error == null) {
            log.info("findAndWriteCharacteristic: $characteristicToWrite")
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite, formatType, offset)
                log.info("findAndWriteCharacteristic: Set")
                val writeResult = writeCharacteristic(characteristicToWrite).await()
                log.info("findAndWriteCharacteristic: Write Complete: $writeResult")
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothError("findAndWriteCharacteristic: Got Null Value")
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return@async XYBluetoothResult(value, error)
    }

    fun findAndWriteCharacteristicFloat(service: UUID, characteristic: UUID, mantissa: Int, exponent: Int, formatType:Int, offset:Int) = GlobalScope.async {
        //this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristicFloat")
        var error: XYBluetoothError? = null
        var value: ByteArray? = null

        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToWrite = findResult.value
        if (findResult.error == null) {
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(mantissa, exponent, formatType, offset)
                val writeResult = writeCharacteristic(characteristicToWrite).await()
                value = writeResult.value
                error = writeResult.error
            } else {
                error = XYBluetoothError("findAndWriteCharacteristicFloat: Got Null Value")
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return@async XYBluetoothResult(value, error)
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:String) = GlobalScope.async {
        //this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var error: XYBluetoothError? = null
        var value: String? = null

        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToWrite = findResult.value
        if (findResult.error == null) {
            if (characteristicToWrite != null) {
                characteristicToWrite.setValue(valueToWrite)
                val writeResult = writeCharacteristic(characteristicToWrite).await()
                value = valueToWrite
                error = writeResult.error
            } else {
                error = XYBluetoothError("findAndWriteCharacteristic: Got Null Value")
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return@async XYBluetoothResult(value, error)
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, bytes:ByteArray) = GlobalScope.async {
        //this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristic")
        var error: XYBluetoothError? = null
        var value: ByteArray? = null

        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToWrite = findResult.value
        if (findResult.error == null) {
            if (characteristicToWrite != null) {
                characteristicToWrite.value = bytes
                val writeResult = writeCharacteristic(characteristicToWrite).await()
                value = writeResult.value
                error = writeResult.error
            } else {
                error = XYBluetoothError("findAndWriteCharacteristic: Got Null Value")
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return@async XYBluetoothResult(value, error)
    }


    fun findAndWriteCharacteristicNotify(service: UUID, characteristic: UUID, enable:Boolean) = GlobalScope.async {
        //this prevents a queued close from closing while we run
        lastAccessTime = now

        log.info("findAndWriteCharacteristicNotify")
        var error: XYBluetoothError? = null
        var value: Boolean? = null

        val findResult = findCharacteristic(service, characteristic).await()
        val characteristicToWrite = findResult.value
        if (findResult.error == null) {
            if (characteristicToWrite != null) {
                val descriptor = characteristicToWrite.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                val setResult = setCharacteristicNotify(characteristicToWrite, enable).await()
                value = setResult.value
                error = setResult.error
                if (setResult.error == null) {
                    var retries = 5
                    while (retries > 0) {
                        val writeResult = writeDescriptor(descriptor).await()
                        error = writeResult.error
                        retries--
                    }
                }
            } else {
                error = XYBluetoothError("findAndWriteCharacteristicNotify: Got Null Value")
            }
        }

        if (error != null) {
            log.error(error.toString(), false)
        }

        lastAccessTime = now

        return@async XYBluetoothResult(value, error)
    }


    companion object {
        val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}