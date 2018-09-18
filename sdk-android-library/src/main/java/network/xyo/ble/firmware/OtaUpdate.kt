package network.xyo.ble.firmware

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

    private val listeners = HashMap<String, Listener>()
    private var lastBlock = false
    private var lastBlockSent = false
    private var lastBlockReady = false
    private var endSignalSent = false
    private var retryCount = 0
    private var chunkCount = -1
    private var blockCount = 0
    private var nextStep = Step.MemDev

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
        reset()
        dispatchNextStep()
    }

    fun cancelUpdate() {
        // TODO - should we allow this ?
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
        blockCount = 0
        chunkCount = -1
    }

    private fun dispatchNextStep() {
        GlobalScope.launch {
            logInfo(TAG, "dispatchNextStep: $nextStep ")
            when (nextStep) {
                OtaUpdate.Step.MemDev -> {
                    val result = setMemDev().await()
                    processNextStep(result)
                }
                OtaUpdate.Step.GpioMap -> {
                    val result = setGpioMap().await()
                    processNextStep(result)
                }
                OtaUpdate.Step.PatchLen -> {
                    val result = setPatchLength().await()
                    processNextStep(result)
                }
                OtaUpdate.Step.WriteData -> {
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
                            processNextStep(result)
                        } else if (!lastBlockSent) {
                            val result = sendBlock().await()
                            processNextStep(result)
                        } else if (!endSignalSent) {
                            val result = sendEndSignal().await()
                            processNextStep(result)
                        } else {
                            //TODO - done
                            val result = sendReboot().await()
                            passUpdate()
                        }
                    }
                }
            }
        }
    }

    private fun processNextStep(byteValue: XYBluetoothResult<ByteArray>) {
        logInfo(TAG, "init processNextStep...")

        val error = byteValue.error.toString()
//        if (error != null && error != "None") {
//            logInfo(TAG, "processNextStep error: ${byteValue.error}")
//            //TODO - better error msg
//            var error = "Update failed"
//            byteValue.error?.let {
//                error = it.message.toString()
//            }
//            failUpdate(error)
//            return
//        }

        if (byteValue.value == null) {
            failUpdate("byteValue is empty")
            return
        }

        val value = BigInteger(byteValue.value).toInt()
        val stringValue = String.format("%#10x", value).trim { it <= ' ' }

        logInfo(TAG, "processNextStep stringValue: $stringValue")
        if (stringValue == "0x10") {
            nextStep = Step.GpioMap
            dispatchNextStep()
        } else if (stringValue == "0x2") {
            nextStep = Step.WriteData
            dispatchNextStep()
        } else if (stringValue == "0x3" || stringValue == "0x1") {
            //TODO - do we hit here
            logInfo(TAG, "processNextStep MemDev block hit")
            //memDev
        } else {
            failUpdate("Failed update on step:$nextStep with value $stringValue")
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

    private fun sendBlock(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "sendBlock")
        return asyncBle {
            val block = otaFile?.getBlock(blockCount)
            val i = ++chunkCount
            var lastChunk = false
            if (chunkCount == block!!.size - 1) {
                chunkCount = -1
                lastChunk = true
            }

            if (lastChunk) {
                if (!lastBlock) {
                    blockCount++
                } else {
                    lastBlockSent = true
                }
                if (blockCount + 1 == otaFile?.numberOfBlocks) {
                    lastBlock = true
                }
            }

            val chunk = block[i]

            logInfo(TAG, "sendBlock-ending chunk: $chunkCount")

            val result = device.spotaService.PATCH_DATA.set(chunk).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    private fun setMemDev(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "start setMemDev... ")
        return asyncBle {
            val memType = 0x13 shl 24 or 0x00
            val result = device.spotaService.MEM_DEV.set(intToUINT32Byte(memType)).await()
            logInfo(TAG, "setMemDev result: $result")

            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    private fun setGpioMap(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "start setGpioMap... ")
        return asyncBle {
            val MISO_GPIO = 0x05
            val MOSI_GPIO = 0x06
            val CS_GPIO = 0x03
            val SCK_GPIO = 0x00
            val memInfo = MISO_GPIO shl 24 or (MOSI_GPIO shl 16) or (CS_GPIO shl 8) or SCK_GPIO
            logInfo(TAG, "setGpioMap: " + String.format("%#10x", Integer.valueOf(memInfo)))

            val result = device.spotaService.GPIO_MAP.set(intToUINT32Byte(memInfo)).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    private fun sendEndSignal(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "start sendEndSignal...")
        return asyncBle {
            //TODO - toInt may be wrong
            val result = device.spotaService.MEM_DEV.set(intToUINT32Byte(END_SIGNAL.toInt())).await()
            endSignalSent = true
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    private fun setPatchLength(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "start setPatchLength...")
        return asyncBle {
            var blocksize = 240
            if (lastBlock) {
                blocksize = otaFile?.numberOfBytes?.rem(240) ?: 0
                lastBlockReady = true
            }

            val result = device.spotaService.PATCH_LEN.set(intToUINT32Byte(blocksize)).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }

    private fun sendReboot(): Deferred<XYBluetoothResult<ByteArray>> {
        logInfo(TAG, "start sendReboot.   ")
        return asyncBle {
            //TODO - toInt may be wrong
            val result = device.spotaService.MEM_DEV.set(intToUINT32Byte(REBOOT_SIGNAL.toInt())).await()
            return@asyncBle XYBluetoothResult(result.value)
        }
    }


    enum class Step {
        MemDev, GpioMap, PatchLen, WriteData
    }

    private fun intToUINT32Byte(value: Int): ByteArray {
        val result = ByteArray(4)
        result[0] = (value and 0xFF).toByte()
        result[1] = (value shr 8 and 0xFF).toByte()
        result[2] = (value shr 16 and 0xFF).toByte()
        result[3] = (value shr 24 and 0xFF).toByte()
        return result
    }

    companion object {
        private const val TAG = "OtaUpdate"
        const val END_SIGNAL = 0xfe000000
        const val REBOOT_SIGNAL = 0xfd000000
        private const val MAX_RETRY_COUNT = 3

    }

    open class Listener {
        open fun updated(device: XYBluetoothDevice) {}
        open fun failed(device: XYBluetoothDevice, error: String) {}
    }
}