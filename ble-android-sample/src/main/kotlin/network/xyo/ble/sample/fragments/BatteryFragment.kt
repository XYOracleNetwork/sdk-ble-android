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
import network.xyo.ble.sample.databinding.FragmentBatteryBinding

@kotlin.ExperimentalUnsignedTypes
class BatteryFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentBatteryBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentBatteryBinding {
        return FragmentBatteryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBatteryRefresh.setOnClickListener {
            readBatteryLevel()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        activity?.runOnUiThread {
            binding.textBatteryLevel.text = deviceData.level
        }
    }

    private fun readBatteryLevel() {
        when (device) {
            is XY4BluetoothDevice -> {
                val xy4 = (device as? XY4BluetoothDevice)
                xy4?.let {
                    getXY4Values(xy4)
                }
            }
            is XY3BluetoothDevice -> {
                val xy3 = (device as? XY3BluetoothDevice)
                xy3?.let {
                    getXY3Values(xy3)
                }
            }
            is XY2BluetoothDevice -> {
                val xy2 = (device as? XY2BluetoothDevice)
                xy2?.let {
                    getXY2Values(xy2)
                }
            }
            else -> {
                binding.textBatteryLevel.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false
                deviceData.level = device.batteryService.level.get().format()

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
                deviceData.level = device.batteryService.level.get().format()

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY2Values(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false
                deviceData.level = device.batteryService.level.get().format()

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }
}
