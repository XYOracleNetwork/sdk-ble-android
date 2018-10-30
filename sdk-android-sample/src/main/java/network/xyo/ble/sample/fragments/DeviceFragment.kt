package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_device.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
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

        if (activity?.data?.systemId.isNullOrEmpty() && activity?.isBusy() == false) {
            setDeviceValues()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            activity?.hideProgressSpinner()

            text_system_id?.text = activity?.data?.systemId
            text_model_number?.text = activity?.data?.modelNumberString
            text_serial_number?.text = activity?.data?.serialNumberString
            text_firmware_revision?.text = activity?.data?.firmwareRevisionString
            text_hardware_revision?.text = activity?.data?.hardwareRevisionString
            text_software_revision?.text = activity?.data?.softwareRevisionString
            text_mfg_name?.text = activity?.data?.manufacturerNameString
            text_ieee?.text = activity?.data?.ieeeRegulatoryCertificationDataList
            text_pnp_id?.text = activity?.data?.pnpId
        }
    }

    private fun setDeviceValues() {
        ui {
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
                text_system_id.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.deviceInformationService.systemId.get().await().let { it ->
                    activity?.data?.systemId = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.deviceInformationService.modelNumberString.get().await().let { it ->
                    activity?.data?.modelNumberString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.serialNumberString.get().await().let { it ->
                    activity?.data?.serialNumberString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.firmwareRevisionString.get().await().let { it ->
                    activity?.data?.firmwareRevisionString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.hardwareRevisionString.get().await().let { it ->
                    activity?.data?.hardwareRevisionString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.softwareRevisionString.get().await().let { it ->
                    activity?.data?.softwareRevisionString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.manufacturerNameString.get().await().let { it ->
                    activity?.data?.manufacturerNameString = it.value ?: it.error?.message
                            ?: "Error"
                }

                device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().let { it ->
                    activity?.data?.ieeeRegulatoryCertificationDataList = "${it.value
                            ?: it.error?.message ?: "Error"}"
                }

                device.deviceInformationService.pnpId.get().await().let { it ->
                    activity?.data?.pnpId = "${it.value ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }


    private fun getX3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false
                var er: String? = "Error"

                device.deviceInformationService.systemId.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.systemId = "${it.value ?: er}"
                }

                device.deviceInformationService.modelNumberString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.modelNumberString = "${it.value ?: er}"
                }

                device.deviceInformationService.serialNumberString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.serialNumberString = "${it.value ?: er}"
                }

                device.deviceInformationService.firmwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.firmwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.hardwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.hardwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.softwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.softwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.manufacturerNameString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.manufacturerNameString = "${it.value ?: er}"
                }

                device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.ieeeRegulatoryCertificationDataList = "${it.value ?: er}"

                }

                device.deviceInformationService.pnpId.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.pnpId = "${it.value ?: er}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false
                var er: String? = "Error"

                device.deviceInformationService.systemId.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.systemId = "${it.value ?: er}"
                }

                device.deviceInformationService.modelNumberString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.modelNumberString = "${it.value ?: er}"
                }

                device.deviceInformationService.serialNumberString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.serialNumberString = "${it.value ?: er}"
                }

                device.deviceInformationService.firmwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.firmwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.hardwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.hardwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.softwareRevisionString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.softwareRevisionString = "${it.value ?: er}"
                }

                device.deviceInformationService.manufacturerNameString.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.manufacturerNameString = "${it.value ?: er}"
                }

                device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.ieeeRegulatoryCertificationDataList = "${it.value ?: er}"

                }

                device.deviceInformationService.pnpId.get().await().let { it ->
                    it.error?.message.let { er = it }
                    activity?.data?.pnpId = "${it.value ?: er}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                DeviceFragment()
    }

}
