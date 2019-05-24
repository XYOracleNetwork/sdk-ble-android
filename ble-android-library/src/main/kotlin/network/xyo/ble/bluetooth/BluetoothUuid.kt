package network.xyo.ble.bluetooth

import android.os.ParcelUuid

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.HashSet
import java.util.UUID

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import unsigned.Ubyte

// This is a copy of BluetoothUuid from Android Marshmallow+ to be used with Android versions earlier than Marshmallow

object BluetoothUuid {

    /* See Bluetooth Assigned Numbers document - SDP section, to getUniqueId the values of UUIDs
     * for the various services.
     *
     * The following 128 bit values are calculated as:
     *  uuid * 2^96 + BASE_UUID
     */
    val AudioSink: ParcelUuid = ParcelUuid.fromString("0000110B-0000-1000-8000-00805F9B34FB")
    val AudioSource: ParcelUuid = ParcelUuid.fromString("0000110A-0000-1000-8000-00805F9B34FB")
    val AdvAudioDist: ParcelUuid = ParcelUuid.fromString("0000110D-0000-1000-8000-00805F9B34FB")
    val HSP: ParcelUuid = ParcelUuid.fromString("00001108-0000-1000-8000-00805F9B34FB")
    val HSP_AG: ParcelUuid = ParcelUuid.fromString("00001112-0000-1000-8000-00805F9B34FB")
    val Handsfree: ParcelUuid = ParcelUuid.fromString("0000111E-0000-1000-8000-00805F9B34FB")
    val Handsfree_AG: ParcelUuid = ParcelUuid.fromString("0000111F-0000-1000-8000-00805F9B34FB")
    val AvrcpController: ParcelUuid = ParcelUuid.fromString("0000110E-0000-1000-8000-00805F9B34FB")
    val AvrcpTarget: ParcelUuid = ParcelUuid.fromString("0000110C-0000-1000-8000-00805F9B34FB")
    val ObexObjectPush: ParcelUuid = ParcelUuid.fromString("00001105-0000-1000-8000-00805f9b34fb")
    val Hid: ParcelUuid = ParcelUuid.fromString("00001124-0000-1000-8000-00805f9b34fb")
    val Hogp: ParcelUuid = ParcelUuid.fromString("00001812-0000-1000-8000-00805f9b34fb")
    val PANU: ParcelUuid = ParcelUuid.fromString("00001115-0000-1000-8000-00805F9B34FB")
    val NAP: ParcelUuid = ParcelUuid.fromString("00001116-0000-1000-8000-00805F9B34FB")
    val BNEP: ParcelUuid = ParcelUuid.fromString("0000000f-0000-1000-8000-00805F9B34FB")
    val PBAP_PCE: ParcelUuid = ParcelUuid.fromString("0000112e-0000-1000-8000-00805F9B34FB")
    val PBAP_PSE: ParcelUuid = ParcelUuid.fromString("0000112f-0000-1000-8000-00805F9B34FB")
    val MAP: ParcelUuid = ParcelUuid.fromString("00001134-0000-1000-8000-00805F9B34FB")
    val MNS: ParcelUuid = ParcelUuid.fromString("00001133-0000-1000-8000-00805F9B34FB")
    val MAS: ParcelUuid = ParcelUuid.fromString("00001132-0000-1000-8000-00805F9B34FB")
    val SAP: ParcelUuid = ParcelUuid.fromString("0000112D-0000-1000-8000-00805F9B34FB")

    val BASE_UUID: ParcelUuid = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")

    /**
     * Length of bytes for 16 bit UUID
     */
    const val UUID_BYTES_16_BIT = 2
    /**
     * Length of bytes for 32 bit UUID
     */
    const val UUID_BYTES_32_BIT = 4
    /**
     * Length of bytes for 128 bit UUID
     */
    const val UUID_BYTES_128_BIT = 16

    val RESERVED_UUIDS = arrayOf(AudioSink, AudioSource, AdvAudioDist, HSP, Handsfree, AvrcpController, AvrcpTarget, ObexObjectPush, PANU, NAP, MAP, MNS, MAS, SAP)

    fun isAudioSource(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == AudioSource
    }

    fun isAudioSink(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == AudioSink
    }

    fun isAdvAudioDist(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == AdvAudioDist
    }

    fun isHandsfree(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == Handsfree
    }

    fun isHeadset(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == HSP
    }

    fun isAvrcpController(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == AvrcpController
    }

    fun isAvrcpTarget(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == AvrcpTarget
    }

    fun isInputDevice(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == Hid
    }

    fun isPanu(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == PANU
    }

    fun isNap(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == NAP
    }

    fun isBnep(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == BNEP
    }

    fun isMap(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == MAP
    }

    fun isMns(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == MNS
    }

    fun isMas(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == MAS
    }

    fun isSap(@NotNull uuid: ParcelUuid): Boolean {
        return uuid == SAP
    }

    /**
     * Returns true if ParcelUuid is present in uuidArray
     *
     * @param uuidArray - Array of ParcelUuids
     * @param uuid - UUID
     */
    fun isUuidPresent(@Nullable uuidArray: Array<ParcelUuid>?, @Nullable uuid: ParcelUuid?): Boolean {
        if ((uuidArray == null || uuidArray.isEmpty()) && uuid == null)
            return true

        if (uuidArray == null)
            return false

        for (element in uuidArray) {
            if (element == uuid) return true
        }
        return false
    }

