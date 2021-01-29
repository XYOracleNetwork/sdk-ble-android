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
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentGenericAccessBinding

@kotlin.ExperimentalUnsignedTypes
class GenericAccessFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentGenericAccessBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentGenericAccessBinding {
        return FragmentGenericAccessBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGenericRefresh.setOnClickListener {
            setGenericAccessValues()
        }
    }

    override fun onResume() {
        super.onResume()

        if (deviceData.deviceName.isNullOrEmpty()) {
            setGenericAccessValues()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        activity?.runOnUiThread {
            binding.textDeviceName.text = deviceData.deviceName
            binding.textAppearance.text = deviceData.appearance
            binding.textPrivacyFlag.text = deviceData.privacyFlag
            binding.textReconnectionAddress.text = deviceData.reconnectionAddress
            binding.textPeripheralParams.text = deviceData.peripheralPreferredConnectionParameters
        }
    }

    private fun setGenericAccessValues() {
        when (device) {
            is XYFinderBluetoothDevice -> {
                val xyFinder = (device as? XYFinderBluetoothDevice)
                xyFinder?.let { getValues(it) }
            }
            else -> {
                binding.textDeviceName.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getValues(device: XYFinderBluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                device.genericAccessService.deviceName.get().let {
                    deviceData.deviceName = "${it.value ?: it.error}"
                }

                device.genericAccessService.appearance.get().let {
                    deviceData.appearance = "${it.value ?: it.error}"
                }

                device.genericAccessService.privacyFlag.get().let {
                    deviceData.privacyFlag = "${it.value ?: it.error}"
                }

                device.genericAccessService.reconnectionAddress.get().let {
                    deviceData.reconnectionAddress = "${it.value ?: it.error}"
                }

                device.genericAccessService.peripheralPreferredConnectionParameters.get().let {
                    deviceData.peripheralPreferredConnectionParameters = "${it.value ?: it.error}"
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }
}
