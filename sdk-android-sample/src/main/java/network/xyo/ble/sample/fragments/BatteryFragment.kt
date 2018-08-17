package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_battery.*
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
        updateUI()
    }

    private fun updateUI() {
        ui {
            button_battery_refresh?.isEnabled = true
            activity?.hideProgressSpinner()

            text_battery_level.text = activity?.data?.level
        }
    }

    private fun setBatteryLevel() {
        logInfo("batteryButton: onClick")
        ui {
            button_battery_refresh.isEnabled = false
            activity?.showProgressSpinner()
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
                unsupported("Not supported by XY2BluetoothDevice")
            }
            else -> {
                unsupported("unknown device")
            }
        }
    }

    private fun getX4Values(device: XY4BluetoothDevice) {
        device.connection {
            val result = device.batteryService.level.get().await()
            activity?.data?.level = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@BatteryFragment.isVisible.let {
                    updateUI()
                }
            }
        }
    }

    private fun getX3Values(device: XY3BluetoothDevice) {
        device.connection {
            val result = device.batteryService.level.get().await()
            activity?.data?.level = "${result.value ?: result.error?.message ?: "Error"}"

            ui {
                this@BatteryFragment.isVisible.let {
                    updateUI()
                }
            }
        }
    }

    companion object {

        fun newInstance() =
                BatteryFragment()
    }

}
