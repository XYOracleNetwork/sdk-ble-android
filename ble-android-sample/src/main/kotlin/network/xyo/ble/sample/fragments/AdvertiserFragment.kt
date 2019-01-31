package network.xyo.ble.sample.fragments

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
import network.xyo.ble.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYOServerActivity
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui
import java.util.*

class AdvertiserFragment : XYBaseFragment() {
    val advertiser: XYBluetoothAdvertiser?
        get () = (activity as? XYOServerActivity)?.bleAdvertiser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advertiser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val updateButton = view.update
        updateButton.setOnClickListener {
            GlobalScope.launch {
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
                restartAdvertiser()
            }
        }
    }

    private suspend fun restartAdvertiser () {
        val status = advertiser?.startAdvertising()?.await()


        ui {
            if (status?.value == 0 && status.error == null) {
                showToast("Success!")
                return@ui
            }

            showToast("Error ${status?.error}!")
        }
    }

    private suspend fun updateAdvertisingMode (view: View) {
        val radioButtonGroup = view.advertising_mode_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY, false)?.await()
            1 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED, false)?.await()
            2 -> advertiser?.changeAdvertisingMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER, false)?.await()
            3 -> advertiser?.changeAdvertisingMode(null, false)?.await()

        }
    }

    private suspend fun updateAdvertisingPower (view: View) {
        val radioButtonGroup = view.advertising_power_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH, false)?.await()
            1 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM, false)?.await()
            2 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH, false)?.await()
            3 -> advertiser?.changeAdvertisingTxLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW, false)?.await()
            4 -> advertiser?.changeAdvertisingTxLevel(null, false)?.await()
        }
    }

    private suspend  fun updateConnectible (view: View) {
        val radioButtonGroup = view.connectable_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser?.changeContactable(true, false)?.await()
            1 -> advertiser?.changeContactable(false, false)?.await()
            2 -> advertiser?.changeContactable(null, false)?.await()
        }
    }

    private suspend fun updateTimeout(view: View) {
        try {
            val timeoutTime = Integer.parseInt(view.timeout_input.text.toString())
            advertiser?.changeTimeout(timeoutTime, false)?.await()
        } catch (e : NumberFormatException) {
            advertiser?.changeTimeout(null, false)?.await()
        }
    }

    private suspend fun updateIncludeDeviceName (view: View) {
        val radioButtonGroup = view.include_device_name_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser?.changeIncludeDeviceName(true, false)?.await()
            1 -> advertiser?.changeIncludeDeviceName(false, false)?.await()
            2 -> advertiser?.changeIncludeDeviceName(null, false)?.await()
        }
    }

    private suspend fun updateIncludeTxPowerLevel (view: View) {
        val radioButtonGroup = view.include_tx_power_level_selector
        val radioButtonID = radioButtonGroup.checkedRadioButtonId
        val radioButton = radioButtonGroup.findViewById<RadioButton>(radioButtonID)
        val idx = radioButtonGroup.indexOfChild(radioButton)

        when(idx) {
            0 -> advertiser?.changeIncludeTxPowerLevel(true, false)?.await()
            1 -> advertiser?.changeIncludeTxPowerLevel(false, false)?.await()
            2 -> advertiser?.changeIncludeTxPowerLevel(null, false)?.await()
        }
    }

    private suspend fun updatePrimaryServiceUuid(view: View) {
        try {
            val uuid = ParcelUuid(UUID.fromString(view.primary_service_input.toString()))
            advertiser?.changePrimaryService(uuid, false)?.await()
        } catch (e : IllegalArgumentException) {
            advertiser?.changePrimaryService(null, false)?.await()
        }
    }

    private suspend fun updatePrimaryServiceData(view: View) {
        val data = view.primary_service_data_input.toString().toByteArray()
        advertiser?.changePrimaryServiceData(data, false)?.await()
    }

    private suspend fun updateManufactureId(view: View) {
        try {
            val id = Integer.parseInt(view.manufacturer_id_input.text.toString())
            advertiser?.changeManufacturerId(id, false)?.await()
        } catch (e : NumberFormatException) {
            advertiser?.changeManufacturerId(null, false)?.await()
        }
    }

    private suspend fun updateManufactureData(view: View) {
        val data = view.manufacturer_data_input.toString().toByteArray()
        advertiser?.changePrimaryServiceData(data, false)?.await()
    }

    companion object {
        fun newInstance() : AdvertiserFragment {
            return AdvertiserFragment()
        }
    }
}