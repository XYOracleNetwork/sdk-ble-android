package network.xyo.ble.gatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.os.Handler
import kotlinx.coroutines.experimental.Deferred
import java.util.*

open class XYBluetoothGattClient protected constructor(
        context:Context,
        device: BluetoothDevice?,
        autoConnect: Boolean,
        callback: XYBluetoothGattCallback?,
        transport: Int?,
        phy: Int?,
        handler: Handler?
) : XYBluetoothGatt(context, device, autoConnect, callback, transport, phy, handler) {

    fun updateBluetoothDevice(device: BluetoothDevice?) {
        this.device = device
        lastAdTime = now
    }

    fun readCharacteristicInt(characteristicToRead: BluetoothGattCharacteristic, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<Int>>{
        return asyncBle {
            logInfo("readCharacteristicInt")
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: Int? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getIntValue(formatType, offset)
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicString(characteristicToRead: BluetoothGattCharacteristic, offset: Int) : Deferred<XYBluetoothResult<String>>{
        return asyncBle {
            logInfo("readCharacteristicString")
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: String? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getStringValue(offset)
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicFloat(characteristicToRead: BluetoothGattCharacteristic, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<Float>>{
        return asyncBle {
            logInfo("readCharacteristicFloat")
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: Float? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getFloatValue(formatType, offset)
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicBytes(characteristicToRead: BluetoothGattCharacteristic) : Deferred<XYBluetoothResult<ByteArray>>{
        return asyncBle {
            logInfo("readCharacteristicBytes")
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: ByteArray? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.value
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndReadCharacteristicInt(service: UUID, characteristic: UUID, formatType:Int, offset:Int = 0) : Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            logInfo("findAndReadCharacteristicInt")
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
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndReadCharacteristicFloat(service: UUID, characteristic: UUID, formatType:Int, offset:Int = 0) : Deferred<XYBluetoothResult<Float>> {
        return asyncBle {
            logInfo("findAndReadCharacteristicFloat")
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
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndReadCharacteristicString(service: UUID, characteristic: UUID, offset:Int = 0) : Deferred<XYBluetoothResult<String>> {
        return asyncBle {
            logInfo("findAndReadCharacteristicString")
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
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndReadCharacteristicBytes(service: UUID, characteristic: UUID) : Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            logInfo("findAndReadCharacteristicString")
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
                logError(error.toString(), false)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:Int, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            logInfo("findAndWriteCharacteristic")
            var error: XYBluetoothError? = null
            var value: Int? = null

            val findResult = findCharacteristic(service, characteristic).await()
            logInfo("findAndWriteCharacteristic: Found")
            val characteristicToWrite = findResult.value
            if (findResult.error == null) {
                logInfo("findAndWriteCharacteristic: $characteristicToWrite")
                if (characteristicToWrite != null) {
                    characteristicToWrite.setValue(valueToWrite, formatType, offset)
                    logInfo("findAndWriteCharacteristic: Set")
                    val writeResult = writeCharacteristic(characteristicToWrite).await()
                    logInfo("findAndWriteCharacteristic: Write Complete: $writeResult")
                    value = valueToWrite
                    error = writeResult.error
                } else {
                    error = XYBluetoothError("findAndWriteCharacteristic: Got Null Value")
                }
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristicFloat(service: UUID, characteristic: UUID, mantissa: Int, exponent: Int, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            logInfo("findAndWriteCharacteristicFloat")
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
                logError(error.toString(), false)
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:String) : Deferred<XYBluetoothResult<String>> {
        return asyncBle {
            logInfo("findAndWriteCharacteristic")
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
                logError(error.toString(), false)
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, bytes:ByteArray) : Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            logInfo("findAndWriteCharacteristic")
            var error: XYBluetoothError? = null
            var value: ByteArray? = null

            val findResult = findCharacteristic(service, characteristic).await()
            val characteristicToWrite = findResult.value
            if (findResult.error == null) {
                if (characteristicToWrite != null) {
                    characteristicToWrite.setValue(bytes)
                    val writeResult = writeCharacteristic(characteristicToWrite).await()
                    value = writeResult.value
                    error = writeResult.error
                } else {
                    error = XYBluetoothError("findAndWriteCharacteristic: Got Null Value")
                }
            }

            if (error != null) {
                logError(error.toString(), false)
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    fun findAndWriteCharacteristicNotify(service: UUID, characteristic: UUID, enable:Boolean) : Deferred<XYBluetoothResult<Boolean>> {
        return asyncBle {
            logInfo("findAndWriteCharacteristicNotify")
            var error: XYBluetoothError? = null
            var value: Boolean? = null

            val findResult = findCharacteristic(service, characteristic).await()
            val characteristicToWrite = findResult.value
            if (findResult.error == null) {
                if (characteristicToWrite != null) {
                    val descriptor = characteristicToWrite.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val setResult = setCharacteristicNotify(characteristicToWrite, enable)
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
                logError(error.toString(), false)
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }
}