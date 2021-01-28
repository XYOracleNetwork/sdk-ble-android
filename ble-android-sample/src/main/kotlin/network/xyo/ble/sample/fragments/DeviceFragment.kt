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
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentDeviceBinding

@kotlin.ExperimentalUnsignedTypes
class DeviceFragment : XYDeviceFragment<FragmentDeviceBinding>() {

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
        device?.reporter?.removeListener("DeviceFragment")
    }

    override fun onResume() {
        super.onResume()

        device?.reporter?.addListener("DeviceFragment", object: XYBluetoothDeviceListener() {
            override fun detected(device: XYBluetoothDevice) {
                updateUI()
                super.detected(device)
            }
        })

        if (deviceData?.systemId.isNullOrEmpty()) {
            setDeviceValues()
        } else {
            updateUI()
        }
    }

    fun updateUI() {
        activity?.runOnUiThread {


            binding.textSystemId.text = deviceData?.systemId
            binding.textModelNumber.text = deviceData?.modelNumberString
            binding.textSerialNumber.text = deviceData?.serialNumberString
            binding.textFirmwareRevision.text = deviceData?.firmwareRevisionString
            binding.textHardwareRevision.text = deviceData?.hardwareRevisionString
            binding.textSoftwareRevision.text = deviceData?.softwareRevisionString
            binding.textMfgName.text = deviceData?.manufacturerNameString
            binding.textIeee.text = deviceData?.ieeeRegulatoryCertificationDataList
            binding.textPnpId.text = deviceData?.pnpId

        }
    }

    private fun setDeviceValues() {

        when (device) {
            is XY4BluetoothDevice -> {
                val x4 = (device as? XY4BluetoothDevice)
                x4?.let { getInformationValues(it) }
            }
            is XY3BluetoothDevice -> {
                val x3 = (device as? XY3BluetoothDevice)
                x3?.let { getInformationValues(it) }
            }
            is XY2BluetoothDevice -> {
                val x2 = (device as? XY2BluetoothDevice)
                x2?.let { getInformationValues(it) }
            }
            else -> {
                binding.textSystemId.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getInformationValues(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
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


    private fun getInformationValues(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
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

    private fun getInformationValues(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
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

    companion object {

        fun newInstance() =
                DeviceFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : DeviceFragment {
            val frag = DeviceFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
