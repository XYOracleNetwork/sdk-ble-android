package network.xyo.ble.generic.ads

import java.nio.ByteBuffer
import kotlin.math.pow
import network.xyo.base.XYBase

open class XYBleAd(buffer: ByteBuffer) : XYBase() {

    val size = buffer.get()
    val type: UByte
    var data: ByteArray? = null

    enum class AdTypes(val id: UByte) {
        Flags(0x01U),
        Incomplete16BitServiceUuids(0x02U),
        Complete16BitServiceUuids(0x03U),
        Incomplete32BitServiceUuids(0x04U),
        Complete32BitServiceUuids(0x05U),
        Incomplete128BitServiceUuids(0x06U),
        Complete128BitServiceUuids(0x07U),
        ShortenedLocalName(0x08U),
        CompleteLocalName(0x09U),
        TxPowerLevel(0x0aU),
        ClassOfDevice(0x0dU),
        SimplePairingHashC(0x0eU),
        SimplePairingHashC192(0x0eU),
        SimpleParingRandomizerR(0x0fU),
        SimpleParingRandomizerR192(0x0fU),
        DeviceId(0x10U),
        SecurityManagerTkValue(0x10U),
        SecurityManagerOutOfBandFlags(0x11U),
        SlaveConnectionIntervalRange(0x12U),
        ListOf16BitServiceSolicitationUuids(0x14U),
        ListOf128BitServiceSolicitationUuids(0x15U),
        ServiceData(0x16U),
        ServiceData16BitUuid(0x16U),
        PublicTargetAddress(0x17U),
        RandomTargetAddress(0x18U),
        Appearance(0x19U),
        AdvertisingInterval(0x1aU),
        LeBluetoothDeviceAddress(0x1bU),
        LeRole(0x1cU),
        SimpleParingHashC256(0x1dU),
        SimpleParingRandomizerR256(0x1eU),
        ListOf32BitServiceSolicitationUuids(0x1fU),
        ServiceData32BitUuid(0x20U),
        ServiceData128BitUuid(0x21U),
        LeSecureConnectionsConfirmationValue(0x22U),
        LeSecureConnectionsRandomValue(0x23U),
        Uri(0x24U),
        IndoorPositioning(0x25U),
        TransportDiscoveryData(0x26U),
        LeSupportedFeatures(0x27U),
        ChannelMapUpdateIndication(0x28U),
        PbAdv(0x29U),
        MeshMessage(0x2aU),
        MeshBeacon(0x2bU),
        ThreeDInformationData(0x3dU),
        ManufacturerSpecificData(0xffU)
    }

    init {
        if (size > 0) {
            type = buffer.get().toUByte()
            if (size > 0) {
                data = ByteArray(size - 1)
                buffer.get(data!!, 0, size - 1)
            } else {
                // if size is zero, we hit the end
                while (buffer.hasRemaining()) {
                    buffer.get()
                }
            }
        } else {
            type = 0U
        }
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }

    override fun hashCode(): Int {
        val p = 16777619
        var hash = 216613626

        hash = (hash.toDouble().pow(size.toDouble()) * p).toInt()
        hash += hash.shl(13)
        hash = hash.xor(hash.shr(7))
        hash += hash.shl(3)
        hash = hash.xor(hash.shr(17))
        hash += hash.shl(5)

        hash = (hash.toDouble().pow(type.toDouble()) * p).toInt()
        hash += hash.shl(13)
        hash = hash.xor(hash.shr(7))
        hash += hash.shl(3)
        hash = hash.xor(hash.shr(17))
        hash += hash.shl(5)

        if (data != null) {

            for (byte in data!!) {
                hash = (hash.toDouble().pow(byte.toDouble()) * p).toInt()
                hash += hash.shl(13)
                hash = hash.xor(hash.shr(7))
                hash += hash.shl(3)
                hash = hash.xor(hash.shr(17))
                hash += hash.shl(5)
            }
        }
        return hash
    }

    override fun toString(): String {
        return "Type: $type, Bytes: ${data?.contentToString()}"
    }
}
