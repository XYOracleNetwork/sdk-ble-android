package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentDeviceBinding

@kotlin.ExperimentalUnsignedTypes
class DeviceFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentDeviceBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentDeviceBinding {
        return FragmentDeviceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDeviceRefresh.setOnClickListener {
            setDeviceValues()
        }
    }

    override fun onPause() {
        super.onPause()
        device.reporter.removeListener("DeviceFragment")
    }

    override fun onResume() {
        super.onResume()

        device.reporter.addListener("DeviceFragment", object: XYBluetoothDeviceListener() {
            override fun detected(device: XYBluetoothDevice) {
                updateUI()
                super.detected(device)
            }
        })

        updateUI()
    }

    fun updateUI() {
        activity?.runOnUiThread {


            binding.textSystemId.text = deviceData.systemId
            binding.textModelNumber.text = deviceData.modelNumberString
            binding.textSerialNumber.text = deviceData.serialNumberString
            binding.textFirmwareRevision.text = deviceData.firmwareRevisionString
            binding.textHardwareRevision.text = deviceData.hardwareRevisionString
            binding.textSoftwareRevision.text = deviceData.softwareRevisionString
            binding.textMfgName.text = deviceData.manufacturerNameString
            binding.textIeee.text = deviceData.ieeeRegulatoryCertificationDataList
            binding.textPnpId.text = deviceData.pnpId

        }
    }

    private fun setDeviceValues() {

        when (device) {
            is XYFinderBluetoothDevice -> {
                val xyFinder = (device as? XYFinderBluetoothDevice)
                xyFinder?.let { getInformationValues(it) }
            }
            else -> {
                binding.textSystemId.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getInformationValues(device: XYFinderBluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData.let {
                    it.systemId = device.deviceInformationService.systemId.get().format()
                    it.modelNumberString = device.deviceInformationService.modelNumberString.get().format()
                    it.serialNumberString = device.deviceInformationService.serialNumberString.get().format()
                    it.firmwareRevisionString = device.deviceInformationService.firmwareRevisionString.get().format()
                    it.hardwareRevisionString = device.deviceInformationService.hardwareRevisionString.get().format()
                    it.softwareRevisionString = device.deviceInformationService.softwareRevisionString.get().format()
                    it.manufacturerNameString = device.deviceInformationService.manufacturerNameString.get().format()
                    it.ieeeRegulatoryCertificationDataList = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().format()
                    it.pnpId = device.deviceInformationService.pnpId.get().format()
                }

                return@connection XYBluetoothResult(true)
            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

}
