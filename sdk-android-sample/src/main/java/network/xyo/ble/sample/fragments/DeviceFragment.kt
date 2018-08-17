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
        updateUI()
    }

    private fun updateUI() {
        ui {
            button_device_refresh?.isEnabled = true
            activity?.hideProgressSpinner()

            text_system_id.text = activity?.data?.systemId
            text_model_number.text = activity?.data?.modelNumberString
            text_serial_number.text = activity?.data?.serialNumberString
            text_firmware_revision.text = activity?.data?.firmwareRevisionString
            text_hardware_revision.text = activity?.data?.hardwareRevisionString
            text_software_revision.text = activity?.data?.softwareRevisionString
            text_mfg_name.text = activity?.data?.manufacturerNameString
            text_ieee.text = activity?.data?.ieeeRegulatoryCertificationDataList
            text_pnp_id.text = activity?.data?.pnpId
        }
    }

    private fun setDeviceValues() {
        ui {
            button_device_refresh.isEnabled = false
            activity?.showProgressSpinner()
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
            activity?.data?.systemId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            activity?.data?.modelNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            activity?.data?.serialNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            activity?.data?.firmwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            activity?.data?.hardwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            activity?.data?.softwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            activity?.data?.manufacturerNameString = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            activity?.data?.ieeeRegulatoryCertificationDataList = "${resultInt.value
                    ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            activity?.data?.pnpId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            var resultInt = device.deviceInformationService.systemId.get().await()
            activity?.data?.systemId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            activity?.data?.modelNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            activity?.data?.serialNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            activity?.data?.firmwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            activity?.data?.hardwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            activity?.data?.softwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            activity?.data?.manufacturerNameString = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            activity?.data?.ieeeRegulatoryCertificationDataList = "${resultInt.value
                    ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            activity?.data?.pnpId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        device.connection {
            var resultInt = device.deviceInformationService.systemId.get().await()
            activity?.data?.systemId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            var result = device.deviceInformationService.modelNumberString.get().await()
            activity?.data?.modelNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.serialNumberString.get().await()
            activity?.data?.serialNumberString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.firmwareRevisionString.get().await()
            activity?.data?.firmwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.hardwareRevisionString.get().await()
            activity?.data?.hardwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.softwareRevisionString.get().await()
            activity?.data?.softwareRevisionString = result.value ?: result.error?.message ?: "Error"

            result = device.deviceInformationService.manufacturerNameString.get().await()
            activity?.data?.manufacturerNameString = result.value ?: result.error?.message ?: "Error"

            resultInt = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await()
            activity?.data?.ieeeRegulatoryCertificationDataList = "${resultInt.value
                    ?: resultInt.error?.message ?: "Error"}"

            resultInt = device.deviceInformationService.pnpId.get().await()
            activity?.data?.pnpId = "${resultInt.value ?: resultInt.error?.message ?: "Error"}"

            ui {
                this@DeviceFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    companion object {

        fun newInstance() =
                DeviceFragment()
    }

}
