package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_alert.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.services.Service
import network.xyo.ui.ui

//TODO - this is server only?
class AlertFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_alert_refresh.setOnClickListener {
            setAlertValues()
        }
    }

    private fun setAlertValues() {
        ui {
            button_alert_refresh.isEnabled = false

            text_control_point.text = ""
            text_unread_alert_status.text = ""
            text_new_alert.text = ""
            text_new_alert_category.text = ""
            text_unread_alert_category.text = ""
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

    private fun getX4Values(device: XY4BluetoothDevice) {
        initServiceSetTextView(device.alertNotification.unreadAlertStatus, text_control_point)
        initServiceSetTextView(device.alertNotification.newAlert, text_unread_alert_status)
        initServiceSetTextView(device.alertNotification.supportedNewAlertCategory, text_new_alert_category)
        initServiceSetTextView(device.alertNotification.supportedUnreadAlertCategory, text_unread_alert_category)
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        initServiceSetTextView(device.alertNotification.unreadAlertStatus, text_control_point)
        initServiceSetTextView(device.alertNotification.newAlert, text_unread_alert_status)
        initServiceSetTextView(device.alertNotification.supportedNewAlertCategory, text_new_alert_category)
        initServiceSetTextView(device.alertNotification.supportedUnreadAlertCategory, text_unread_alert_category)
    }

    override fun unsupported(text: String) {
        super.unsupported(text)
        ui {
            button_alert_refresh.isEnabled = true
        }
    }

    override fun initServiceSetTextView(service: Service.IntegerCharacteristic, textView: TextView) {
        super.initServiceSetTextView(service, textView)
        ui {
            button_alert_refresh.isEnabled = true
        }
    }

    companion object {
        private const val TAG = "AlertFragment"

        fun newInstance() =
                AlertFragment()
    }
}
