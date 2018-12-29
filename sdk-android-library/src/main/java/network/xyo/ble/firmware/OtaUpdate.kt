package network.xyo.ble.firmware

import kotlinx.coroutines.*
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import network.xyo.core.XYBase

class OtaUpdate(var device: XY4BluetoothDevice, private val otaFile: OtaFile?): XYBase() {

    private val listeners = HashMap<String, Listener>()
    private var updateJob: Job? = null
    private var lastBlock = false
    private var lastBlockSent = false
    private var lastBlockReady = false
    private var endSignalSent = false
    private var retryCount = 0
    private var chunkCount = -1
    private var blockCounter = 0

    private var _imageBank = 0
    var imageBank: Int
        get() = _imageBank
        set(value) {
            _imageBank = value
        }

    //SPI_DI
    private var _miso_gpio = 0x05
    var MISO_GPIO: Int
        get() = _miso_gpio
        set(value) {
            _miso_gpio = value
        }

    //SPI_DO
    private var _mosi_gpio = 0x06
    var MOSI_GPIO: Int
        get() = _mosi_gpio
        set(value) {
            _mosi_gpio = value
        }

    //SPI_EN
    private var cs_gpio = 0x07
    var CS_GPIO: Int
        get() = cs_gpio
        set(value) {
            cs_gpio = value
        }

    //DPI_CLK
    private var _sck_gpio = 0x00
    var SCK_GPIO: Int
        get() = _sck_gpio
        set(value) {
            _sck_gpio = value
        }

    //todo - NOT IMPLEMENTED
//    private var _allowRetry = true
//    var allowRetry: Boolean
//        get() = _allowRetry
//        set(allow) {
//            _allowRetry = allow
//        }

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

    fun addListener(key: String, listener: Listener) {
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
        retryCount = 0
        blockCounter = 0
        chunkCount = -1
        lastBlock = false
        lastBlockSent = false
        lastBlockReady = false
        endSignalSent = false
    }

    private fun startUpdate() {
        updateJob = GlobalScope.launch {
            var hasError = false

            //STEP 1 - memdev
            val memResult = setMemDev().await()
            memResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                updateJob?.cancelAndJoin()
                log.info( "startUpdate - MemDev ERROR: $error")
            }

            //STEP 2 - GpioMap
            val gpioResult = setGpioMap().await()
            gpioResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                updateJob?.cancelAndJoin()
                log.info( "startUpdate - GPIO ERROR: $error")
            }

            //STEP 3 - Set patch length for the first and last block
            val patchResult = setPatchLength().await()
            patchResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                updateJob?.cancelAndJoin()
                log.info( "startUpdate - patch ERROR: $error")
            }

            //STEP 4 - send blocks
            while (!lastBlockSent && !hasError) {
                progressUpdate()
                val blockResult = sendBlock().await()
                blockResult.error?.let { error ->
                    hasError = true
                    failUpdate(error.message.toString())
                    updateJob?.cancelAndJoin()
                    log.info( "startUpdate - sendBlock ERROR: $error")
                }

                if (lastBlock) {
                    if (!lastBlockReady && otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize) != 0) {
                        log.info( "startUpdate LAST BLOCK - SET PATCH LEN: $lastBlock")
                        val finalPatchResult = setPatchLength().await()

                        finalPatchResult.error?.let { error ->
                            hasError = true
                            failUpdate(error.message.toString())
                            updateJob?.cancelAndJoin()
                            log.info( "startUpdate - finalPatchResult ERROR: $error")
                        }
                    }
                }

            }

            log.info( "startUpdate done sending blocks")

            //SEND END SIGNAL
            val endResult = sendEndSignal().await()
            endResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                updateJob?.cancelAndJoin()
                log.info( "startUpdate - endSignal Result ERROR: $error")
            }

            //REBOOT
            val reboot = sendReboot().await()
            reboot.error?.let { error ->
                log.info( "startUpdate - reboot ERROR: $error")
            }

            passUpdate()
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
    private fun setMemDev(): Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or _imageBank
            log.info( "setMemDev: " + String.format("%#010x", memType))
            val result = device.spotaService.SPOTA_MEM_DEV.set(memType).await()

            return@asyncBle XYBluetoothResult(result.value, result.error)
        }
    }

    //STEP 2
    private fun setGpioMap(): Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            val memInfo = _miso_gpio shl 24 or (_mosi_gpio shl 16) or (cs_gpio shl 8) or _sck_gpio

            val result = device.spotaService.SPOTA_GPIO_MAP.set(memInfo).await()

            return@asyncBle XYBluetoothResult(result.value, result.error)
        }
    }

    //STEP 3 - (and when final block is sent)
    private fun setPatchLength(): Deferred<XYBluetoothResult<XYBluetoothResult<Int>>> {
        return asyncBle {
            var blockSize = otaFile?.fileBlockSize
            if (lastBlock) {
                blockSize = otaFile?.numberOfBytes?.rem(otaFile.fileBlockSize)
                lastBlockReady = true
            }

            log.info( "setPatchLength blockSize: $blockSize - ${String.format("%#06x", blockSize)}")

            val result = device.spotaService.SPOTA_PATCH_LEN.set(blockSize!!).await()

            return@asyncBle XYBluetoothResult(result, result.error)
        }
    }

    //STEP 4
    private fun sendBlock(): Deferred<XYBluetoothResult<ByteArray>> {
        return asyncBle {
            val block = otaFile?.getBlock(blockCounter)
            val i = ++chunkCount
            var lastChunk = false
            if (chunkCount == block!!.size - 1) {
                chunkCount = -1
                lastChunk = true
            }

            val chunk = block[i]
            val msg = "Sending block " + (blockCounter + 1) + ", chunk " + (i + 1) + " of " + block.size + ", size " + chunk.size
            log.info( msg)



            if (lastChunk) {
                log.info( "sendBlock... lastChunk")
                if (!lastBlock) {
                    blockCounter++
                } else {
                    lastBlockSent = true
                }

                if (blockCounter + 1 == otaFile?.numberOfBlocks) {
                    lastBlock = true
                }
            }
            val result = device.spotaService.SPOTA_PATCH_DATA.set(chunk).await()

            return@asyncBle XYBluetoothResult(result.value, result.error)
        }
    }


    private fun sendEndSignal(): Deferred<XYBluetoothResult<Int>> {
        log.info( "sendEndSignal...")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(END_SIGNAL).await()
            log.info( "sendEndSignal result: $result")
            endSignalSent = true
            return@asyncBle XYBluetoothResult(result.value, result.error)
        }
    }

    //DONE
    private fun sendReboot(): Deferred<XYBluetoothResult<Int>> {
        log.info( "sendReboot...")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(REBOOT_SIGNAL).await()
            return@asyncBle XYBluetoothResult(result.value, result.error)
        }
    }


    companion object {
        private const val TAG = "OtaUpdate"

        //TODO - setBlock retry
        const val MAX_RETRY_COUNT = 3
        const val END_SIGNAL = -0x2000000
        const val REBOOT_SIGNAL = -0x3000000
        const val MEMORY_TYPE_EXTERNAL_SPI = 0x13
    }

    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
        open fun progress(sent: Int, total: Int) {}
    }
}