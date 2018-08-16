package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alert.*
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui

//TODO - this is server only?
class AlertFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_alert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_alert_refresh.setOnClickListener {
            setAlertValues()
        }
    }

    override fun onResume() {
        super.onResume()
        button_alert_refresh.isEnabled = true
    }

    private fun setAlertValues() {
        ui {
            button_alert_refresh.isEnabled = false
            activity?.showProgressSpinner()

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
        device.connection {
            var result = device.alertNotification.controlPoint.get().await()
            text_control_point.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.unreadAlertStatus.get().await()
            text_unread_alert_status.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.newAlert.get().await()
            text_new_alert.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedNewAlertCategory.get().await()
            text_new_alert_category.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedUnreadAlertCategory.get().await()
            text_unread_alert_category.text = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@AlertFragment.isVisible.let {
                    button_alert_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }

            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            var result = device.alertNotification.controlPoint.get().await()
            text_control_point.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.unreadAlertStatus.get().await()
            text_unread_alert_status.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.newAlert.get().await()
            text_new_alert.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedNewAlertCategory.get().await()
            text_new_alert_category.text = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedUnreadAlertCategory.get().await()
            text_unread_alert_category.text = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@AlertFragment.isVisible.let {
                    button_alert_refresh?.isEnabled = true
                    activity?.hideProgressSpinner()
                }
            }
        }
    }

    override fun unsupported(text: String) {
        super.unsupported(text)
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
