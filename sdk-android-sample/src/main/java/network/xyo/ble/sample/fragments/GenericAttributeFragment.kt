package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_generic_attribute.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class GenericAttributeFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_generic_attribute, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_gatt_refresh.setOnClickListener {
            setGattValues()
        }
    }

    override fun onResume() {
        super.onResume()

        if (activity?.data?.serviceChanged.isNullOrEmpty() && activity?.isBusy() == false) {
            setGattValues()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            activity?.hideProgressSpinner()

            text_service_changed?.text = activity?.data?.serviceChanged
        }
    }

    private fun setGattValues() {
        ui {
            activity?.hideProgressSpinner()
        }

        when (activity?.device) {
            is XY4BluetoothDevice -> {
                val x4 = (activity?.device as? XY4BluetoothDevice)
                x4?.let {
                    getX4Values(x4)

                }
            }
            is XY3BluetoothDevice -> {
                val x3 = (activity?.device as? XY3BluetoothDevice)
                x3?.let {
                    getX3Values(x3)
                }
            }
            is XY2BluetoothDevice -> {
                val x2 = (activity?.device as? XY2BluetoothDevice)
                x2?.let {
                    getX2Values(x2)
                }
            }
            else -> {
                text_service_changed.text = getString(R.string.unknown_device)
            }

        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.genericAttributeService.serviceChanged.get().await().let { it ->
                    activity?.data?.serviceChanged = "${it.value ?: it.error?.message ?: "Error"}"
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

                device.genericAttributeService.serviceChanged.get().await().let { it ->
                    activity?.data?.serviceChanged = "${it.value ?: it.error?.message ?: "Error"}"
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

                device.genericAttributeService.serviceChanged.get().await().let { it ->
                    activity?.data?.serviceChanged = "${it.value ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                GenericAttributeFragment()
    }
}
