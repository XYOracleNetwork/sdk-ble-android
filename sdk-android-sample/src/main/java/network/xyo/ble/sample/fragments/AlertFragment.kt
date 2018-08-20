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
        updateUI()
    }

    private fun setAlertValues() {
        ui {
            button_alert_refresh.isEnabled = false
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
                unsupported("Not supported by XY2BluetoothDevice")
            }
            else -> {
                unsupported("unknown device")
            }

        }
    }

    private fun updateUI() {
        ui {
            button_alert_refresh?.isEnabled = true
            activity?.hideProgressSpinner()

            text_control_point.text = activity?.data?.controlPoint
            text_unread_alert_status.text = activity?.data?.unreadAlertStatus
            text_new_alert.text = activity?.data?.newAlert
            text_new_alert_category.text = activity?.data?.supportedNewAlertCategory
            text_unread_alert_category.text = activity?.data?.supportedUnreadAlertCategory
        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        device.connection {
            var result = device.alertNotification.controlPoint.get().await()
            activity?.data?.controlPoint = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.unreadAlertStatus.get().await()
            activity?.data?.unreadAlertStatus = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.alertNotification.newAlert.get().await()
            activity?.data?.newAlert = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedNewAlertCategory.get().await()
            activity?.data?.supportedNewAlertCategory = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.alertNotification.supportedUnreadAlertCategory.get().await()
            activity?.data?.supportedUnreadAlertCategory = "${result.value ?: result.error?.message
            ?: "Error"}"

            this@AlertFragment.isVisible.let {
                updateUI()
            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            var result = device.alertNotification.controlPoint.get().await()
            activity?.data?.controlPoint = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.unreadAlertStatus.get().await()
            activity?.data?.unreadAlertStatus = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.alertNotification.newAlert.get().await()
            activity?.data?.newAlert = "${result.value ?: result.error?.message ?: "Error"}"

            result = device.alertNotification.supportedNewAlertCategory.get().await()
            activity?.data?.supportedNewAlertCategory = "${result.value ?: result.error?.message
            ?: "Error"}"

            result = device.alertNotification.supportedUnreadAlertCategory.get().await()
            activity?.data?.supportedUnreadAlertCategory = "${result.value ?: result.error?.message
            ?: "Error"}"

            this@AlertFragment.isVisible.let {
                updateUI()
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

        fun newInstance() =
                AlertFragment()
    }
}
