package network.xyo.ble.firmware

import android.os.Environment
import network.xyo.core.XYBase
import network.xyo.core.XYBase.Companion.logInfo
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.experimental.xor


class OtaFile private constructor(private val inputStream: InputStream?) {

    private val crc: Byte
    private val bytes: ByteArray
    private val bytesAvailable: Int = inputStream!!.available()
    private var blocks: Array<Array<ByteArray>?>? = null

    init {
        bytes = ByteArray(bytesAvailable + 1)
        inputStream!!.read(bytes)
        crc = getCrc()
        bytes[bytesAvailable] = crc
        hexStringToTwoByteArray()
    }

    var numberOfBlocks = -1
        private set

    val numberOfBytes: Int
        get() = bytes.size

    fun getBlock(index: Int): Array<ByteArray>? {
        return blocks?.get(index)
    }

    private fun getCrc(): Byte {
        var crcCode: Byte = 0
        for (i in 0 until bytesAvailable) {
            val byteValue = bytes[i]
            val intVal = byteValue.toInt()
            crcCode = crcCode xor intVal.toByte()
        }
        logInfo(TAG, "\"Firmware CRC: $crcCode")
        return crcCode
    }

    fun hexStringToTwoByteArray(): Array<ByteArray> {
        val result = bytes
        val resultLength = result.size
        run {
            var i = 0
            while (i < resultLength-1) {
                result[i shr 1] = ((if (result[i] < 0x3a) result[i] - 0x30 else result[i] - 0x37) * 16 + if (result[i + 1] < 0x3a) result[i + 1] - 0x30 else result[i + 1] - 0x37).toByte()
                i += 2
            }
        }

        XYBase.logExtreme(TAG, "testOta-length of result: " + result.size / 2)
        var slots = resultLength / 32
        if (resultLength % 32 > 0) {
            slots++
        }
        val firmwareByteArray = Array(slots) { ByteArray(20) }

        for (i in 0 until slots) {
            val addr = i * 16
            firmwareByteArray[i][0] = addr.toByte()
            firmwareByteArray[i][1] = addr.ushr(8).toByte()
            firmwareByteArray[i][2] = addr.ushr(16).toByte()
            firmwareByteArray[i][3] = addr.ushr(24).toByte()

            for (j in 0..15) {
                if (i * 16 + j < resultLength) {
                    firmwareByteArray[i][j + 4] = result[i * 16 + j]
                } else {
                    firmwareByteArray[i][j + 4] = 0xff.toByte()
                }
            }
        }
        return firmwareByteArray
    }


    private fun getXorValue(array: ByteArray): Byte {
        var cooked = 0
        for (i in array.indices) {
            cooked = (cooked xor array[i].toInt())
        }
        return cooked.toByte()
    }


    companion object {
        private const val TAG = "OtaFile"
        private val filesDir = Environment.getExternalStorageDirectory().absolutePath + "/xyo"
        private const val chunkSize = 20
        private const val patchLen = 128


        fun fromFile(file: File): OtaFile {
            val inputStream = FileInputStream(file)
            return OtaFile(inputStream)
        }

        fun fromLocalStorage(filename: String, location: String = filesDir): OtaFile {
            val inputStream = FileInputStream("$location/$filename")
            return OtaFile(inputStream)
        }

        fun fromInputStream(inputStream: InputStream?): OtaFile {
            return OtaFile(inputStream)
        }

    }

}