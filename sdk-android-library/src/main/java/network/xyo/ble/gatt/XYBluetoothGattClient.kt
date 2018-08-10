package network.xyo.ble.gatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
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
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: Int? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getIntValue(formatType, offset)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicString(characteristicToRead: BluetoothGattCharacteristic, offset: Int) : Deferred<XYBluetoothResult<String>>{
        return asyncBle {
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: String? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getStringValue(offset)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicFloat(characteristicToRead: BluetoothGattCharacteristic, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<Float>>{
        return asyncBle {
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: Float? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.getFloatValue(formatType, offset)
            }

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun readCharacteristicBytes(characteristicToRead: BluetoothGattCharacteristic) : Deferred<XYBluetoothResult<ByteArray>>{
        return asyncBle {
            val readResult = readCharacteristic(characteristicToRead).await()
            var value: ByteArray? = null
            val error = readResult.error
            if (error == null) {
                value = readResult.value?.value
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

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:Int, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            logInfo("asyncFindAndWriteCharacteristic")
            var error: XYBluetoothError? = null
            var value: Int? = null

            val findResult = findCharacteristic(service, characteristic).await()
            logInfo("asyncFindAndWriteCharacteristic: Found")
            val characteristicToWrite = findResult.value
            if (findResult.error == null) {
                logInfo("asyncFindAndWriteCharacteristic: $characteristicToWrite")
                if (characteristicToWrite != null) {
                    characteristicToWrite.setValue(valueToWrite, formatType, offset)
                    logInfo("asyncFindAndWriteCharacteristic: Set")
                    val writeResult = writeCharacteristic(characteristicToWrite).await()
                    logInfo("asyncFindAndWriteCharacteristic: Write Complete: $writeResult")
                    value = valueToWrite
                    error = writeResult.error
                } else {
                    error = XYBluetoothError("asyncFindAndWriteCharacteristic: Got Null Value")
                }
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristicFloat(service: UUID, characteristic: UUID, mantissa: Int, exponent: Int, formatType:Int, offset:Int) : Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            logInfo("asyncFindAndWriteCharacteristicFloat")
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
                    error = XYBluetoothError("asyncFindAndWriteCharacteristic: Got Null Value")
                }
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, valueToWrite:String) : Deferred<XYBluetoothResult<String>> {
        return asyncBle {
            logInfo("asyncFindAndWriteCharacteristic")
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
                    error = XYBluetoothError("asyncFindAndWriteCharacteristic: Got Null Value")
                }
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristic(service: UUID, characteristic: UUID, bytes:ByteArray) : Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            logInfo("asyncFindAndWriteCharacteristic")
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
                    error = XYBluetoothError("asyncFindAndWriteCharacteristic: Got Null Value")
                }
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }

    fun findAndWriteCharacteristicNotify(service: UUID, characteristic: UUID, enable:Boolean) : Deferred<XYBluetoothResult<Boolean>> {
        return asyncBle {
            logInfo("asyncFindAndWriteCharacteristic")
            var error: XYBluetoothError? = null
            var value: Boolean? = null

            val findResult = findCharacteristic(service, characteristic).await()
            val characteristicToWrite = findResult.value
            if (findResult.error == null) {
                if (characteristicToWrite != null) {
                    if (enable) {
                        characteristicToWrite.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    } else {
                        characteristicToWrite.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    }
                    val writeResult = writeCharacteristic(characteristicToWrite).await()
                    value = enable
                    error = writeResult.error
                } else {
                    error = XYBluetoothError("asyncFindAndWriteCharacteristic: Got Null Value")
                }
            }

            lastAccessTime = now

            return@asyncBle XYBluetoothResult(value, error)
        }
    }
}