package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_generic_access.*
import kotlinx.coroutines.experimental.launch
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

        if (activity?.data?.deviceName.isNullOrEmpty() && activity?.isBusy() == false) {
            setGenericAccessValues()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            activity?.hideProgressSpinner()

            text_device_name?.text = activity?.data?.deviceName
            text_appearance?.text = activity?.data?.appearance
            text_privacy_flag?.text = activity?.data?.privacyFlag
            text_reconnection_address?.text = activity?.data?.reconnectionAddress
            text_peripheral_params?.text = activity?.data?.peripheralPreferredConnectionParameters
        }
    }

    private fun setGenericAccessValues() {
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
                text_device_name.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.genericAccessService.deviceName.get().await().let { it ->
                    activity?.data?.deviceName = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.appearance.get().await().let { it ->
                    activity?.data?.appearance = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.privacyFlag.get().await().let { it ->
                    activity?.data?.privacyFlag = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.reconnectionAddress.get().await().let { it ->
                    activity?.data?.reconnectionAddress = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.genericAccessService.peripheralPreferredConnectionParameters.get().await().let { it ->
                    activity?.data?.peripheralPreferredConnectionParameters = "${it.value
                            ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.genericAccessService.deviceName.get().await().let { it ->
                    activity?.data?.deviceName = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.appearance.get().await().let { it ->
                    activity?.data?.appearance = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.privacyFlag.get().await().let { it ->
                    activity?.data?.privacyFlag = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.reconnectionAddress.get().await().let { it ->
                    activity?.data?.reconnectionAddress = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.genericAccessService.peripheralPreferredConnectionParameters.get().await().let { it ->
                    activity?.data?.peripheralPreferredConnectionParameters = "${it.value
                            ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.genericAccessService.deviceName.get().await().let { it ->
                    activity?.data?.deviceName = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.appearance.get().await().let { it ->
                    activity?.data?.appearance = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.privacyFlag.get().await().let { it ->
                    activity?.data?.privacyFlag = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.genericAccessService.reconnectionAddress.get().await().let { it ->
                    activity?.data?.reconnectionAddress = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.genericAccessService.peripheralPreferredConnectionParameters.get().await().let { it ->
                    activity?.data?.peripheralPreferredConnectionParameters = "${it.value
                            ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                GenericAccessFragment()
    }
}
