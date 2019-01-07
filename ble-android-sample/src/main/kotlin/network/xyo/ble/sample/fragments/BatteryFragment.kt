package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_battery.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY2BluetoothDevice
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class BatteryFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_battery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_battery_refresh.setOnClickListener {
            setBatteryLevel()
        }
    }

    override fun onResume() {
        super.onResume()

        if (activity?.data?.level.isNullOrEmpty() && activity?.isBusy() == false) {
            setBatteryLevel()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            activity?.hideProgressSpinner()

            text_battery_level?.text = activity?.data?.level
        }
    }

    private fun setBatteryLevel() {
        ui {
            activity?.showProgressSpinner()
        }

        when (activity?.device) {
            is XY4BluetoothDevice -> {
                val x4 = (activity?.device as? XY4BluetoothDevice)
                x4?.let {
                    getXY4Values(x4)
                }
            }
            is XY3BluetoothDevice -> {
                val x3 = (activity?.device as? XY3BluetoothDevice)
                x3?.let {
                    getXY3Values(x3)
                }
            }
            is XY2BluetoothDevice -> {
                text_battery_level.text = getString(R.string.not_supported_x2)
            }
            else -> {
                text_battery_level.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.batteryService.level.get().await().let { it ->
                    activity?.data?.level = "${it.value ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                device.batteryService.level.get().await().let { it ->
                    activity?.data?.level = "${it.value ?: it.error?.message ?: "Error"}"
                }

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                BatteryFragment()
    }

}
