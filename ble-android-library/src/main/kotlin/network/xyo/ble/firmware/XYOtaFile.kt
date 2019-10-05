package network.xyo.ble.firmware

import android.util.Log
import network.xyo.base.XYBase
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.math.max

/**
 * File format for Over-the-air images
 */

class XYOtaFile(private val inputStream: InputStream?) : XYBase() {

    private var bytes: ByteArray? = null
    private var blocks: Array<Array<ByteArray>>? = null
    private val bytesAvailable: Int = inputStream!!.available()

    private var crc: UByte = 0U

    var fileBlockSize = 128
        private set

    private var fileChunkSize = 20

    var numberOfBlocks = 0
        private set

    var chunksPerBlockCount = 0
        private set
    var totalChunkCount = 0
        private set

    val numberOfBytes: Int
        get() = bytes!!.size

    init {
        bytes = ByteArray(bytesAvailable + 1)
        inputStream!!.read(bytes!!)
        crc = calculateCrc()
        bytes!![bytesAvailable] = crc.toByte()

        //Default block/chunk sizes for XY4 devices.
        setFileBlockSize(128, 20)
    }

    // Set the file blockSize and ChunkSize if not using the default 128/20 values
    private fun setFileBlockSize(blockSize: Int, chunkSize: Int) {
        fileBlockSize = max(blockSize, chunkSize)
        fileChunkSize = chunkSize
        chunksPerBlockCount = fileBlockSize / fileChunkSize + if (fileBlockSize.rem(fileChunkSize) != 0) 1 else 0
        numberOfBlocks = bytes!!.size / fileBlockSize + if (bytes!!.size.rem(fileBlockSize) != 0) 1 else 0
        initBlocksSuota()
    }

    // override arrayOfNulls to let us specify the size
    private inline fun <reified T> fileEmptyArray(size: Int): Array<T> =
            @Suppress("UNCHECKED_CAST")
            (arrayOfNulls<T>(size) as Array<T>)


    //Chunk the XYOtaFile into correct block sizes.
    private fun initBlocksSuota() {
        totalChunkCount = 0
        blocks = fileEmptyArray(numberOfBlocks)

        log.info("XYOtaFile", "initBlocksSuota numberOfBlocks: $numberOfBlocks")

        var byteOffset = 0
        // Loop through all the bytes and split them into pieces the size of the default chunk size
        for (i in 0 until numberOfBlocks) {
            var blockSize = fileBlockSize
            var numberOfChunksInBlock = chunksPerBlockCount
            // Check if the last block needs to be smaller
            if (byteOffset + fileBlockSize > bytes!!.size) {
                blockSize = bytes!!.size.rem(fileBlockSize)
                numberOfChunksInBlock = blockSize / fileChunkSize + if (blockSize.rem(fileChunkSize) != 0) 1 else 0
            }
            var chunkNumber = 0
            blocks!![i] = fileEmptyArray(numberOfChunksInBlock)

            var j = 0
            while (j < blockSize) {
                // Default chunk size
                var chunkSize = fileChunkSize
                // Last chunk in block
                if (j + fileChunkSize > blockSize) {
                    chunkSize = blockSize.rem(fileChunkSize)
                }

                //XYBase.log.info("XYOtaFile", "total bytes: " + bytes!!.size + ", offset: " + byteOffset + ", block: " + i + ", chunk: " + (chunkNumber + 1) + ", blockSize: " + blockSize + ", chunkSize: " + chunkSize)
                val chunk = (bytes!!).copyOfRange(byteOffset, byteOffset + chunkSize)
                blocks!![i][chunkNumber] = chunk
                byteOffset += chunkSize
                chunkNumber++
                totalChunkCount++
                j += fileChunkSize
            }
        }
    }

    fun getBlock(index: Int): Array<ByteArray> {
        return blocks!![index]
    }

    fun close() {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun calculateCrc(): UByte {
        var crcCode: UByte = 0x0U
        for (i in 0 until bytesAvailable) {
            crcCode = crcCode xor bytes!![i].toUByte()
        }
        //XYBase.log.info("XYOtaFile", String.format("Firmware CRC: %#04x", crc_code and 0xff.toByte()))

        return crcCode
    }

    companion object {

        fun getByName(folderName: String, filename: String): XYOtaFile {
            // Get the file and store it in fileStream

            val inputStream = FileInputStream("${folderPath(folderName)}/$filename")
            return XYOtaFile(inputStream)
        }

        fun getByStream(stream: InputStream): XYOtaFile {
            return XYOtaFile(stream)
        }

        fun folderPath(folderName: String): String {
            return folderName //""${Environment.getExternalStorageDirectory().absolutePath}/$folderName"
        }

        fun createFileDirectory(folderName: String): Boolean {
            val directoryName = folderPath(folderName)
            val directory: java.io.File
            directory = java.io.File(directoryName)
            return directory.exists() || directory.mkdirs()
        }

        fun list(folderName: String): ArrayList<String>? {
            val f = java.io.File(folderPath(folderName))
            val file = f.listFiles() ?: return null
            Log.d("Files", "Size: " + file.size)
            val names = ArrayList<String>()
            for (i in file.indices) {
                Log.d("Files", "FileName:" + file[i].name)
                names.add(file[i].name)
            }
            return names
        }
    }
}
