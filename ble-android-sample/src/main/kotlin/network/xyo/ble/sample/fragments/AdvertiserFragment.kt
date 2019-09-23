package network.xyo.ble.sample.fragments

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import kotlinx.android.synthetic.main.fragment_advertiser.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.generic.gatt.server.XYIBeaconAdvertiseDataCreator
import network.xyo.ble.sample.R
import network.xyo.base.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui
import java.nio.ByteBuffer
import java.util.*

class AdvertiserFragment : XYBaseFragment() {
    private var isInIBeacon = false
    var advertiser: XYBluetoothAdvertiser? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advertiser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val updateButton = view.update
        val iBeaconSwitch = view.sw_is_ibeacon

        updateButton.setOnClickListener {
            GlobalScope.launch {
                updateAdvertisingMode(view)
                updateAdvertisingPower(view)
                updateConnectible(view)
                updateTimeout(view)
                if(updateAdvertiserData(view)) {
                    restartAdvertiser()
                }

            }
        }

        iBeaconSwitch.setOnCheckedChangeListener { _, isChecked ->
            isInIBeacon = isChecked
        }
    }

    private suspend fun restartAdvertiser () {
        advertiser?.stopAdvertising()
        val status = advertiser?.startAdvertising()


        ui {
            if (status?.value == 0 && status.error == null) {
                showToast("Success!")
                return@ui
            }

            showToast("Error ${status?.error}!")
        }
    }

    private fun updateAdvertisingMode (view: View) {
        val radioButtonGroup = view.advertising_mode_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            1 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            2 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            3 -> advertiser?.changeAdvertisingMode(null)

        }
    }

    private fun updateAdvertisingPower (view: View) {
        val radioButtonGroup = view.advertising_power_selector
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

    private  fun updateConnectible (view: View) {
        val radioButtonGroup = view.connectable_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> advertiser?.changeContactable(true)
            1 -> advertiser?.changeContactable(false)
        }
    }

    private fun updateTimeout(view: View) {
        try {
            val timeoutTime = Integer.parseInt(view.timeout_input.text.toString())
            advertiser?.changeTimeout(timeoutTime)
        } catch (e : NumberFormatException) {
            advertiser?.changeTimeout(null)
        }
    }

    private fun getIncludeDeviceName (view: View) : Boolean {
        val radioButtonGroup = view.include_device_name_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> return true
        }

        return false
    }

    private fun updateAdvertiserData (view: View) : Boolean {
        if (isInIBeacon) {
            return updateAdvertiserDataIBeacon(view)
        }
        return updateAdvertiserDataRegular(view)
    }

    private fun updateAdvertiserDataIBeacon (view: View) : Boolean {

        try {
            val manufacturerId = Integer.parseInt(view.ibeacon_manufacturer_id_input.text.toString())
            val major = Integer.parseInt(view.ibeacon_major.text.toString())
            val minor = Integer.parseInt(view.ibeacon_minor.text.toString())
            val uuid = UUID.fromString(view.ibeacon_primary_service_input.text.toString())



            val data = XYIBeaconAdvertiseDataCreator.create(
                    ByteBuffer.allocate(2).putShort(major.toShort()).array(),
                    ByteBuffer.allocate(2).putShort(minor.toShort()).array(),
                    uuid,
                    manufacturerId,
                    getIncludeDeviceName(view))

            advertiser?.advertisingData = data.build()
            return true

        } catch (e: NumberFormatException) {
            showToast("Error Phrasing Malefactor ID")
        }  catch (e : IllegalArgumentException) {
            ui {
                showToast("Error Phrasing Primary Service UUID")
            }
        }
        return false
    }


    private fun updateAdvertiserDataRegular (view: View) : Boolean {
        val builder = AdvertiseData.Builder()
        try {
            val uuid = ParcelUuid(UUID.fromString(view.standard_primary_service_input.toString()))
            builder.addServiceUuid(uuid)
            builder.addServiceData(uuid, view.standard_primary_service_data_input.toString().toByteArray())
            val id = Integer.parseInt(view.standard_manufacturer_id_input.text.toString())
            val data = view.standard_manufacturer_data_input.text.toString().toByteArray()
            builder.addManufacturerData(id, data)
        } catch (e : NumberFormatException) {
            ui { showToast("Error Phrasing Malefactor ID") }
            return false
        } catch (e : IllegalArgumentException) {
            ui { showToast("Error Phrasing Primary Service UUID") }
            return false
        }

        builder.setIncludeTxPowerLevel(getIncludeTx(view))
        builder.setIncludeDeviceName(getIncludeDeviceName(view))
        advertiser?.advertisingData = builder.build()
        return true
    }

    private fun getIncludeTx (view: View) : Boolean {
        val radioButtonGroup = view.include_tx_power_level_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)

        return when(radioButtonGroup.indexOfChild(radioButton)) {
            0 -> true
            1 -> false
            else -> false
        }
    }



    companion object : XYBase() {
        fun newInstance(regularAdvertiser: XYBluetoothAdvertiser?) : AdvertiserFragment {
            val frag = AdvertiserFragment()
            frag.advertiser = regularAdvertiser
            return frag
        }

        fun newInstance () : AdvertiserFragment {
            return AdvertiserFragment()
        }
    }
}