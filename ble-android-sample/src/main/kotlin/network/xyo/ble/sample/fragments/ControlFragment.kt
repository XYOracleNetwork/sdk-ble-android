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
import network.xyo.ble.sample.databinding.FragmentControlBinding
import network.xyo.ble.services.xy.ControlService

@kotlin.ExperimentalUnsignedTypes
class ControlFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentControlBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentControlBinding {
        return FragmentControlBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonControlRefresh.setOnClickListener {
            GlobalScope.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var control: ControlService? = null

        (device as? XY2BluetoothDevice)?.let {
            control = it.controlService
        }

        (device as? XY3BluetoothDevice)?.let {
            control = it.controlService
        }

        control?.let {
            val buzzer = it.buzzer.get()
            activity?.runOnUiThread { binding.textBuzzer.text = buzzer.toString() }

            val handShake = it.handshake.get()
            activity?.runOnUiThread { binding.textHandShake.text = handShake.toString() }

            val version = it.version.get()
            activity?.runOnUiThread { binding.textVersion.text = version.toString() }

            val buzzerSelect = it.buzzerSelect.get()
            activity?.runOnUiThread { binding.textBuzzerSelect.text = buzzerSelect.toString() }

            val surge = it.surge.get()
            activity?.runOnUiThread { binding.textSurge.text = surge.toString() }

            val button = it.button.get()
            activity?.runOnUiThread { binding.textButton.text = button.toString() }

            val disconnect = it.disconnect.get()
            activity?.runOnUiThread { binding.textDisconnect.text = disconnect.toString() }
        }
    }
}
