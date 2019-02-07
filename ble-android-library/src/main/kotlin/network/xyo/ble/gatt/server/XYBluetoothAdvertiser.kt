package network.xyo.ble.gatt.server

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.*
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import network.xyo.ble.XYBluetoothBase
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.gatt.peripheral.asyncBle
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

open class XYBluetoothAdvertiser(context: Context, var advertisingData : AdvertiseData?) : XYBluetoothBase(context){
    protected val listeners = HashMap<String, AdvertiseCallback>()
    private val bleAdvertiser : BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser

    protected var advertisingMode : Int? = null
    protected var advertisingTxLever : Int? = null
    protected var connectible : Boolean? = null
    protected var timeout : Int? = null
    protected var includeTxPowerLevel : Boolean? = null

    fun addListener (key: String, listener : AdvertiseCallback) {
        listeners[key] = listener
    }

    protected fun removeListener (key: String) {
        listeners.remove(key)
    }

    fun startAdvertising () = asyncBle {
        if (bleAdvertiser != null) {
            val startCode = suspendCoroutine<Int> { cont ->
                addListener("startAdvertising", object : AdvertiseCallback() {
                    override fun onStartFailure(errorCode: Int) {
                        removeListener("startAdvertising")
                        cont.resume(errorCode)
                    }

                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                        removeListener("startAdvertising")
                        cont.resume(0)
                    }
                })

                bleAdvertiser.startAdvertising(makeAdvertisingSettings(), advertisingData, primaryCallback)
            }

            if (startCode == 0) {
                return@asyncBle XYBluetoothResult(startCode)
            }

            when(startCode) {
                AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> {
                    return@asyncBle XYBluetoothResult(startCode, XYBluetoothError("ADVERTISE_FAILED_ALREADY_STARTED"))
                }

                AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> {
                    return@asyncBle XYBluetoothResult(startCode, XYBluetoothError("ADVERTISE_FAILED_DATA_TOO_LARGE"))
                }

                AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> {
                    return@asyncBle XYBluetoothResult(startCode, XYBluetoothError("ADVERTISE_FAILED_FEATURE_UNSUPPORTED"))
                }

                AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> {
                    return@asyncBle XYBluetoothResult(startCode, XYBluetoothError("ADVERTISE_FAILED_INTERNAL_ERROR"))
                }

                AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> {
                    return@asyncBle XYBluetoothResult(startCode, XYBluetoothError("ADVERTISE_FAILED_TOO_MANY_ADVERTISERS"))
                }
            }
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No bluetoothLe Advertiser!"))
    }

    fun stopAdvertising ()  {
        bleAdvertiser?.stopAdvertising(primaryCallback)
    }

    /*private fun restart () : Deferred<XYBluetoothResult<Int>> {
        stopAdvertising()
        return startAdvertising()
    }*/

    open fun changeIncludeTxPowerLevel (newIncludeTxPowerLevel : Boolean?) {
        includeTxPowerLevel = newIncludeTxPowerLevel
    }

    open fun changeContactable (newConnectible : Boolean) {
        connectible = newConnectible
    }

    open fun changeTimeout (newTimeout : Int?)  {
        timeout = newTimeout
    }

    open fun changeAdvertisingMode (newAdvertisingMode : Int?) : Boolean {
        if (newAdvertisingMode != null) {
            if (!(newAdvertisingMode == ADVERTISE_MODE_BALANCED
                    || newAdvertisingMode == ADVERTISE_MODE_LOW_LATENCY
                    || newAdvertisingMode == ADVERTISE_MODE_LOW_POWER)) {
                return false
            }
        }
        advertisingMode = newAdvertisingMode

        return true
    }

    open fun changeAdvertisingTxLevel (newAdvertisingTxLevel : Int?) : Boolean {
        if (newAdvertisingTxLevel != null) {
            if (!(newAdvertisingTxLevel == ADVERTISE_TX_POWER_HIGH
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_LOW
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_MEDIUM
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_ULTRA_LOW)) {
                return false
            }
        }

        advertisingTxLever = newAdvertisingTxLevel

        return true
    }


    protected open fun makeAdvertisingSettings () : AdvertiseSettings {
        val builder = AdvertiseSettings.Builder()

        advertisingMode?.let {
            builder.setAdvertiseMode(it)
        }

        connectible?.let {
            builder.setConnectable(it)
        }

        timeout?.let {
            builder.setTimeout(it)
        }

        advertisingTxLever?.let {
            builder.setTxPowerLevel(it)
        }

        return builder.build()
    }


    private val primaryCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            for ((_, listener) in listeners) {
                listener.onStartFailure(errorCode)
            }
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)

            for ((_, listener) in listeners) {
                listener.onStartSuccess(settingsInEffect)
            }
        }
    }
}