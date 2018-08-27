package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alert.*
import kotlinx.coroutines.experimental.launch
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

        if (activity?.data?.controlPoint.isNullOrEmpty() && activity?.isBusy() == false) {
            setAlertValues()
        } else {
            updateUI()
        }

    }

    private fun setAlertValues() {
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
                text_control_point.text = getString(R.string.not_supported_x2)
            }
            else -> {
                text_control_point.text = getString(R.string.unknown_device)
            }

        }
    }

    private fun updateUI() {
        ui {
            activity?.hideProgressSpinner()

            text_control_point?.text = activity?.data?.controlPoint
            text_unread_alert_status?.text = activity?.data?.unreadAlertStatus
            text_new_alert?.text = activity?.data?.newAlert
            text_new_alert_category?.text = activity?.data?.supportedNewAlertCategory
            text_unread_alert_category?.text = activity?.data?.supportedUnreadAlertCategory
        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.alertNotification.controlPoint.get().await().let { it ->
                    activity?.data?.controlPoint = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.alertNotification.unreadAlertStatus.get().await().let { it ->
                    activity?.data?.unreadAlertStatus = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.alertNotification.newAlert.get().await().let { it ->
                    activity?.data?.newAlert = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.alertNotification.supportedNewAlertCategory.get().await().let { it ->
                    activity?.data?.supportedNewAlertCategory = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.alertNotification.supportedUnreadAlertCategory.get().await().let { it ->
                    activity?.data?.supportedUnreadAlertCategory = "${it.value ?: it.error?.message
                    ?: "Error"}"
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

                device.alertNotification.controlPoint.get().await().let { it ->
                    activity?.data?.controlPoint = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.alertNotification.unreadAlertStatus.get().await().let { it ->
                    activity?.data?.unreadAlertStatus = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.alertNotification.newAlert.get().await().let { it ->
                    activity?.data?.newAlert = "${it.value ?: it.error?.message ?: "Error"}"
                }

                device.alertNotification.supportedNewAlertCategory.get().await().let { it ->
                    activity?.data?.supportedNewAlertCategory = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

                device.alertNotification.supportedUnreadAlertCategory.get().await().let { it ->
                    activity?.data?.supportedUnreadAlertCategory = "${it.value ?: it.error?.message
                    ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                AlertFragment()
    }
}