    /**
     * Returns true if there any common ParcelUuids in uuidA and uuidB.
     *
     * @param uuidA - List of ParcelUuids
     * @param uuidB - List of ParcelUuids
     */
    fun containsAnyUuid(@Nullable uuidA: Array<ParcelUuid>?, @Nullable uuidB: Array<ParcelUuid>?): Boolean {
        if (uuidA == null && uuidB == null) return true

        if (uuidA == null) {
            return uuidB!!.isEmpty()
        }

        if (uuidB == null) {
            return uuidA.isEmpty()
        }

        val uuidSet = HashSet(Arrays.asList(*uuidA))
        for (uuid in uuidB) {
            if (uuidSet.contains(uuid)) return true
        }
        return false
    }

    /**
     * Returns true if all the ParcelUuids in ParcelUuidB are present in
     * ParcelUuidA
     *
     * @param uuidA - Array of ParcelUuidsA
     * @param uuidB - Array of ParcelUuidsB
     */
    fun containsAllUuids(@Nullable uuidA: Array<ParcelUuid>?, @Nullable uuidB: Array<ParcelUuid>?): Boolean {
        if (uuidA == null && uuidB == null) return true

        if (uuidA == null) {
            return uuidB!!.isEmpty()
        }

        if (uuidB == null) return true

        val uuidSet = HashSet(Arrays.asList(*uuidA))
        for (uuid in uuidB) {
            if (!uuidSet.contains(uuid)) return false
        }
        return true
    }

    /**
     * Extract the Service Identifier or the actual uuid from the Parcel Uuid.
     * For example, if 0000110B-0000-1000-8000-00805F9B34FB is the parcel Uuid,
     * this function will return 110B
     *
     * @param parcelUuid - UUID
     * @return the service identifier.
     */
    fun getServiceIdentifierFromParcelUuid(@NotNull parcelUuid: ParcelUuid): Int {
        val uuid = parcelUuid.uuid
        val value = (uuid.mostSignificantBits and 0x0000FFFF00000000L).ushr(32)
        return value.toInt()
    }

    /**
     * Parse UUID from bytes. The `uuidBytes` can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return [ParcelUuid] parsed from bytes.
     * @throws IllegalArgumentException If the `uuidBytes` cannot be parsed.
     */
    @NotNull
    fun parseUuidFrom(@Nullable uuidBytes: ByteArray?): ParcelUuid {
        if (uuidBytes == null) {
            throw IllegalArgumentException("uuidBytes cannot be null")
        }
        val length = uuidBytes.size
        if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT &&
                length != UUID_BYTES_128_BIT) {
            throw IllegalArgumentException("uuidBytes length invalid - $length")
        }

        // Construct a 128 bit UUID.
        if (length == UUID_BYTES_128_BIT) {
            val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
            val msb = buf.getLong(8)
            val lsb = buf.getLong(0)
            return ParcelUuid(UUID(msb, lsb))
        }

        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        var shortUuid: Long
        if (length == UUID_BYTES_16_BIT) {
            shortUuid = Ubyte(uuidBytes[0]).toLong()
            shortUuid += (Ubyte(uuidBytes[1]) shl 8).toLong()
        } else {
            shortUuid = Ubyte(uuidBytes[0]).toLong()
            shortUuid += (Ubyte(uuidBytes[1]) shl 8).toLong()
            shortUuid += (Ubyte(uuidBytes[2]) shl 16).toLong()
            shortUuid += (Ubyte(uuidBytes[3]) shl 24).toLong()
        }
        val msb = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
        val lsb = BASE_UUID.uuid.leastSignificantBits
        return ParcelUuid(UUID(msb, lsb))
    }

    /**
     * Parse UUID to bytes. The returned value is shortest representation, a 16-bit, 32-bit or 128-bit UUID,
     * Note returned value is little endian (Bluetooth).
     *
     * @param uuid uuid to parse.
     * @return shortest representation of `uuid` as bytes.
     * @throws IllegalArgumentException If the `uuid` is null.
     */
    @NotNull
    fun uuidToBytes(@Nullable uuid: ParcelUuid?): ByteArray {
        if (uuid == null) {
            throw IllegalArgumentException("uuid cannot be null")
        }

        if (is16BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_16_BIT)
            val uuidVal = getServiceIdentifierFromParcelUuid(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            return uuidBytes
        }

        if (is32BitUuid(uuid)) {
            val uuidBytes = ByteArray(UUID_BYTES_32_BIT)
            val uuidVal = getServiceIdentifierFromParcelUuid(uuid)
            uuidBytes[0] = (uuidVal and 0xFF).toByte()
            uuidBytes[1] = (uuidVal and 0xFF00 shr 8).toByte()
            uuidBytes[2] = (uuidVal and 0xFF0000 shr 16).toByte()
            uuidBytes[3] = (uuidVal and -0x1000000 shr 24).toByte()
            return uuidBytes
        }

        // Construct a 128 bit UUID.
        val msb = uuid.uuid.mostSignificantBits
        val lsb = uuid.uuid.leastSignificantBits

        val uuidBytes = ByteArray(UUID_BYTES_128_BIT)
        val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
        buf.putLong(8, msb)
        buf.putLong(0, lsb)
        return uuidBytes
    }

    /**
     * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
     *
     * @param parcelUuid - UUID
     * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
     */
    fun is16BitUuid(@NotNull parcelUuid: ParcelUuid): Boolean {
        val uuid = parcelUuid.uuid
        return uuid.leastSignificantBits == BASE_UUID.uuid.leastSignificantBits && uuid.mostSignificantBits and -0xffff00000001L == 0x1000L
    }


    /**
     * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
     *
     * @param parcelUuid - UUID
     * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
     */
    fun is32BitUuid(@NotNull parcelUuid: ParcelUuid): Boolean {
        val uuid = parcelUuid.uuid
        return uuid.leastSignificantBits == BASE_UUID.uuid.leastSignificantBits && !is16BitUuid(parcelUuid) && uuid.mostSignificantBits and 0xFFFFFFFFL == 0x1000L
    }
}
