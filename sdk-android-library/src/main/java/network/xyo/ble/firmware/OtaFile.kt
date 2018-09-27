package com.dialog.suota.data

import android.content.Context
import android.os.Environment
import android.util.Log
import network.xyo.core.XYBase
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.xor

class OtaFile @Throws(IOException::class)
private constructor(private val inputStream: InputStream?) {
    var crc: Byte = 0
        private set
    private var bytes: ByteArray? = null

    private var blocks: Array<Array<ByteArray>>? = null

    var fileBlockSize = 0
        private set
    private var fileChunkSize = 20
    private val bytesAvailable: Int
    var numberOfBlocks = -1
        private set
    var chunksPerBlockCount: Int = 0
        private set
    var totalChunkCount: Int = 0
        private set
    private var type: Int = 0

    val numberOfBytes: Int
        get() = this.bytes!!.size

    init {
        this.bytesAvailable = this.inputStream!!.available()

        this.bytes = ByteArray(this.bytesAvailable + 1)
        this.inputStream!!.read(this.bytes!!)
        this.crc = calculateCrc()
        this.bytes!![this.bytesAvailable] = this.crc

        setFileBlockSize(240, 20)
    }

    fun setFileBlockSize(fileBlockSize: Int, fileChunkSize: Int) {
        this.fileBlockSize = Math.max(fileBlockSize, fileChunkSize)
        this.fileChunkSize = fileChunkSize
        this.chunksPerBlockCount = this.fileBlockSize / this.fileChunkSize + if (this.fileBlockSize % this.fileChunkSize != 0) 1 else 0
        this.numberOfBlocks = bytes!!.size / this.fileBlockSize + if (bytes!!.size % this.fileBlockSize != 0) 1 else 0
        this.initBlocksSuota()
    }

    private inline fun <reified T> fileEmptyArray(size: Int): Array<T> =
            @Suppress("UNCHECKED_CAST")
            (arrayOfNulls<T>(size) as Array<T>)

    //This one
    private fun initBlocksSuota() {
        totalChunkCount = 0
        blocks = fileEmptyArray(numberOfBlocks)
        Log.d("OtaFile", "initBlocksSuota numberOfBlocks: $numberOfBlocks")

        var byteOffset = 0
        // Loop through all the bytes and split them into pieces the size of the default chunk size
        for (i in 0 until numberOfBlocks) {
            var blockSize = fileBlockSize
            var numberOfChunksInBlock = chunksPerBlockCount
            // Check if the last block needs to be smaller
            if (byteOffset + fileBlockSize > bytes!!.size) {
                blockSize = bytes!!.size % fileBlockSize
                numberOfChunksInBlock = blockSize / fileChunkSize + if (blockSize % fileChunkSize != 0) 1 else 0
            }
            var chunkNumber = 0
            blocks!![i] = fileEmptyArray(numberOfChunksInBlock)

            var j = 0
            while (j < blockSize) {
                // Default chunk size
                var chunkSize = fileChunkSize
                // Last chunk in block
                if (j + fileChunkSize > blockSize) {
                    chunkSize = blockSize % fileChunkSize
                }

                Log.d("OtaFile", "total bytes: " + bytes!!.size + ", offset: " + byteOffset + ", block: " + i + ", chunk: " + (chunkNumber + 1) + ", blocksize: " + blockSize + ", chunksize: " + chunkSize)
                val chunk = Arrays.copyOfRange(bytes!!, byteOffset, byteOffset + chunkSize)
                blocks!![i][chunkNumber] = chunk
                byteOffset += chunkSize
                chunkNumber++
                totalChunkCount++
                j += fileChunkSize
                Log.d("OtaFile", "fileChunkSize ---------------LAST CHUNK ON: $fileChunkSize -- j: $j")
            }
        }
    }

    fun getBlock(index: Int): Array<ByteArray> {
        return blocks!![index]
    }

    fun close() {
        if (this.inputStream != null) {
            try {
                this.inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    @Throws(IOException::class)
    private fun calculateCrc(): Byte {
        var crc_code: Byte = 0
        for (i in 0 until this.bytesAvailable) {
            val byteValue = this.bytes!![i]
            val intVal = byteValue.toInt()
            crc_code = crc_code xor intVal.toByte()
        }
        Log.d("OtaFile", String.format("Fimware CRC: %#04x", crc_code and 0xff.toByte()))
        return crc_code
    }

    companion object {
        private val filesDir = Environment.getExternalStorageDirectory().absolutePath + "/Xyo"

        @Throws(IOException::class)
        fun getByFileName(filename: String): OtaFile {
            // Get the file and store it in fileStream

            val `is` = FileInputStream("$filesDir/$filename")
            return OtaFile(`is`)
        }

        fun createFileDirectories(c: Context): Boolean {
            val directoryName = filesDir
            val directory: java.io.File
            directory = java.io.File(directoryName)
            return directory.exists() || directory.mkdirs()
        }
    }
}
