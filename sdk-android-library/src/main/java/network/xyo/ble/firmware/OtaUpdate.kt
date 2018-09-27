package network.xyo.ble.firmware

import android.util.Log
import com.dialog.suota.data.OtaFile
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import network.xyo.core.XYBase.Companion.logInfo
import java.math.BigInteger

class OtaUpdate(var device: XY4BluetoothDevice, private val otaFile: OtaFile?) {

    val MISO_GPIO = 0x05            // p0_5
    val MOSI_GPIO = 0x06            // p0_6
    val CS_GPIO = 0x07              // p0_7
    val SCK_GPIO = 0x00             // p0_0
    val END_SIGNAL = -0x2000000
    val REBOOT_SIGNAL = -0x3000000
    val MEMORY_TYPE_EXTERNAL_SPI = 0x13

    // SUOTA
    private var imageBank: Int = 0

    private val listeners = HashMap<String, Listener>()
    private var lastBlock = false
    private var lastBlockSent = false
    private var lastBlockReady = false
    private var endSignalSent = false
    private var retryCount = 0
    private var chunkCount = -1
    private var blockCounter = 0
    private var nextStep = Step.MemDev

    //private var mtu = 23

    private var _allowRetry = true
    var allowRetry: Boolean
        get() = _allowRetry
        set(allow) {
            _allowRetry = allow
        }

    /**
     * Starts the update
     */
    fun start() {
        //reset()
        startUpdate()
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

    private fun reset() {
        retryCount = 0
        nextStep = Step.MemDev

        lastBlock = false
        lastBlockSent = false
        lastBlockReady = false
        endSignalSent = false
        blockCounter = 0
        chunkCount = -1
    }

    private fun startUpdate() {
        GlobalScope.launch {
            var hasError = false

            //STEP 1 - memdev
            val memResult = setMemDev().await()
            memResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                logInfo(TAG, "startUpdate - MemDev ERROR: $error")
            }

            //STEP 2 - GpioMap
            val gpioResult = setGpioMap().await()
            gpioResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                logInfo(TAG, "startUpdate - GPIO ERROR: $error")
            }

            //STEP 3 - (and when final block is sent)
            val patchResult = setPatchLength().await()
            patchResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                logInfo(TAG, "startUpdate - patch ERROR: $error")
            }

            //STEP 4 - send blocks
            while (!lastBlockSent && !hasError) {

                val blockResult = sendBlock().await()
                blockResult.error?.let { error ->
                    hasError = true
                    failUpdate(error.message.toString())
                    logInfo(TAG, "startUpdate - sendBlock ERROR: $error")
                }

                if (lastBlock) {
                    logInfo(TAG, "startUpdate LAST BLOCK - SET PATCH LEN ***************: $lastBlock")
                    val finalPatchResult = setPatchLength().await()

                    finalPatchResult.error?.let { error ->
                        hasError = true
                        failUpdate(error.message.toString())
                        logInfo(TAG, "startUpdate - finalPatchResult ERROR: $error")
                    }
                }
            }

            logInfo(TAG, "startUpdate done sending blocks.........")

            //SEND END SIGNAL
            val endResult = sendEndSignal().await()
            endResult.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                logInfo(TAG, "startUpdate - endResult ERROR: $error")
            }

            //REBOOT
            val reboot = sendReboot().await()
            reboot.error?.let { error ->
                hasError = true
                failUpdate(error.message.toString())
                logInfo(TAG, "startUpdate - reboot ERROR: $error")
            }

            passUpdate()
        }
    }

    private fun passUpdate() {
        logInfo(TAG, "passUpdate -- listener.updated")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.updated(device)
                }
            }
        }
    }

    private fun failUpdate(error: String) {
        logInfo(TAG, "failUpdate -- listener.failed")
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
            val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or imageBank
            logInfo(TAG, "setMemDev: " + String.format("%#010x", memType))
            val result = device.spotaService.SPOTA_MEM_DEV.set(memType).await()

            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //STEP 2
    private fun setGpioMap(): Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            val memInfo = MISO_GPIO shl 24 or (MOSI_GPIO shl 16) or (CS_GPIO shl 8) or SCK_GPIO
            logInfo(TAG, "setGpioMap: " + String.format("%#010x", Integer.valueOf(memInfo)))

            val result = device.spotaService.SPOTA_GPIO_MAP.set(memInfo).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //STEP 3 - (and when final block is sent)
    private fun setPatchLength(): Deferred<XYBluetoothResult<XYBluetoothResult<Int>>> {
        logInfo(TAG, "setPatchLength")
        return asyncBle {
            //TODO - is this correct?
            var blockSize = 240
            if (lastBlock) {
                blockSize = otaFile?.numberOfBytes?.rem(240) ?: 0
                lastBlockReady = true
            }

            logInfo(TAG, "start setPatchLength blockSize: $blockSize - ${String.format("%#06x", blockSize)}")

            val result = device.spotaService.SPOTA_PATCH_LEN.set(blockSize).await()
            logInfo(TAG, "start setPatchLength result: ${result.value.toString()}")
            return@asyncBle XYBluetoothResult(result)
        }
    }

    //STEP 4
    private fun sendBlock(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "sendBlock...")
        return asyncBle {
            val block = otaFile?.getBlock(blockCounter)
            val i = ++chunkCount
            var lastChunk = false
            if (chunkCount == block!!.size - 1) {
                chunkCount = -1
                lastChunk = true
            }

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

            val chunk = block[i]
            val result = device.spotaService.SPOTA_PATCH_DATA.set(chunk).await()

            return@asyncBle XYBluetoothResult(result.value)
        }
    }


    private fun sendEndSignal(): Deferred<XYBluetoothResult<Int>> {
        logInfo(TAG, "sendEndSignal...")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(END_SIGNAL).await()
            endSignalSent = true
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //DONE
    private fun sendReboot(): Deferred<XYBluetoothResult<Int>> {
        logInfo(TAG, "sendReboot...")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(REBOOT_SIGNAL).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }


    enum class Step {
        MemDev, GpioMap, PatchLen, WriteData
    }

    companion object {
        private const val TAG = "OtaUpdate"
        //TODO - setBlock retry
        private const val MAX_RETRY_COUNT = 3
    }

    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
    }
}