package network.xyo.ble.firmware

import kotlinx.coroutines.*
import network.xyo.base.XYBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.services.dialog.SpotaService

class XYBluetoothDeviceUpdate(private var spotaService: SpotaService, var device: XYBluetoothDevice, private val otaFile: XYOtaFile?) : XYBase() {

    private val listeners = HashMap<String, XYOtaUpdate.Listener>()
    private var updateJob: Job? = null
    private var lastBlock = false
    private var lastBlockSent = false
    private var lastBlockReady = false
    private var endSignalSent = false
    private var chunkCount = 0x0
    private var blockCounter = 0x0

    /**
     * Send REBOOT_SIGNAL after flashing. Default is true.
     */
    private var sendRebootOnComplete = true

    /**
     * Image Bank to flash to - default is 0
     */
    private var imageBank = 0x0U

    // SPI_DI
    private var misoGpio = 0x05U

    // SPI_DO
    private var mosiGpio = 0x06U

    // SPI_EN
    private var csGpio = 0x07U

    // DPI_CLK
    private var sckGpio = 0x00U

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

    private fun reset() {
        blockCounter = 0
        chunkCount = 0
        lastBlock = false
        lastBlockSent = false
        lastBlockReady = false
        endSignalSent = false
    }

    private fun startUpdate() {
        updateJob = GlobalScope.async {

            val conn = device.connection {

                // STEP 1 - memdev
                val memResult = setMemDev()
                var error = memResult.error
                if (error != XYBluetoothResult.ErrorCode.None) {
                    log.info(TAG, "startUpdate:MemDev ERROR: $error")

                    return@connection XYBluetoothResult(false, error)
                }

                // STEP 2 - GpioMap
                val gpioResult = setGpioMap()
                error = gpioResult.error
                if (error != XYBluetoothResult.ErrorCode.None) {
                    log.info(TAG, "startUpdate:GPIO ERROR: $error")

                    return@connection XYBluetoothResult(false, error)
                }

                // STEP 3 - Set patch length for the first and last block
                val patchResult = setPatchLength()
                error = patchResult.error
                if (error != XYBluetoothResult.ErrorCode.None) {
                    log.info(TAG, "startUpdate:patch ERROR: $error")

                    return@connection XYBluetoothResult(false, error)
                }

                // STEP 4 - send blocks
                while (!lastBlockSent) {
                    progressUpdate()
                    val blockResult = sendBlock()
                    var blockError = blockResult.error
                    if (blockError != XYBluetoothResult.ErrorCode.None) {
                        log.info(TAG, "startUpdate:sendBlock ERROR: $blockError")

                        return@connection XYBluetoothResult(false, blockError)
                    }

                    if (lastBlock) {
                        if (!lastBlockReady && otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize) != 0) {
                            log.info(TAG, "startUpdate:LAST BLOCK - SET PATCH LEN: $lastBlock")

                            val finalPatchResult = setPatchLength()
                            blockError = finalPatchResult.error
                            if (blockError != XYBluetoothResult.ErrorCode.None) {
                                log.info(TAG, "startUpdate:finalPatchResult ERROR: $blockError")

                                return@connection XYBluetoothResult(false, blockError)
                            }
                        }
                    }
                }

                log.info(TAG, "startUpdate:done sending blocks")

                // step 5 - send end signal
                val endResult = sendEndSignal()
                error = endResult.error
                if (error != XYBluetoothResult.ErrorCode.None) {
                    log.info(TAG, "startUpdate:endSignal Result ERROR: $error")

                    return@connection XYBluetoothResult(false, error)
                }

                // step 6 - reboot
                if (sendRebootOnComplete) {
                    log.info(TAG, "startUpdate:sending Reboot")
                    val reboot = sendReboot()
                    error = reboot.error
                    if (error != XYBluetoothResult.ErrorCode.None) {
                        // May loose connection after calling reboot - this is normal - don't fail it.
                        log.info(TAG, "startUpdate:reboot sent: $error")
                    }
                }

                return@connection XYBluetoothResult(true)
            }

            if (conn.hasError()) {
                // Failed to connect
                log.info(TAG, "startUpdate:conn.hasError, FAIL UPDATE ON: ${conn.error}")
                failUpdate(conn.error.name)
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

    // STEP 1
    private suspend fun setMemDev(): XYBluetoothResult<UInt> {
        return GlobalScope.async {
            val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or imageBank
            log.info(TAG, "setMemDev: " + String.format("%#010x", memType))
            val result = spotaService.spotaMemDev.set(memType)

            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    // STEP 2
    private suspend fun setGpioMap(): XYBluetoothResult<UInt> {
        return GlobalScope.async {
            val memInfo = misoGpio shl 24 or (mosiGpio shl 16) or (csGpio shl 8) or sckGpio

            val result = spotaService.spotaGpioMap.set(memInfo)

            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    // STEP 3 - (and when final block is sent)
    private suspend fun setPatchLength(): XYBluetoothResult<XYBluetoothResult<UInt>> {
        return GlobalScope.async {
            var blockSize = otaFile?.fileBlockSize
            if (lastBlock) {
                blockSize = otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize)
                lastBlockReady = true
            }

            log.info(TAG, "setPatchLength blockSize: $blockSize - ${String.format("%#06x", blockSize)}")

            val result = spotaService.spotaPatchLen.set(blockSize!!.toUInt())

            return@async XYBluetoothResult(result, result.error)
        }.await()
    }

    // STEP 4
    private suspend fun sendBlock(): XYBluetoothResult<ByteArray> {
        return GlobalScope.async {
            val block = otaFile?.getBlock(blockCounter)
            val i = ++chunkCount
            var lastChunk = false
            if (chunkCount == block!!.size - 1) {
                chunkCount = 0
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

    // step 5
    private suspend fun sendEndSignal(): XYBluetoothResult<UInt> {
        log.info(TAG, "sendEndSignal...")
        return GlobalScope.async {
            val result = spotaService.spotaMemDev.set(END_SIGNAL)
            endSignalSent = true
            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    // step 6
    private suspend fun sendReboot(): XYBluetoothResult<UInt> {
        log.info(TAG, "sendReboot...")
        return GlobalScope.async {
            val result = spotaService.spotaMemDev.set(REBOOT_SIGNAL)
            return@async XYBluetoothResult(result.value, result.error)
        }.await()
    }

    companion object {
        private const val TAG = "XYBluetoothDeviceUpdate"

        const val END_SIGNAL = 0x2000000U
        const val REBOOT_SIGNAL = 0x3000000U
        const val MEMORY_TYPE_EXTERNAL_SPI = 0x13U
    }
}
