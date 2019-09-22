package network.xyo.ble.ads

import network.xyo.base.XYBase
import java.nio.ByteBuffer
import kotlin.math.pow

open class XYBleAd(buffer: ByteBuffer) : XYBase() {

    val size = buffer.get()
    val type: Byte
    var data: ByteArray? = null

    enum class AdTypes(val id: Short) {
        Flags(0x01),
        Incomplete16BitServiceUuids(0x02),
        Complete16BitServiceUuids(0x03),
        Incomplete32BitServiceUuids(0x04),
        Complete32BitServiceUuids(0x05),
        Incomplete128BitServiceUuids(0x06),
        Complete128BitServiceUuids(0x07),
        ShortenedLocalName(0x08),
        CompleteLocalName(0x09),
        TxPowerLevel(0x0a),
        ClassOfDevice(0x0d),
        SimplePairingHashC(0x0e),
        SimplePairingHashC192(0x0e),
        SimpleParingRandomizerR(0x0f),
        SimpleParingRandomizerR192(0x0f),
        DeviceId(0x10),
        SecurityManagerTkValue(0x10),
        SecurityManagerOutOfBandFlags(0x11),
        SlaveConnectionIntervalRange(0x12),
        ListOf16BitServiceSolicitationUuids(0x14),
        ListOf128BitServiceSolicitationUuids(0x15),
        ServiceData(0x16),
        ServiceData16BitUuid(0x16),
        PublicTargetAddress(0x17),
        RandomTargetAddress(0x18),
        Appearance(0x19),
        AdvertisingInterval(0x1a),
        LeBluetoothDeviceAddress(0x1b),
        LeRole(0x1c),
        SimpleParingHashC256(0x1d),
        SimpleParingRandomizerR256(0x1e),
        ListOf32BitServiceSolicitationUuids(0x1f),
        ServiceData32BitUuid(0x20),
        ServiceData128BitUuid(0x21),
        LeSecureConnectionsConfirmationValue(0x22),
        LeSecureConnectionsRandomValue(0x23),
        Uri(0x24),
        IndoorPositioning(0x25),
        TransportDiscoveryData(0x26),
        LeSupportedFeatures(0x27),
        ChannelMapUpdateIndication(0x28),
        PbAdv(0x29),
        MeshMessage(0x2a),
        MeshBeacon(0x2b),
        ThreeDInformationData(0x3d),
        ManufacturerSpecificData(0xff)
    }

    init {
        if (size > 0) {
            type = buffer.get()
            if (size > 0) {
                data = ByteArray(size - 1)
                buffer.get(data!!, 0, size - 1)
            } else {
                //if size is zero, we hit the end
                while (buffer.hasRemaining()) {
                    buffer.get()
                }
            }
        } else {
            type = 0
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