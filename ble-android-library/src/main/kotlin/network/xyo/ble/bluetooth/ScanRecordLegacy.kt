package network.xyo.ble.bluetooth

import android.os.ParcelUuid
import android.util.ArrayMap
import android.util.SparseArray
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*

// This is a copy of ScanRecord from Android Marshmallow+ to be used with Android versions earlier than Marshmallow

@kotlin.ExperimentalUnsignedTypes
@kotlin.ExperimentalStdlibApi
class ScanRecordLegacy private constructor(@param:Nullable @field:Nullable
                                           /**
                                            * Returns a list of service UUIDs within the advertisement that are used to identify the
                                            * bluetooth GATT services.
                                            */
                                           @get:Nullable
                                           val serviceUuids: List<ParcelUuid>,
                                           /**
                                            * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
                                            * data.
                                            */
                                           val manufacturerSpecificData: SparseArray<ByteArray>,
                                           /**
                                            * Returns a map of service UUID and its corresponding service data.
                                            */
                                           val serviceData: Map<ParcelUuid, ByteArray>,
        // Flags of the advertising data.
                                           /**
                                            * Returns the advertising flags indicating the discoverable mode and capability of the device.
                                            * Returns -1 if the flag field is not set.
                                            */
                                           val advertiseFlags: Int, // Transmission power level(in dB).
                                           /**
                                            * Returns the transmission power level of the packet in dBm. Returns [Integer.MIN_VALUE]
                                            * if the field is not set. This value can be used to calculate the path loss of a received
                                            * packet using the following equation:
                                            *
                                            *
                                            * `pathloss = txPowerLevel - rssi`
                                            */
                                           val txPowerLevel: Int,
        // Local name of the Bluetooth LE device.
                                           /**
                                            * Returns the local name of the BLE device. The is a UTF-8 encoded string.
                                            */
                                           @get:Nullable
                                           val deviceName: String, // Raw bytes of scan record.
                                           /**
                                            * Returns raw bytes of scan record.
                                            */
                                           val bytes: ByteArray) {

    /**
     * Returns the manufacturer specific data associated with the manufacturer id. Returns
     * `null` if the `manufacturerId` is not found.
     */
    @Nullable
    fun getManufacturerSpecificData(manufacturerId: Int): ByteArray {
        return manufacturerSpecificData.get(manufacturerId)
    }

    /**
     * Returns the service data byte array associated with the `serviceUuid`. Returns
     * `null` if the `serviceDataUuid` is not found.
     */
    @Nullable
    fun getServiceData(@Nullable serviceDataUuid: ParcelUuid?): ByteArray? {
        return if (serviceDataUuid == null) {
            null
        } else serviceData[serviceDataUuid]
    }

    @NotNull
    override fun toString(): String {
        return ("ScanRecord [mAdvertiseFlags=" + advertiseFlags + ", mServiceUuids=" + serviceUuids
                + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(manufacturerSpecificData)
                + ", mServiceData=" + BluetoothLeUtils.toString(serviceData)
                + ", mTxPowerLevel=" + txPowerLevel + ", mDeviceName=" + deviceName + "]")
    }

    companion object {

        private val TAG = ScanRecordLegacy::class.java.simpleName

        // The following data type values are assigned by Bluetooth SIG.
        // For more add refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
        private val DATA_TYPE_FLAGS = 0x01.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06.toUByte()
        private val DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07.toUByte()
        private val DATA_TYPE_LOCAL_NAME_SHORT = 0x08.toUByte()
        private val DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09.toUByte()
        private val DATA_TYPE_TX_POWER_LEVEL = 0x0A.toUByte()
        private val DATA_TYPE_SERVICE_DATA = 0x16.toUByte()
        private val DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF.toUByte()

        /**
         * Parse scan record bytes to [android.bluetooth.le.ScanRecord].
         *
         *
         * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
         *
         *
         * All numerical multi-byte entities and values shall use little-endian **byte**
         * order.
         *
         * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
         * @hide
         */
        @Nullable
        fun parseFromBytes(@Nullable scanRecord: ByteArray?): ScanRecordLegacy? {
            if (scanRecord == null) {
                return null
            }

            var currentPos = 0
            var advertiseFlag = 0xff.toUByte()
            var serviceUuids: MutableList<ParcelUuid>? = ArrayList()
            var localName: String? = null
            var txPowerLevel = Integer.MIN_VALUE

            val manufacturerData = SparseArray<ByteArray>()
            val serviceData = ArrayMap<ParcelUuid, ByteArray>()

            while (currentPos < scanRecord.size) {
                // length is unsigned int.
                val length = scanRecord[currentPos++].toUByte()
                if (length == 0.toUByte()) {
                    break
                }
                // Note the length includes the length of the field type itself.
                val dataLength = length.toInt() - 1
                // fieldType is unsigned int.
                val fieldType = scanRecord[currentPos++].toUByte()
                when (fieldType) {
                    DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toUByte()
                    DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(scanRecord, currentPos,
                            dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids
                            ?: ArrayList())
                    DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids ?: ArrayList())
                    DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids ?: ArrayList())
                    DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName = String(
                            extractBytes(scanRecord, currentPos, dataLength))
                    DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                    DATA_TYPE_SERVICE_DATA -> {
                        // The first two bytes of the service data are service data UUID in little
                        // endian. The rest bytes are service data.
                        val serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT
                        val serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
                                serviceUuidLength)
                        val serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes)
                        val serviceDataArray = extractBytes(scanRecord,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength)
                        serviceData[serviceDataUuid] = serviceDataArray
                    }
                    DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        val manufacturerId = (scanRecord[currentPos + 1].toUByte().toInt() shl 8) + scanRecord[currentPos].toUByte().toInt()
                        val manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
                                dataLength - 2)
                        manufacturerData.put(manufacturerId, manufacturerDataBytes)
                    }
                    else -> {
                    }
                }// Just ignore, we don't handle such data type.
                currentPos += dataLength
            }

            if (serviceUuids!!.isEmpty()) {
                serviceUuids = null
            }
            return ScanRecordLegacy(serviceUuids?.toList()
                    ?: ArrayList(), manufacturerData, serviceData,
                    advertiseFlag.toInt(), txPowerLevel, localName ?: "", scanRecord)
        }

        // Parse service UUIDs.
        private fun parseServiceUuid(@NotNull scanRecord: ByteArray, currentPos: Int, dataLength: Int,
                                     uuidLength: Int, @NotNull serviceUuids: MutableList<ParcelUuid>): Int {
            var currentPosCounter = currentPos
            var dataLengthCounter = dataLength
            while (dataLengthCounter > 0) {
                val uuidBytes = extractBytes(scanRecord, currentPosCounter,
                        uuidLength)
                serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes))
                dataLengthCounter -= uuidLength
                currentPosCounter += uuidLength
            }
            return currentPosCounter
        }

        // Helper method to extract bytes from byte array.
        @NotNull
        private fun extractBytes(@NotNull scanRecord: ByteArray, start: Int, length: Int): ByteArray {
            val bytes = ByteArray(length)
            System.arraycopy(scanRecord, start, bytes, 0, length)
            return bytes
        }
    }
}
