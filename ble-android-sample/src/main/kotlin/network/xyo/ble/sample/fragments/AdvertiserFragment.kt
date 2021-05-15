package network.xyo.ble.sample.fragments

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.generic.gatt.server.XYIBeaconAdvertiseDataCreator
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.sample.databinding.FragmentAdvertiserBinding
import java.nio.ByteBuffer
import java.util.UUID

@ExperimentalUnsignedTypes
class AdvertiserFragment(private var advertiser: XYBluetoothAdvertiser?) : XYAppBaseFragment<FragmentAdvertiserBinding>() {
    private var isInIBeacon = false

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentAdvertiserBinding {
        return FragmentAdvertiserBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.update.setOnClickListener {
            GlobalScope.launch {
                updateAdvertisingMode()
                updateAdvertisingPower()
                updateConnectible()
                updateTimeout()
                if(updateAdvertiserData()) {
                    restartAdvertiser()
                }

            }
        }

        binding.swIsIbeacon.setOnCheckedChangeListener { _, isChecked ->
            isInIBeacon = isChecked
        }
    }

    private suspend fun restartAdvertiser () {
        advertiser?.stopAdvertising()
        val status = advertiser?.startAdvertising()


        activity?.runOnUiThread {
            if (status?.value == 0 && status.error == XYBluetoothResultErrorCode.None) {
                log.info("Success!")
                return@runOnUiThread
            }

            log.error("Error ${status?.error}!")
        }
    }

    private fun updateAdvertisingMode () {
        val radioButtonGroup = binding.advertisingModeSelector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            1 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            2 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            3 -> advertiser?.changeAdvertisingMode(null)

        }
    }

    private fun updateAdvertisingPower () {
        val radioButtonGroup = binding.advertisingPowerSelector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            1 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            2 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            3 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            4 -> advertiser?.changeAdvertisingTxLevel(null)
        }
    }

    private  fun updateConnectible () {
        val radioButtonGroup = binding.connectibleSelector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> advertiser?.changeContactable(true)
            1 -> advertiser?.changeContactable(false)
        }
    }

    private fun updateTimeout() {
        try {
            val timeoutTime = Integer.parseInt(binding.timeoutInput.text.toString())
            advertiser?.changeTimeout(timeoutTime)
        } catch (e : NumberFormatException) {
            advertiser?.changeTimeout(null)
        }
    }

    private fun getIncludeDeviceName () : Boolean {
        val radioButtonGroup = binding.includeDeviceNameSelector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> return true
        }

        return false
    }

    private fun updateAdvertiserData () : Boolean {
        if (isInIBeacon) {
            return updateAdvertiserDataIBeacon()
        }
        return updateAdvertiserDataRegular()
    }

    private fun updateAdvertiserDataIBeacon () : Boolean {

        try {
            val manufacturerId = Integer.parseInt(binding.ibeaconManufacturerIdInput.text.toString())
            val major = Integer.parseInt(binding.ibeaconMajor.text.toString())
            val minor = Integer.parseInt(binding.ibeaconMinor.text.toString())
            val uuid = UUID.fromString(binding.ibeaconPrimaryServiceInput.text.toString())



            val data = XYIBeaconAdvertiseDataCreator.create(
                    ByteBuffer.allocate(2).putShort(major.toShort()).array(),
                    ByteBuffer.allocate(2).putShort(minor.toShort()).array(),
                    uuid,
                    manufacturerId,
                    getIncludeDeviceName())

            advertiser?.advertisingData = data.build()
            return true

        } catch (e: NumberFormatException) {
            log.error("Error Phrasing Malefactor ID")
        }  catch (e : IllegalArgumentException) {
            activity?.runOnUiThread {
                log.error("Error Phrasing Primary Service UUID")
            }
        }
        return false
    }


    private fun updateAdvertiserDataRegular () : Boolean {
        val builder = AdvertiseData.Builder()
        try {
            val uuid = ParcelUuid(UUID.fromString(binding.standardPrimaryServiceInput.toString()))
            builder.addServiceUuid(uuid)
            builder.addServiceData(uuid, binding.standardPrimaryServiceDataInput.toString().toByteArray())
            val id = Integer.parseInt(binding.standardManufacturerIdInput.text.toString())
            val data = binding.standardManufacturerDataInput.text.toString().toByteArray()
            builder.addManufacturerData(id, data)
        } catch (e : NumberFormatException) {
            activity?.runOnUiThread { log.error("Error Phrasing Malefactor ID") }
            return false
        } catch (e : IllegalArgumentException) {
            activity?.runOnUiThread { log.error("Error Phrasing Primary Service UUID") }
            return false
        }

        builder.setIncludeTxPowerLevel(getIncludeTx())
        builder.setIncludeDeviceName(getIncludeDeviceName())
        advertiser?.advertisingData = builder.build()
        return true
    }

    private fun getIncludeTx () : Boolean {
        val radioButtonGroup = binding.includeTxPowerLevelSelector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        return when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> true
            1 -> false
            else -> false
        }
    }
}
