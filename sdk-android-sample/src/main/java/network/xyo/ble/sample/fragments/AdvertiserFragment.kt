package network.xyo.ble.sample.fragments

import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import kotlinx.android.synthetic.main.fragment_advertiser.view.*
import network.xyo.ble.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.sample.R
import network.xyo.ui.XYBaseFragment
import java.util.*

class AdvertiserFragment(private val advertiser: XYBluetoothAdvertiser) : XYBaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advertiser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val updateButton = view.update
        updateButton.setOnClickListener {
            updateAdvertisingMode(view)
            updateAdvertisingPower(view)
            updateConnectible(view)
            updateTimeout(view)
            updateIncludeDeviceName(view)
            updateIncludeTxPowerLevel(view)
            updatePrimaryServiceUuid(view)
            updatePrimaryServiceData(view)
            updateManufactureId(view)
            updateManufactureData(view)
        }
    }

    private fun updateAdvertisingMode (view: View) {
        val radioButtonGroup = view.advertising_mode_selector
        val radioButtonID = radioButtonGroup.getCheckedRadioButtonId()
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            1 -> advertiser.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            2 -> advertiser.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            3 -> advertiser.changeAdvertisingMode(null)
        }
    }

    private fun updateAdvertisingPower (view: View) {
        val radioButtonGroup = view.advertising_power_selector
        val radioButtonID = radioButtonGroup.getCheckedRadioButtonId()
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            1 -> advertiser.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            2 -> advertiser.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            3 -> advertiser.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW)
            4 -> advertiser.changeAdvertisingTxLevel(null)
        }
    }

    private fun updateConnectible (view: View) {
        val radioButtonGroup = view.connectable_selector
        val radioButtonID = radioButtonGroup.getCheckedRadioButtonId()
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser.changeContactable(true)
            1 -> advertiser.changeContactable(false)
            2 -> advertiser.changeContactable(null)
        }
    }

    private fun updateTimeout(view: View) {
        try {
            val timeoutTime = Integer.parseInt(view.timeout_input.text.toString())
            advertiser.changeTimeout(timeoutTime)
        } catch (e : NumberFormatException) {
            advertiser.changeTimeout(null)
        }
    }

    private fun updateIncludeDeviceName (view: View) {
        val radioButtonGroup = view.include_device_name_selector
        val radioButtonID = radioButtonGroup.getCheckedRadioButtonId()
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser.changeIncludeDeviceName(true)
            1 -> advertiser.changeIncludeDeviceName(false)
            2 -> advertiser.changeIncludeDeviceName(null)
        }
    }

    private fun updateIncludeTxPowerLevel (view: View) {
        val radioButtonGroup = view.include_tx_power_level_selector
        val radioButtonID = radioButtonGroup.getCheckedRadioButtonId()
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser.changeIncludeTxPowerLevel(true)
            1 -> advertiser.changeIncludeTxPowerLevel(false)
            2 -> advertiser.changeIncludeTxPowerLevel(null)
        }
    }

    private fun updatePrimaryServiceUuid(view: View) {
        try {
            val uuid = ParcelUuid(UUID.fromString(view.primary_service_input.toString()))
            advertiser.chnagePrimaryService(uuid)
        } catch (e : IllegalArgumentException) {
            advertiser.chnagePrimaryService(null)
        }
    }

    private fun updatePrimaryServiceData(view: View) {
        val data = view.primary_service_data_input.toString().toByteArray()
        advertiser.changePrimaryServiceData(data)
    }

    private fun updateManufactureId(view: View) {
        try {
            val id = Integer.parseInt(view.manufacturer_id_input.text.toString())
            advertiser.changeManufacturerId(id)
        } catch (e : NumberFormatException) {
            advertiser.changeManufacturerId(null)
        }
    }

    private fun updateManufactureData(view: View) {
        val data = view.manufacturer_data_input.toString().toByteArray()
        advertiser.changePrimaryServiceData(data)
    }

    companion object {
        fun newInstance (advertiser: XYBluetoothAdvertiser) : AdvertiserFragment {
            return AdvertiserFragment(advertiser)
        }
    }
}