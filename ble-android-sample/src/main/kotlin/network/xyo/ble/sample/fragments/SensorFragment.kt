package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentSensorBinding
import network.xyo.ble.services.xy.SensorService
import network.xyo.ble.generic.gatt.peripheral.ble

class SensorFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentSensorBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentSensorBinding {
        return FragmentSensorBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSensorRefresh.setOnClickListener {
            ble.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var sensor: SensorService? = null
        (device as? XY3BluetoothDevice)?.let {
            sensor = it.sensorService
        }

        (device as? XY2BluetoothDevice)?.let {
            sensor = it.sensorService
        }

        sensor?.let {

            val raw = it.raw.get()
            activity?.runOnUiThread { binding.textRaw.text = raw.toString() }

            val timeout = it.timeout.get()
            activity?.runOnUiThread { binding.textTimeout.text = timeout.toString() }

            val threshold = it.threshold.get()
            activity?.runOnUiThread { binding.textThreshold.text = threshold.toString() }

            val inactive = it.inactive.get()
            activity?.runOnUiThread { binding.textInactive.text = inactive.toString() }

            val movementCount = it.movementCount.get()
            activity?.runOnUiThread { binding.textMovementCount.text = movementCount.toString() }

        }
    }
}
