package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentExtendedConfigBinding
import network.xyo.ble.services.xy.ExtendedConfigService

@kotlin.ExperimentalUnsignedTypes
class ExtendedConfigFragment : XYDeviceFragment<FragmentExtendedConfigBinding>() {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentExtendedConfigBinding {
        return FragmentExtendedConfigBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonExtendedConfigRefresh.setOnClickListener {
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
            activity?.runOnUiThread { binding.textGpsInterval.text = gpsInterval.toString() }

            val tone = it.tone.get()
            activity?.runOnUiThread { binding.textGpsInterval.text = tone.toString() }

            val stayAwake = it.gpsInterval.get()
            activity?.runOnUiThread { binding.textStayAwake.text = stayAwake.toString() }

            val inactiveVirtualBeaconSettings = it.inactiveVirtualBeaconSettings.get()
            activity?.runOnUiThread { binding.textInactiveVirtualBeaconSettings.text = inactiveVirtualBeaconSettings.toString() }

            val inactiveInterval = it.inactiveInterval.get()
            activity?.runOnUiThread { binding.textInactiveInterval.text = inactiveInterval.toString() }

            val virtualBeaconSettings = it.virtualBeaconSettings.get()
            activity?.runOnUiThread { binding.textVirtualBeaconSettings.text = virtualBeaconSettings.toString() }

            val gpsMode = it.gpsMode.get()
            activity?.runOnUiThread { binding.textGpsMode.text = gpsMode.toString() }

            val simId = it.simId.get()
            activity?.runOnUiThread { binding.textSimId.text = simId.toString() }

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
