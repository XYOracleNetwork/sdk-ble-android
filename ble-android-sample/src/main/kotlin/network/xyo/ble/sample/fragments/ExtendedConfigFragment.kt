package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_extended_config.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.services.xy.ExtendedConfigService
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class ExtendedConfigFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_extended_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_extended_config_refresh.setOnClickListener {
            GlobalScope.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var config: ExtendedConfigService? = null

        (device as? XY2BluetoothDevice)?.let {
            config = it.extendedConfigService
        }

        (device as? XY3BluetoothDevice)?.let {
            config = it.extendedConfigService
        }

        config?.let {
            val gpsInterval = it.gpsInterval.get()
            ui { text_gps_interval.text = gpsInterval.toString() }

            val tone = it.tone.get()
            ui { text_gps_interval.text = tone.toString() }

            val stayAwake = it.gpsInterval.get()
            ui { text_stay_awake.text = stayAwake.toString() }

            val inactiveVirtualBeaconSettings = it.inactiveVirtualBeaconSettings.get()
            ui { text_inactive_virtual_beacon_settings.text = inactiveVirtualBeaconSettings.toString() }

            val inactiveInterval = it.inactiveInterval.get()
            ui { text_inactive_interval.text = inactiveInterval.toString() }

            val virtualBeaconSettings = it.virtualBeaconSettings.get()
            ui { text_virtual_beacon_settings.text = virtualBeaconSettings.toString() }

            val gpsMode = it.gpsMode.get()
            ui { text_gps_mode.text = gpsMode.toString() }

            val simId = it.simId.get()
            ui { text_sim_id.text = simId.toString() }

        }
    }

    companion object {

        fun newInstance() =
                ExtendedConfigFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : ExtendedConfigFragment {
            val frag = ExtendedConfigFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
