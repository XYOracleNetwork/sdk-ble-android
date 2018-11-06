package network.xyo.ble.gatt.server

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertiseSettings.*
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.Deferred
import network.xyo.ble.gatt.XYBluetoothBase
import network.xyo.ble.gatt.XYBluetoothError
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume

open class XYBluetoothAdvertiser(context: Context) : XYBluetoothBase(context){
    protected val listeners = HashMap<String, AdvertiseCallback>()
    private val bleAdvertiser : BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser

    protected var advertisingMode : Int? = null
    protected var advertisingTxLever : Int? = null
    protected var connectible : Boolean? = null
    protected var timeout : Int? = null

    protected var includeDeviceName : Boolean? = null
    protected var includeTxPowerLevel : Boolean? = null
    protected var primaryService : ParcelUuid? = null
    protected var primaryServiceData : ByteArray? = null
    protected var manufacturerId : Int? = null
    protected var manufacturerData : ByteArray? = null

    protected fun addListener (key: String, listener : AdvertiseCallback) {
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
                bleAdvertiser.startAdvertising(getAdvertisingSettings(), getAdvertisingData(), primaryCallback)
            }
            return@asyncBle XYBluetoothResult(startCode)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No bluetoothLe Advertiser!"))
    }

    fun stopAdvertising ()  {
        bleAdvertiser?.stopAdvertising(primaryCallback)
    }

    fun changeManufacturerData (newManufacturerData : ByteArray?, restart: Boolean) = asyncBle {
        manufacturerData = newManufacturerData

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeManufacturerId (newManufacturerId : Int?, restart: Boolean) = asyncBle {
        manufacturerId = newManufacturerId

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changePrimaryServiceData (newPrimaryServiceData: ByteArray?, restart: Boolean) = asyncBle {
        primaryServiceData = newPrimaryServiceData

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun chnagePrimaryService (newPrimaryService : ParcelUuid?, restart: Boolean) = asyncBle {
        primaryService = newPrimaryService

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeIncludeTxPowerLevel (newIncludeTxPowerLevel : Boolean?, restart: Boolean) = asyncBle {
        includeTxPowerLevel = newIncludeTxPowerLevel

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeIncludeDeviceName (newIncludeDeviceName : Boolean?, restart: Boolean) = asyncBle {
        includeDeviceName = newIncludeDeviceName

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeContactable (newConnectible : Boolean?, restart : Boolean) = asyncBle {
        connectible = newConnectible

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeTimeout (newTimeout : Int?, restart : Boolean) = asyncBle {
        timeout = newTimeout

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeAdvertisingMode (newAdvertisingMode : Int?, restart: Boolean) = asyncBle {
        if (newAdvertisingMode != null) {
            if (!(newAdvertisingMode == ADVERTISE_MODE_BALANCED
                    || newAdvertisingMode == ADVERTISE_MODE_LOW_LATENCY
                    || newAdvertisingMode == ADVERTISE_MODE_LOW_POWER)) {
                return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Invalid Advertising Mode"))
            }
        }
        advertisingMode = newAdvertisingMode

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    fun changeAdvertisingTxLevel (newAdvertisingTxLevel : Int?, restart: Boolean) = asyncBle {
        if (newAdvertisingTxLevel != null) {
            if (!(newAdvertisingTxLevel == ADVERTISE_TX_POWER_HIGH
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_LOW
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_MEDIUM
                    || newAdvertisingTxLevel == ADVERTISE_TX_POWER_ULTRA_LOW)) {
                return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("Invalid Advertising Tx Level"))
            }
        }
        advertisingTxLever = newAdvertisingTxLevel

        if (restart) {
            return@asyncBle restart().await()
        }
        return@asyncBle XYBluetoothResult(0, null)
    }

    private fun restart () : Deferred<XYBluetoothResult<Int>> {
        stopAdvertising()
        return startAdvertising()
    }

    private fun getAdvertisingSettings() : AdvertiseSettings {
        return makeAdvertisingSettings(advertisingMode, connectible, timeout, advertisingTxLever)
    }

    private fun getAdvertisingData() : AdvertiseData {
        return makeAdvertisingData(includeDeviceName, includeTxPowerLevel, primaryService, primaryServiceData, manufacturerId, manufacturerData)
    }

    private fun makeAdvertisingSettings (advertisingMode : Int?,
                                         connectible : Boolean?,
                                         timeout: Int?,
                                         advertisingTxLever : Int?) : AdvertiseSettings {

        val builder = AdvertiseSettings.Builder()

        if (advertisingMode != null) {
            builder.setAdvertiseMode(advertisingMode)
        }

        if (connectible != null) {
            builder.setConnectable(connectible)
        }

        if (timeout != null) {
            builder.setTimeout(timeout)
        }

        if (advertisingTxLever != null) {
            builder.setTxPowerLevel(advertisingTxLever)
        }

        return builder.build()
    }

    private fun makeAdvertisingData (includeDeviceName : Boolean?,
                                     includeTxPowerLevel : Boolean?,
                                     primaryService : ParcelUuid?,
                                     primaryServiceData : ByteArray?,
                                     manufacturerId : Int?,
                                     manufacturerData : ByteArray?) : AdvertiseData {

        val builder = AdvertiseData.Builder()

        if (includeDeviceName != null) {
            builder.setIncludeDeviceName(includeDeviceName)
        }

        if (includeTxPowerLevel != null) {
            builder.setIncludeTxPowerLevel(includeTxPowerLevel)
        }

        if (primaryService != null) {
            builder.addServiceUuid(primaryService)

            if (primaryServiceData != null) {
                builder.addServiceData(primaryService, primaryServiceData)
            }
        }

        if (manufacturerId != null) {
            builder.addManufacturerData(manufacturerId, manufacturerData)
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