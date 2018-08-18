package network.xyo.ble.gatt

import network.xyo.core.XYBase


open class XYBluetoothError(message: String) : Error(message) {

    init {
        XYBase.logError(tag, message, false)
    }

    val tag: String
        get() {
            val parts = this.javaClass.kotlin.simpleName?.split('.') ?: return "Unknown"
            return parts[parts.lastIndex]
        }

    override fun toString(): String {
        return "$tag: ${super.toString()}"
    }
}