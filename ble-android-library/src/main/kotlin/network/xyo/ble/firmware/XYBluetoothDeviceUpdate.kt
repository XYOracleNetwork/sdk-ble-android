package network.xyo.ble.firmware

import kotlinx.coroutines.*
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.services.dialog.SpotaService
import network.xyo.base.XYBase

class XYBluetoothDeviceUpdate(private var spotaService: SpotaService, var device: XYBluetoothDevice, private val otaFile: XYOtaFile?) : XYBase() {

    private val listeners = HashMap<String, XYOtaUpdate.Listener>()
    private var updateJob: Job? = null
    private var lastBlock = false
    private var lastBlockSent = false
    private var lastBlockReady = false
    private var endSignalSent = false
    private var chunkCount = -1
    private var blockCounter = 0

    /**
     * Send REBOOT_SIGNAL after flashing. Default is true.
     */
    var sendRebootOnComplete = true

    /**
     * Image Bank to flash to - default is 0
     */
    var imageBank = 0

    //SPI_DI
    var misoGpio = 0x05

    //SPI_DO
    var mosiGpio = 0x06

    //SPI_EN
    var csGpio = 0x07

    //DPI_CLK
    var sckGpio = 0x00

    /**
     * Starts the update
     */
    fun start() {
        startUpdate()
    }

    fun cancel() {
        GlobalScope.launch {
            updateJob?.cancelAndJoin()
            reset()
            listeners.clear()
        }

    }

