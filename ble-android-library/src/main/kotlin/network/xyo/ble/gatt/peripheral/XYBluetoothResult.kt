package network.xyo.ble.gatt.peripheral

//we have the interface here to allow for returning any generic implementation
interface IXYBluetoothResult

open class XYBluetoothResult<T>: IXYBluetoothResult {

    var value: T? = null
    var error: XYBluetoothError? = null

    constructor(value: T?, error: XYBluetoothError?) {
        this.value = value
        this.error = error
    }

    constructor(value: T?) {
        this.value = value
        this.error = null
    }

    constructor(error: XYBluetoothError) {
        this.value = null
        this.error = error
    }


    override fun toString(): String {
        return "XYBluetoothResult: V: $value, E: ${error?.message ?: error ?: ""}"
    }

    open fun format(): String {
        return (value ?: error?.message ?: "Error").toString()
    }

    fun hasError(): Boolean {
        return error?.message != null
    }

}