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

        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_device_refresh.setOnClickListener {
            setDeviceValues()
        }
    }

    override fun onResume() {
        super.onResume()
        button_device_refresh.isEnabled = true
    }

    private fun setDeviceValues() {
        ui {
            button_device_refresh.isEnabled = false
            activity?.showProgressSpinner()

            text_system_id.text = ""
            text_model_number.text = ""
            text_serial_number.text = ""
            text_firmware_revision.text = ""
            text_hardware_revision.text = ""
            text_software_revision.text = ""
            text_mfg_name.text = ""
            text_ieee.text = ""
            text_pnp_id.text = ""
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
        device.connection {
            var resultInt = device.deviceInformationService.systemId.get().await()
            text_system_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            text_model_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            text_serial_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            text_firmware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            text_hardware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            text_software_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            text_mfg_name.text = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            text_ieee.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            text_pnp_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    button_device_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            var resultInt = device.deviceInformationService.systemId.get().await()
            text_system_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            text_model_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            text_serial_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            text_firmware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            text_hardware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            text_software_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            text_mfg_name.text = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            text_ieee.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            text_pnp_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    button_device_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        device.connection {
            var resultInt = device.deviceInformationService.systemId.get().await()
            text_system_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            text_model_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            text_serial_number.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            text_firmware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            text_hardware_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            text_software_revision.text = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            text_mfg_name.text = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            text_ieee.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            text_pnp_id.text = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    button_device_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    companion object {
        private const val TAG = "DeviceFragment"

        fun newInstance() =
                DeviceFragment()
    }

}
