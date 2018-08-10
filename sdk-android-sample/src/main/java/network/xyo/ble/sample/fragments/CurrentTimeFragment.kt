package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_current_time.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class CurrentTimeFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_time_refresh.setOnClickListener {
            setTimeValues()
        }
    }

    private fun setTimeValues() {
        ui {
            //button_time_refresh.isEnabled = false
            activity?.showProgressSpinner()

            text_localTimeInformation.text = ""
            text_currentTime.text = ""
            text_referenceTimeInformation.text = ""
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
                unsupported("Not supported by XY2BluetoothDevice")
            }
            else -> {
                unsupported("unknown device")
            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        initServiceSetTextView(device.currentTimeService.currentTime, text_currentTime)
        initServiceSetTextView(device.currentTimeService.localTimeInformation, text_localTimeInformation)
        initServiceSetTextView(device.currentTimeService.referenceTimeInformation, text_referenceTimeInformation)
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        initServiceSetTextView(device.currentTimeService.currentTime, text_currentTime)
        initServiceSetTextView(device.currentTimeService.localTimeInformation, text_localTimeInformation)
        initServiceSetTextView(device.currentTimeService.referenceTimeInformation, text_referenceTimeInformation)
    }

    companion object {

        fun newInstance() =
                CurrentTimeFragment()
    }

}
