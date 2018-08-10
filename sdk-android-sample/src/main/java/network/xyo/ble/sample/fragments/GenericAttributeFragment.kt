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

    private fun initGattValues() {
        ui {
            text_service_changed.text = ""
        }

        when (activity?.device) {
            is XY4BluetoothDevice -> {
                val x4 = (activity?.device as? XY4BluetoothDevice)
                x4?.let {
                    initServiceSetTextView(x4.genericAttributeService.serviceChanged, text_service_changed)
                }
            }
            is XY3BluetoothDevice -> {
                val x3 = (activity?.device as? XY3BluetoothDevice)
                x3?.let {
                    initServiceSetTextView(x3.genericAttributeService.serviceChanged, text_service_changed)
                }
            }
            is XY2BluetoothDevice -> {
                val x2 = (activity?.device as? XY3BluetoothDevice)
                x2?.let {
                    initServiceSetTextView(x2.genericAttributeService.serviceChanged, text_service_changed)
                }
            }
            else -> {
                unsupported("unknown device")
            }

        }
    }

    companion object {
        private const val TAG = "GenericAttributeFragment"

        fun newInstance() =
                GenericAttributeFragment()
    }
}
