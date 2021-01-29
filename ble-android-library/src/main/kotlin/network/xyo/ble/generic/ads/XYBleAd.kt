package network.xyo.ble.generic.ads

import java.nio.ByteBuffer
import kotlin.math.pow
import network.xyo.base.XYBase

open class XYBleAd(buffer: ByteBuffer) : XYBase() {

    val size = buffer.get()
    private val type: UByte
    var data: ByteArray? = null

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