    fun addListener(key: String, listener: XYOtaUpdate.Listener) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners.put(key, listener)
            }
        }
    }

    fun removeListener(key: String) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners.remove(key)
            }
        }
    }

    fun reset() {
        blockCounter = 0
        chunkCount = -1
        lastBlock = false
        lastBlockSent = false
        lastBlockReady = false
        endSignalSent = false
    }

    private fun startUpdate() {
        updateJob = GlobalScope.async {

            val conn = device.connection {

                //STEP 1 - memdev
                val memResult = setMemDev()
                var error = memResult.error
                if (error != null) {
                    log.info(TAG, "startUpdate:MemDev ERROR: ${error.message.toString()}")

                    return@connection XYBluetoothResult(false, error)
                }

                //STEP 2 - GpioMap
                val gpioResult = setGpioMap()
                error = gpioResult.error
                if (error != null) {
                    log.info(TAG, "startUpdate:GPIO ERROR: ${error.message.toString()}")

                    return@connection XYBluetoothResult(false, error)
                }

                //STEP 3 - Set patch length for the first and last block
                val patchResult = setPatchLength()
                error = patchResult.error
                if (error != null) {
                    log.info(TAG, "startUpdate:patch ERROR: ${error.message.toString()}")

                    return@connection XYBluetoothResult(false, error)
                }

                //STEP 4 - send blocks
                while (!lastBlockSent) {
                    progressUpdate()
                    val blockResult = sendBlock()
                    var blockError = blockResult.error
                    if (blockError != null) {
                        log.info(TAG, "startUpdate:sendBlock ERROR: ${blockError.message.toString()}")

                        return@connection XYBluetoothResult(false, blockError)
                    }

                    if (lastBlock) {
                        if (!lastBlockReady && otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize) != 0) {
                            log.info(TAG, "startUpdate:LAST BLOCK - SET PATCH LEN: $lastBlock")

                            val finalPatchResult = setPatchLength()
                            blockError = finalPatchResult.error
                            if (blockError != null) {
                                log.info(TAG, "startUpdate:finalPatchResult ERROR: ${blockError.message.toString()}")

                                return@connection XYBluetoothResult(false, blockError)
                            }
                        }
                    }
                }

                log.info(TAG, "startUpdate:done sending blocks")

                //step 5 - send end signal
                val endResult = sendEndSignal()
                error = endResult.error
                if (error != null) {
                    log.info(TAG, "startUpdate:endSignal Result ERROR: ${error.message.toString()}")

                    return@connection XYBluetoothResult(false, error)
                }

                //step 6 - reboot
                if (sendRebootOnComplete) {
                    log.info(TAG, "startUpdate:sending Reboot")
                    val reboot = sendReboot()
                    error = reboot.error
                    if (error != null) {
                        // May loose connection after calling reboot - this is normal - don't fail it.
                        log.info(TAG, "startUpdate:reboot sent: ${error.message.toString()}")
                    }
                }

                return@connection XYBluetoothResult(true)
            }

            if (conn.hasError()) {
                //Failed to connect
                log.info(TAG, "startUpdate:conn.hasError, FAIL UPDATE ON: ${conn.error?.message.toString()}")
                failUpdate(conn.error?.message.toString())
                return@async false
            } else {
                passUpdate()
                return@async true
            }
        }
    }

    private fun progressUpdate() {
        val chunkNumber = blockCounter * (otaFile?.chunksPerBlockCount ?: 0) + chunkCount + 1
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    otaFile?.totalChunkCount?.let { listener.progress(chunkNumber, it) }
                }
            }
        }
    }

    private fun passUpdate() {
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.updated(device)
                }
            }
        }
    }

    private fun failUpdate(error: String) {
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.failed(device, error)
                }
            }
        }
    }

    //STEP 1
    private suspend fun setMemDev(): XYBluetoothResult<Int> {
        return GlobalScope.async {
            val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or imageBank
            log.info(TAG, "setMemDev: " + String.format("%#010x", memType))
            val result = spotaService.spotaMemDev.set(memType)

            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    //STEP 2
    private suspend fun setGpioMap(): XYBluetoothResult<Int> {
        return GlobalScope.async {
            val memInfo = misoGpio shl 24 or (mosiGpio shl 16) or (csGpio shl 8) or sckGpio

            val result = spotaService.spotaGpioMap.set(memInfo)

            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    //STEP 3 - (and when final block is sent)
    private suspend fun setPatchLength(): XYBluetoothResult<XYBluetoothResult<Int>> {
        return GlobalScope.async {
            var blockSize = otaFile?.fileBlockSize
            if (lastBlock) {
                blockSize = otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize)
                lastBlockReady = true
            }

            log.info(TAG, "setPatchLength blockSize: $blockSize - ${String.format("%#06x", blockSize)}")

            val result = spotaService.spotaPatchLen.set(blockSize!!)

            return@async XYBluetoothResult(result, result.error)
        }.await()
    }

    //STEP 4
    private suspend fun sendBlock(): XYBluetoothResult<ByteArray> {
        return GlobalScope.async {
            val block = otaFile?.getBlock(blockCounter)
            val i = ++chunkCount
            var lastChunk = false
            if (chunkCount == block!!.size - 1) {
                chunkCount = -1
                lastChunk = true
            }

            val chunk = block[i]

            if (lastChunk) {
                if (!lastBlock) {
                    blockCounter++
                } else {
                    lastBlockSent = true
                }

                if (blockCounter + 1 == otaFile?.numberOfBlocks) {
                    lastBlock = true
                }
            }
            val result = spotaService.spotaPatchData.set(chunk)

            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    //step 5
    private suspend fun sendEndSignal(): XYBluetoothResult<Int> {
        log.info(TAG, "sendEndSignal...")
        return GlobalScope.async {
            val result = spotaService.spotaMemDev.set(END_SIGNAL)
            endSignalSent = true
            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    //step 6
    private suspend fun sendReboot(): XYBluetoothResult<Int> {
        log.info(TAG, "sendReboot...")
        return GlobalScope.async {
            val result = spotaService.spotaMemDev.set(REBOOT_SIGNAL)
            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    companion object {
        private const val TAG = "XYBluetoothDeviceUpdate"

        const val END_SIGNAL = -0x2000000
        const val REBOOT_SIGNAL = -0x3000000
        const val MEMORY_TYPE_EXTERNAL_SPI = 0x13
    }

}