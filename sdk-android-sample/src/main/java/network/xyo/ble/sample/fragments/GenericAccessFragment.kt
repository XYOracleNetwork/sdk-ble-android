package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_generic_access.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class GenericAccessFragment : XYAppBaseFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_generic_access, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_generic_refresh.setOnClickListener {
            setGenericAccessValues()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        ui {
            button_generic_refresh?.isEnabled = true
            activity?.hideProgressSpinner()

            text_device_name.text = activity?.data?.deviceName
            text_appearance.text = activity?.data?.appearance
            text_privacy_flag.text = activity?.data?.privacyFlag
            text_reconnection_address.text = activity?.data?.reconnectionAddress
            text_peripheral_params.text = activity?.data?.peripheralPreferredConnectionParameters
        }
    }

    private fun setGenericAccessValues() {
        ui {
            button_generic_refresh.isEnabled = false
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
            var result = device.genericAccessService.deviceName.get().await()
            activity?.data?.deviceName = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.appearance.get().await()
            activity?.data?.appearance = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.privacyFlag.get().await()
            activity?.data?.privacyFlag = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.reconnectionAddress.get().await()
            activity?.data?.reconnectionAddress = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.genericAccessService.peripheralPreferredConnectionParameters.get().await()
            activity?.data?.peripheralPreferredConnectionParameters = "${result.value
                    ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAccessFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            var result = device.genericAccessService.deviceName.get().await()
            activity?.data?.deviceName = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.appearance.get().await()
            activity?.data?.appearance = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.privacyFlag.get().await()
            activity?.data?.privacyFlag = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.reconnectionAddress.get().await()
            activity?.data?.reconnectionAddress = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.genericAccessService.peripheralPreferredConnectionParameters.get().await()
            activity?.data?.peripheralPreferredConnectionParameters = "${result.value
                    ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAccessFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        device.connection {
            var result = device.genericAccessService.deviceName.get().await()
            activity?.data?.deviceName = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.appearance.get().await()
            activity?.data?.appearance = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.privacyFlag.get().await()
            activity?.data?.privacyFlag = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.genericAccessService.reconnectionAddress.get().await()
            activity?.data?.reconnectionAddress = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.genericAccessService.peripheralPreferredConnectionParameters.get().await()
            activity?.data?.peripheralPreferredConnectionParameters = "${result.value
                    ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAccessFragment.isVisible.let {
                    updateUI()
                }

            }
        }
    }

    companion object {

        fun newInstance() =
                GenericAccessFragment()
    }
}
