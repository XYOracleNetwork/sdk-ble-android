package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentCurrentTimeBinding

@kotlin.ExperimentalUnsignedTypes
class CurrentTimeFragment : XYDeviceFragment<FragmentCurrentTimeBinding>() {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentCurrentTimeBinding {
        return FragmentCurrentTimeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTimeRefresh.setOnClickListener {
            setTimeValues()
        }
    }

    override fun onResume() {
        super.onResume()

        if (deviceData?.currentTime.isNullOrEmpty()) {
            setTimeValues()
        } else {
            updateUI()
        }

    }

    private fun updateUI() {
        activity?.runOnUiThread {
            binding.textCurrentTime.text = deviceData?.currentTime
            binding.textLocalTimeInformation.text = deviceData?.localTimeInformation
            binding.textReferenceTimeInformation.text = deviceData?.referenceTimeInformation
        }
    }

    private fun setTimeValues() {
        when (device) {
            is XY4BluetoothDevice -> {
                val x4 = (device as? XY4BluetoothDevice)
                x4?.let { getXY4Values(it) }
            }
            is XY3BluetoothDevice -> {
                val x3 = (device as? XY3BluetoothDevice)
                x3?.let { getXY3Values(it) }
            }
            is XY2BluetoothDevice -> {
                binding.textCurrentTime.text = getString(R.string.not_supported_x2)
            }
            else -> {
                binding.textCurrentTime.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.currentTime = device.currentTimeService.currentTime.get().format()
                    it.localTimeInformation = device.currentTimeService.localTimeInformation.get().format()
                    it.referenceTimeInformation = device.currentTimeService.referenceTimeInformation.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }


    private fun getXY3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.currentTime = device.currentTimeService.currentTime.get().format()
                    it.localTimeInformation = device.currentTimeService.localTimeInformation.get().format()
                    it.referenceTimeInformation = device.currentTimeService.referenceTimeInformation.get().format()
                }

                return@connection XYBluetoothResult(true)
            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                CurrentTimeFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : CurrentTimeFragment {
            val frag = CurrentTimeFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
