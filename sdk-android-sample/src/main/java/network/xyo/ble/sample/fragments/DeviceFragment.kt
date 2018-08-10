package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_device.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class DeviceFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_device_refresh.setOnClickListener {
            setDeviceValues()
        }
    }

    private fun setDeviceValues() {
        ui {
            text_system_id.text = ""
            text_model_number.text = ""
            text_serial_number.text = ""
            text_firmware_revision.text = ""
            text_hardware_revision.text = ""
            text_software_revision.text = ""
            text_mfg_name.text = ""
            text_ieee.text = ""
            text_pnp_id.text= ""
        }

        when (activity?.device) {
            is XY4BluetoothDevice -> {
                val x4 = (activity?.device as? XY4BluetoothDevice)
                x4?.let { getX4Values(it) }
            }
            is XY3BluetoothDevice -> {
                val x3 = (activity?.device as? XY3BluetoothDevice)
                x3?.let { getX3Values(it) }
            }
            is XY2BluetoothDevice -> {
                val x2 = (activity?.device as? XY2BluetoothDevice)
                x2?.let { getX2Values(it) }
            }
            else -> {
                unsupported("unknown device")
            }

        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        initServiceSetTextView(device.deviceInformationService.systemId, text_system_id)
        initServiceSetTextView(device.deviceInformationService.modelNumberString, text_model_number)
        initServiceSetTextView(device.deviceInformationService.serialNumberString, text_serial_number)
        initServiceSetTextView(device.deviceInformationService.firmwareRevisionString, text_firmware_revision)
        initServiceSetTextView(device.deviceInformationService.hardwareRevisionString, text_hardware_revision)
        initServiceSetTextView(device.deviceInformationService.softwareRevisionString, text_software_revision)
        initServiceSetTextView(device.deviceInformationService.manufacturerNameString, text_mfg_name)
        initServiceSetTextView(device.deviceInformationService.ieeeRegulatoryCertificationDataList, text_ieee)
        initServiceSetTextView(device.deviceInformationService.pnpId, text_pnp_id)
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        initServiceSetTextView(device.deviceInformationService.systemId, text_system_id)
        initServiceSetTextView(device.deviceInformationService.modelNumberString, text_model_number)
        initServiceSetTextView(device.deviceInformationService.serialNumberString, text_serial_number)
        initServiceSetTextView(device.deviceInformationService.firmwareRevisionString, text_firmware_revision)
        initServiceSetTextView(device.deviceInformationService.hardwareRevisionString, text_hardware_revision)
        initServiceSetTextView(device.deviceInformationService.softwareRevisionString, text_software_revision)
        initServiceSetTextView(device.deviceInformationService.manufacturerNameString, text_mfg_name)
        initServiceSetTextView(device.deviceInformationService.ieeeRegulatoryCertificationDataList, text_ieee)
        initServiceSetTextView(device.deviceInformationService.pnpId, text_pnp_id)
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        initServiceSetTextView(device.deviceInformationService.systemId, text_system_id)
        initServiceSetTextView(device.deviceInformationService.modelNumberString, text_model_number)
        initServiceSetTextView(device.deviceInformationService.serialNumberString, text_serial_number)
        initServiceSetTextView(device.deviceInformationService.firmwareRevisionString, text_firmware_revision)
        initServiceSetTextView(device.deviceInformationService.hardwareRevisionString, text_hardware_revision)
        initServiceSetTextView(device.deviceInformationService.softwareRevisionString, text_software_revision)
        initServiceSetTextView(device.deviceInformationService.manufacturerNameString, text_mfg_name)
        initServiceSetTextView(device.deviceInformationService.ieeeRegulatoryCertificationDataList, text_ieee)
        initServiceSetTextView(device.deviceInformationService.pnpId, text_pnp_id)
    }

    companion object {
        private const val TAG = "DeviceFragment"

        fun newInstance() =
                DeviceFragment()
    }

}
