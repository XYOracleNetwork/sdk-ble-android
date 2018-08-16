package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_generic_attribute.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class GenericAttributeFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generic_attribute, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_gatt_refresh.setOnClickListener {
            initGattValues()
        }
    }

    override fun onResume() {
        super.onResume()
        button_gatt_refresh.isEnabled = true
    }

    private fun initGattValues() {
        ui {
            button_gatt_refresh.isEnabled = false
            activity?.showProgressSpinner()

            text_service_changed.text = ""
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
                unsupported("unknown device")
            }

        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        device.connection {
            val result = device.genericAttributeService.serviceChanged.get().await()
            text_service_changed.text = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAttributeFragment.isVisible.let {
                    button_gatt_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            val result = device.genericAttributeService.serviceChanged.get().await()
            text_service_changed.text = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAttributeFragment.isVisible.let {
                    button_gatt_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    private fun getX2Values(device: XY2BluetoothDevice) {
        device.connection {
            val result = device.genericAttributeService.serviceChanged.get().await()
            text_service_changed.text = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@GenericAttributeFragment.isVisible.let {
                    button_gatt_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    companion object {
        private const val TAG = "GenericAttributeFragment"

        fun newInstance() =
                GenericAttributeFragment()
    }
}
