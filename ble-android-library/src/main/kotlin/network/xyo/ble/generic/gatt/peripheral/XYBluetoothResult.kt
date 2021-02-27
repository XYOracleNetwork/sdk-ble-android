package network.xyo.ble.generic.gatt.peripheral

import network.xyo.ble.debug
import network.xyo.ble.generic.gatt.peripheral.exceptions.NoGattRuntimeException
import java.lang.RuntimeException

open class XYBluetoothResult<T> {

    var value: T? = null
    private var _error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None
    var error: XYBluetoothResultErrorCode
        get() = _error
        set(value) {
            _error = value
            debug {
                when (error) {
                    XYBluetoothResultErrorCode.NoGatt -> {
                        throw NoGattRuntimeException()
                    }

                    else -> {
                        throw RuntimeException("Bluetooth call failed [${value}]")
                    }
                }
            }
        }

    constructor() {
    }

    constructor(value: T?, error: XYBluetoothResultErrorCode = XYBluetoothResultErrorCode.None) {
        this.value = value
        this.error = error
    }

    constructor(value: T?) {
        this.value = value
        this.error = XYBluetoothResultErrorCode.None
    }

    constructor(error: XYBluetoothResultErrorCode) {
        this.value = null
        this.error = error
    }

    override fun toString(): String {
        return "XYBluetoothResult: V: $value, E: ${error.name}"
    }

    open fun format(): String {
        return (value ?: error.name).toString()
    }

    fun hasError(): Boolean {
        return error != XYBluetoothResultErrorCode.None
    }
}
