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
import network.xyo.ble.gatt.peripheral.XYBluetoothError
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.gatt.peripheral.asyncBle
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

                bleAdvertiser.startAdvertising(makeAdvertisingSettings(), makeAdvertisingData(), primaryCallback)
            }
            return@asyncBle XYBluetoothResult(startCode)
        }

        return@asyncBle XYBluetoothResult<Int>(XYBluetoothError("No bluetoothLe Advertiser!"))
    }

    fun stopAdvertising ()  {
        bleAdvertiser?.stopAdvertising(primaryCallback)
    }

    open fun changeManufacturerData (newManufacturerData : ByteArray?) {
        manufacturerData = newManufacturerData
    }

    open fun changeManufacturerId (newManufacturerId : Int?) {
        manufacturerId = newManufacturerId
    }

    open fun changePrimaryServiceData (newPrimaryServiceData: ByteArray?) {
        primaryServiceData = newPrimaryServiceData
    }

    open fun changePrimaryService (newPrimaryService : ParcelUuid?) {
        primaryService = newPrimaryService
    }

    open fun changeIncludeTxPowerLevel (newIncludeTxPowerLevel : Boolean?) {
        includeTxPowerLevel = newIncludeTxPowerLevel
    }

    open fun changeIncludeDeviceName (newIncludeDeviceName : Boolean?)  {
        includeDeviceName = newIncludeDeviceName
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

    private fun restart () : Deferred<XYBluetoothResult<Int>> {
        stopAdvertising()
        return startAdvertising()
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

    protected open fun makeAdvertisingData () : AdvertiseData {
        val builder = AdvertiseData.Builder()

        includeDeviceName?.let {
            builder.setIncludeDeviceName(it)
        }

        includeTxPowerLevel?.let {
            builder.setIncludeTxPowerLevel(it)
        }

        primaryService?.let {
            builder.addServiceUuid(it)

            primaryServiceData?.let {
                builder.addServiceData(primaryService, primaryServiceData)
            }
        }

        manufacturerId?.let {
            builder.addManufacturerData(it, manufacturerData)
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