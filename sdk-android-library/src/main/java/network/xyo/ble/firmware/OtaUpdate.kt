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

    private var patchDataSize = 20
    private var fileChunkSize = 20
    private var mtu = 23

    internal var hasError = false

    private var _allowRetry = true
    var allowRetry: Boolean
        get() = _allowRetry
        set(allow) {
            _allowRetry = allow
        }

    init {
        initErrorMap()
    }

    /**
     * Starts the update
     */
    fun start() {
        //reset()
        //otaFile?.setFileBlockSize(240, 20)
        //dispatchNextStep()
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
        hasError = false
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
            //STEP 1 - memdev
            val memResult = setMemDev().await()
            memResult.error?.let { error ->
                logInfo(TAG, "startUpdate - MemDev ERROR: $error")
            }

            logInfo(TAG, "startUpdate MemDev memResult: $memResult")

            //STEP 2 - GpioMap
            val gpioResult = setGpioMap().await()
            gpioResult.error?.let { error ->
                logInfo(TAG, "startUpdate - GPIO ERROR: $error")
            }

            logInfo(TAG, "startUpdate MemDev gpioResult: $gpioResult")

            //STEP 3 - (and when final block is sent)
            val patchResult = setPatchLength().await() //setPatchLength().await()
            patchResult.error?.let { error ->
                logInfo(TAG, "startUpdate - patch ERROR: $error")
            }

            logInfo(TAG, "startUpdate MemDev patchResult: $gpioResult")

            //STEP 4 - send blocks

            while (!lastBlockSent) {

                val blockResult = sendBlock().await()
                blockResult.error?.let { error ->
                    logInfo(TAG, "startUpdate - sendBlock ERROR: $error")
                }
                logInfo(TAG, "startUpdate sendBlock: $blockResult")

                if (lastBlock) {
                    logInfo(TAG, "startUpdate LAST BLOCK - SET PATCH LEN ***************: $lastBlock")
                    val finalPatchResult = setPatchLength().await()

                    finalPatchResult.error?.let { error ->
                        logInfo(TAG, "startUpdate - finalPatchResult ERROR: $error")
                    }
                }

            }

            logInfo(TAG, "startUpdate done sending blocks......... gpioResult")

            //SEND END SIGNAL
            val endResult = sendEndSignal().await()
            endResult.error?.let { error ->
                logInfo(TAG, "startUpdate - endResult ERROR: $error")
            }

            logInfo(TAG, "startUpdate -- sent end signal")

            //REBOOT
            val reboot = sendReboot().await()
            reboot.error?.let { error ->
                logInfo(TAG, "startUpdate - reboot ERROR: $error")
            }

            logInfo(TAG, "startUpdate -- sent reboot....")

        }
    }

    private fun dispatchNextStep() {
        GlobalScope.launch {
            logInfo(TAG, "dispatchNextStep: $nextStep ")
            when (nextStep) {
                OtaUpdate.Step.MemDev -> {
                    // Init mem type
                   // val notify = enableNotifications().await()
                   // logInfo(TAG, "dispatchNextStep MemDev notify: ${notify.value.toString()} ")
                    val result = setMemDev().await()
                    logInfo(TAG, "dispatchNextStep MemDev result: ${result.value.toString()} ")

                    //Returning 19 - error
                    //processNextStep(result)
                }
                OtaUpdate.Step.GpioMap -> {
                    // Set mem_type for SPOTA_GPIO_MAP_UUID
                    val result = setGpioMap().await()
                   // processNextStep(result)
                }
                OtaUpdate.Step.PatchLen -> {
                    // Set SPOTA_PATCH_LEN_UUID
                    val result = setPatchLength().await()
                   // processNextStep(result)
                }
                OtaUpdate.Step.WriteData -> {
                    // Send a block containing blocks of 20 bytes until the patch length (default 240) has been reached
                    // Wait for response and repeat this action
                    if (!lastBlock) {
                        val result = sendBlock().await()
                        if (result.error != null) {
                            if (allowRetry && retryCount < MAX_RETRY_COUNT) {
                                retryCount++
                                //try to send block again
                                this@OtaUpdate.dispatchNextStep()
                            } else {
                                failUpdate("exceeded Max retry count of $retryCount on WriteData")
                            }
                        }

                    } else {
                        if (!lastBlockReady) {
                            val result = setPatchLength().await()
                            //processNextStep(result)
                        } else if (!lastBlockSent) {
                            val result = sendBlock().await()
                           // processNextStep(result)
                        } else if (!endSignalSent) {
                           // val result = sendEndSignal().await()
                            //processNextStep(result)
                        } else {
                            //TODO - done
                           // val result = sendReboot().await()
                            passUpdate()
                        }
                    }
                }
            }
        }
    }

    private fun processNextStep(byteValue: XYBluetoothResult<ByteArray>) {
        logInfo(TAG, "processNextStep...")

        val error = byteValue.error.toString()
        logInfo(TAG, "processNextStep error: $error")

//        if (byteValue.value == null) {
//            failUpdate("byteValue is empty")
//            return
//        }

        var value = 0
        byteValue.value?.let {
            value = BigInteger(it).toInt()
        }

        logInfo(TAG, "processNextStep value: ${byteValue.toString()}")
        val stringValue = String.format("%#10x", value).trim { it <= ' ' }
        logInfo(TAG, "*** processNextStep stringValue: $stringValue")

        if (stringValue == "0x10") {
            // SUOTA image started
            nextStep = Step.GpioMap
            dispatchNextStep()

        } else if (stringValue == "0x2") {
            // Successfully sent a block, send the next one
            nextStep = Step.WriteData
            dispatchNextStep()

        } else if (stringValue == "0x3" || stringValue == "0x1") {
            // SPOTA service status
            //TODO - do we hit here
            logInfo(TAG, "************** processNextStep MemDev block hit")
            //memDev
        } else {
            val errorMsg = onError(value)
            failUpdate("OTA Update Failed on step: $stringValue - error: $errorMsg")

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

//    private fun enableNotifications(): Deferred<XYBluetoothResult<Int>> {
//        return asyncBle {
//            //val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or imageBank
//            logInfo(TAG, "start enableNotifications: ")
//            val result = device.spotaService.SPOTA_SERV_STATUS.set(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE).await()
//
//            return@asyncBle XYBluetoothResult(result.value)
//        }
//    }

    //STEP 1
    private fun setMemDev(): Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            val memType = MEMORY_TYPE_EXTERNAL_SPI shl 24 or imageBank
            logInfo(TAG, "start setMemDev: " + String.format("%#010x", memType))
            val result = device.spotaService.SPOTA_MEM_DEV.set(memType).await()

            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //STEP 2
    private fun setGpioMap(): Deferred<XYBluetoothResult<Int>> {
        return asyncBle {
            val memInfo = MISO_GPIO shl 24 or (MOSI_GPIO shl 16) or (CS_GPIO shl 8) or SCK_GPIO
            logInfo(TAG, "start setGpioMap: $memInfo")
            logInfo(TAG, "setGpioMap: " + String.format("%#010x", Integer.valueOf(memInfo)))

            val result = device.spotaService.SPOTA_GPIO_MAP.set(memInfo).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //STEP 3 - (and when final block is sent)
    private fun setPatchLength(): Deferred<XYBluetoothResult<XYBluetoothResult<Int>>> {
        logInfo(TAG, "start setPatchLength...")
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
        //logInfo(TAG, "sendBlock")
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

            //logInfo(TAG, "sendBlock-ending chunk: $chunkCount")

            val result = device.spotaService.SPOTA_PATCH_DATA.set(chunk).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }


    private fun sendEndSignal(): Deferred<XYBluetoothResult<Int>> {
        logInfo(TAG, "start sendEndSignal...")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(END_SIGNAL).await()
            endSignalSent = true
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    //DONE
    private fun sendReboot(): Deferred<XYBluetoothResult<Int>> {
        logInfo(TAG, "start sendReboot.   ")
        return asyncBle {
            val result = device.spotaService.SPOTA_MEM_DEV.set(REBOOT_SIGNAL).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }


    enum class Step {
        MemDev, GpioMap, PatchLen, WriteData
    }

    private fun onError(errorCode: Int): String {
        //TODO - show ui/toast
        val error = errors?.get(errorCode)
        Log.d(TAG, "Error: $errorCode $error")
        return error.toString()
    }

    lateinit var errors: java.util.HashMap<Int, String>
    val ERROR_COMMUNICATION = 0xffff // ble communication error
    val ERROR_SUOTA_NOT_FOUND = 0xfffe // suota service was not found
    private fun initErrorMap() {
        errors = java.util.HashMap()

        errors[0x03] = "Forced exit of SPOTA service."
        errors[0x04] = "Patch Data CRC mismatch."
        errors[0x05] = "Received patch Length not equal to PATCH_LEN characteristic value."
        errors[0x06] = "External Memory Error. Writing to external device failed."
        errors[0x07] = "Internal Memory Error. Not enough internal memory space for patch."
        errors[0x08] = "Invalid memory device."
        errors[0x09] = "Application error."

        // SUOTAR application specific error codes
        errors[0x01] = "SPOTA service started instead of SUOTA."
        errors[0x11] = "Invalid image bank."
        errors[0x12] = "Invalid image header."
        errors[0x13] = "Invalid image size."
        errors[0x14] = "Invalid product header."
        errors[0x15] = "Same Image Error."
        errors[0x16] = "Failed to read from external memory device."

        // Application error codes
        errors[ERROR_COMMUNICATION] = "Communication error."
        errors[ERROR_SUOTA_NOT_FOUND] = "The remote device does not support SUOTA."
    }

    companion object {
        private const val TAG = "OtaUpdate"
        private const val MAX_RETRY_COUNT = 3

    }

    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
    }
}