package network.xyo.ble.gatt

class XYBluetoothResult<T> {

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
        return "XYBluetoothResult: V: ${value}, E: ${error?.message ?: error ?: ""}"
    }
}