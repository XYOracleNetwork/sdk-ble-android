package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_device.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.gatt.XYBluetoothResult
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
                x4?.let { getInformationValues(it) }
            }
            is XY3BluetoothDevice -> {
                val x3 = (activity?.device as? XY3BluetoothDevice)
                x3?.let { getInformationValues(it) }
            }
            is XY2BluetoothDevice -> {
                val x2 = (activity?.device as? XY2BluetoothDevice)
                x2?.let { getInformationValues(it) }
            }
            else -> {
                text_system_id.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getInformationValues(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                activity?.data?.let {
                    it.systemId = device.deviceInformationService.systemId.get().await().format()
                    it.modelNumberString = device.deviceInformationService.modelNumberString.get().await().format()
                    it.serialNumberString = device.deviceInformationService.serialNumberString.get().await().format()
                    it.firmwareRevisionString = device.deviceInformationService.firmwareRevisionString.get().await().format()
                    it.hardwareRevisionString = device.deviceInformationService.hardwareRevisionString.get().await().format()
                    it.softwareRevisionString = device.deviceInformationService.softwareRevisionString.get().await().format()
                    it.manufacturerNameString = device.deviceInformationService.manufacturerNameString.get().await().format()
                    it.ieeeRegulatoryCertificationDataList = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().format()
                    it.pnpId = device.deviceInformationService.pnpId.get().await().format()
                }
            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }


    private fun getInformationValues(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                activity?.data?.let {
                    it.systemId = device.deviceInformationService.systemId.get().await().format()
                    it.modelNumberString = device.deviceInformationService.modelNumberString.get().await().format()
                    it.serialNumberString = device.deviceInformationService.serialNumberString.get().await().format()
                    it.firmwareRevisionString = device.deviceInformationService.firmwareRevisionString.get().await().format()
                    it.hardwareRevisionString = device.deviceInformationService.hardwareRevisionString.get().await().format()
                    it.softwareRevisionString = device.deviceInformationService.softwareRevisionString.get().await().format()
                    it.manufacturerNameString = device.deviceInformationService.manufacturerNameString.get().await().format()
                    it.ieeeRegulatoryCertificationDataList = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().format()
                    it.pnpId = device.deviceInformationService.pnpId.get().await().format()
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getInformationValues(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                activity?.data?.let {
                    it.systemId = device.deviceInformationService.systemId.get().await().format()
                    it.modelNumberString = device.deviceInformationService.modelNumberString.get().await().format()
                    it.serialNumberString = device.deviceInformationService.serialNumberString.get().await().format()
                    it.firmwareRevisionString = device.deviceInformationService.firmwareRevisionString.get().await().format()
                    it.hardwareRevisionString = device.deviceInformationService.hardwareRevisionString.get().await().format()
                    it.softwareRevisionString = device.deviceInformationService.softwareRevisionString.get().await().format()
                    it.manufacturerNameString = device.deviceInformationService.manufacturerNameString.get().await().format()
                    it.ieeeRegulatoryCertificationDataList = device.deviceInformationService.ieeeRegulatoryCertificationDataList.get().await().format()
                    it.pnpId = device.deviceInformationService.pnpId.get().await().format()
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
