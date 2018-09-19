package network.xyo.ble.gatt

class XYGattStatus (statusCode: Int) {
    val message = gattStatusCodeToString(statusCode)

    override fun toString(): String {
        return message
    }

    companion object {
        fun gattStatusCodeToString (statusCode : Int) : String {
            when (statusCode) {
                0x00 -> return "BLE_HCI_STATUS_CODE_SUCCESS"
                0x01 -> return "BLE_HCI_STATUS_CODE_UNKNOWN_BTLE_COMMAND"
                0x02 -> return "BLE_HCI_STATUS_CODE_UNKNOWN_CONNECTION_IDENTIFIER"
                0x05 -> return "BLE_HCI_AUTHENTICATION_FAILURE"
                0x06 -> return "BLE_HCI_STATUS_CODE_PIN_OR_KEY_MISSING"
                0x07 -> return "BLE_HCI_MEMORY_CAPACITY_EXCEEDED"
                0x08 -> return "BLE_HCI_CONNECTION_TIMEOUT"
                0x0c -> return "BLE_HCI_STATUS_CODE_COMMAND_DISALLOWED"
                0x12 -> return "BLE_HCI_STATUS_CODE_INVALID_BTLE_COMMAND_PARAMETERS"
                0x13 -> return "BLE_HCI_REMOTE_USER_TERMINATED_CONNECTION"
                0x14 -> return "BLE_HCI_REMOTE_DEV_TERMINATION_DUE_TO_LOW_RESOURCES"
                0x15 -> return "BLE_HCI_REMOTE_DEV_TERMINATION_DUE_TO_POWER_OFF"
                0x16 -> return "BLE_HCI_LOCAL_HOST_TERMINATED_CONNECTION"
                0x1a -> return "BLE_HCI_UNSUPPORTED_REMOTE_FEATURE"
                0x1e -> return "BLE_HCI_STATUS_CODE_INVALID_LMP_PARAMETERS"
                0x1f -> return "BLE_HCI_STATUS_CODE_UNSPECIFIED_ERROR"
                0x22 -> return "BLE_HCI_STATUS_CODE_LMP_RESPONSE_TIMEOUT"
                0x24 -> return "BLE_HCI_STATUS_CODE_LMP_PDU_NOT_ALLOWED"
                0x28 -> return "BLE_HCI_INSTANT_PASSED"
                0x29 -> return "BLE_HCI_PAIRING_WITH_UNIT_KEY_UNSUPPORTED"
                0x2a -> return "BLE_HCI_DIFFERENT_TRANSACTION_COLLISION"
                0x3a -> return "BLE_HCI_CONTROLLER_BUSY"
                0x3b -> return "BLE_HCI_CONN_INTERVAL_UNACCEPTABLE"
                0x3c -> return "BLE_HCI_DIRECTED_ADVERTISER_TIMEOUT"
                0x3d -> return "BLE_HCI_CONN_TERMINATED_DUE_TO_MIC_FAILURE"
                0x3e -> return "BLE_HCI_CONN_FAILED_TO_BE_ESTABLISHED"
                0x80 -> return "GATT_NO_RESSOURCES"
                0x81 -> return "GATT_INTERNAL_ERROR"
                0x82 -> return "GATT_WRONG_STATE"
                0x83 -> return "GATT_DB_FULL"
                0x84 -> return "GATT_BUSY"
                0x85 -> return "GATT_ERROR"
                0x87 -> return "GATT_ILLEGAL_PARAMETER"
                0x89 -> return "GATT_AUTH_FAIL"
                else -> return "UNKNOWN ERROR"
            }
        }
    }
}