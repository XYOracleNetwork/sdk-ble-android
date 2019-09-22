package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alert.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class AlertFragment : XYDeviceFragment() {

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

        if (deviceData?.controlPoint.isNullOrEmpty()) {
            setAlertValues()
        } else {
            updateUI()
        }

    }

    private fun setAlertValues() {
        throbber?.show()

        when (device) {
            is XY4BluetoothDevice -> {
                val x4 = (device as? XY4BluetoothDevice)
                x4?.let { getXY4Values(it) }
            }
            is XY3BluetoothDevice -> {
                val x3 = (device as? XY3BluetoothDevice)
                x3?.let { getXY3Values(it) }
            }
            is XY2BluetoothDevice -> {
                text_control_point.text = getString(R.string.not_supported_x2)
            }
            else -> {
                text_control_point.text = getString(R.string.unknown_device)
            }

        }

        throbber?.hide()
    }

    private fun updateUI() {
        ui {
            text_control_point?.text = deviceData?.controlPoint
            text_unread_alert_status?.text = deviceData?.unreadAlertStatus
            text_new_alert?.text = deviceData?.newAlert
            text_new_alert_category?.text = deviceData?.supportedNewAlertCategory
            text_unread_alert_category?.text = deviceData?.supportedUnreadAlertCategory
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        var hasConnectionError = true
        GlobalScope.launch {
            device.connection {
                hasConnectionError = false

                /*deviceData?.let {
                    it.controlPoint = device.alertNotification.controlPoint.get().format()
                    it.unreadAlertStatus = device.alertNotification.unreadAlertStatus.get().format()
                    it.newAlert = device.alertNotification.newAlert.get().format()
                    it.supportedNewAlertCategory = device.alertNotification.supportedNewAlertCategory.get().format()
                    it.supportedUnreadAlertCategory = device.alertNotification.supportedUnreadAlertCategory.get().format()
                }*/
                return@connection XYBluetoothResult(true)
            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                /*deviceData?.let {
                    it.controlPoint = device.alertNotification.controlPoint.get().format()
                    it.unreadAlertStatus = device.alertNotification.unreadAlertStatus.get().format()
                    it.newAlert = device.alertNotification.newAlert.get().format()
                    it.supportedNewAlertCategory = device.alertNotification.supportedNewAlertCategory.get().format()
                    it.supportedUnreadAlertCategory = device.alertNotification.supportedUnreadAlertCategory.get().format()
                }*/

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                AlertFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : AlertFragment {
            val frag = AlertFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }
}
