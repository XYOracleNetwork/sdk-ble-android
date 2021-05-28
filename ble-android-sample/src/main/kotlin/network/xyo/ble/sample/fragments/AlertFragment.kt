package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentAlertBinding
import network.xyo.ble.generic.gatt.peripheral.ble

class AlertFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentAlertBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentAlertBinding {
        return FragmentAlertBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAlertRefresh.setOnClickListener {
            setAlertValues()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setAlertValues() {
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
                binding.textControlPoint.text = getString(R.string.not_supported_x2)
            }
            else -> {
                binding.textControlPoint.text = getString(R.string.unknown_device)
            }

        }
    }

    private fun updateUI() {
        activity?.runOnUiThread {
            binding.textControlPoint.text = deviceData.controlPoint
            binding.textUnreadAlertStatus.text = deviceData.unreadAlertStatus
            binding.textNewAlert.text = deviceData.newAlert
            binding.textNewAlertCategory.text = deviceData.supportedNewAlertCategory
            binding.textUnreadAlertCategory.text = deviceData.supportedUnreadAlertCategory
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        var hasConnectionError = true
        ble.launch {
            device.connection {
                hasConnectionError = false

                deviceData.let {
                    it.controlPoint = device.alertNotification.controlPoint.get().format()
                    it.unreadAlertStatus = device.alertNotification.unreadAlertStatus.get().format()
                    it.newAlert = device.alertNotification.newAlert.get().format()
                    it.supportedNewAlertCategory = device.alertNotification.supportedNewAlertCategory.get().format()
                    it.supportedUnreadAlertCategory = device.alertNotification.supportedUnreadAlertCategory.get().format()
                }
                return@connection XYBluetoothResult(true)
            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY3Values(device: XY3BluetoothDevice) {
        ble.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData.let {
                    it.controlPoint = device.alertNotification.controlPoint.get().format()
                    it.unreadAlertStatus = device.alertNotification.unreadAlertStatus.get().format()
                    it.newAlert = device.alertNotification.newAlert.get().format()
                    it.supportedNewAlertCategory = device.alertNotification.supportedNewAlertCategory.get().format()
                    it.supportedUnreadAlertCategory = device.alertNotification.supportedUnreadAlertCategory.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }
}
