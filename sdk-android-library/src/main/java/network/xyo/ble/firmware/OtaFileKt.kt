package network.xyo.ble.firmware

import android.os.Environment
import network.xyo.core.XYBase.Companion.logInfo
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.experimental.xor


class OtaFileKt private constructor(private val inputStream: InputStream?) {

    private val crc: Byte
    private val bytes: ByteArray
    private val bytesAvailable: Int = inputStream!!.available()
    private var blocks: Array<Array<ByteArray>?>? = null

    init {
        bytes = ByteArray(bytesAvailable + 1)
        inputStream!!.read(bytes)
        crc = getCrc()
        bytes[bytesAvailable] = crc
        hexStringToThreeByteArray()
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

    private fun hexStringToThreeByteArray() {
//        val utils = OtaUtils()
//        blocks = utils.initBlocksSuota(bytes)
//        numberOfBlocks = utils.numberOfBlocks
//        XYBase.logInfo(TAG, "\"blocks size: ${blocks?.size}")
    }

    private fun hexStringToThreeByteArray2() {
        val result = bytes
        val resultLength = result.size-1
        run {
            var i = 0
            while (i < resultLength) {
                result[i shr 1] = ((
                        if (result[i] < 0x3a) result[i] - 0x30
                        else result[i] - 0x37) * 16 +
                        if (result[i + 1] < 0x3a) result[i + 1] - 0x30
                        else result[i + 1] - 0x37).toByte()
                i += 2
            }
        }

        val xor = getXorValue(result)
        val xorArray = byteArrayOf(xor)

        val newResult = ByteArray(result.size + 1)
        System.arraycopy(result, 0, newResult, 0, result.size)
        System.arraycopy(xorArray, 0, newResult, result.size, 1)

        val newResultLength = newResult.size

        // each block will contain 240 bytes maximum
        // total number of blocks that will be written
        numberOfBlocks = Math.ceil(newResultLength.toDouble() / patchLen.toDouble()).toInt()

        // need to send all chunks of 20, then chunk of modulus remainder
        blocks = emptyArray()
        var offset = 0

        for (i in 0 until numberOfBlocks) {
            var blockSize = patchLen
            if (i + 1 == numberOfBlocks) {
                blockSize = newResultLength % blockSize
            }

            var chunkCounter = 0
            //firmwareByteArray[i] = emptyArray()
            var j = 0
            while (j < blockSize) {
                var tempChunkSize = chunkSize
                if (offset + chunkSize > newResultLength) {
                    tempChunkSize = newResultLength - offset
                } else if (j + chunkSize > blockSize) {
                    tempChunkSize = blockSize % chunkSize
                }
                logInfo(TAG, "loop1: $i -- $chunkCounter")
                val chunk = result.copyOfRange(offset, offset + tempChunkSize)
                blocks?.let { block ->
                    val fwByteArray = block[i]
                    fwByteArray?.set(chunkCounter, chunk)
                }



                offset += tempChunkSize
                chunkCounter++
                j += chunkSize
            }
        }

        logInfo(TAG, "firmwareByteArray size: ${blocks?.size}")
                //return firmwareByteArray
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


        fun fromFile(file: File): OtaFileKt {
            val inputStream = FileInputStream(file)
            return OtaFileKt(inputStream)
        }

        fun fromLocalStorage(filename: String, location: String = filesDir): OtaFileKt {
            val inputStream = FileInputStream("$location/$filename")
            return OtaFileKt(inputStream)
        }

        fun fromInputStream(inputStream: InputStream?): OtaFileKt {
            return OtaFileKt(inputStream)
        }

    }

}