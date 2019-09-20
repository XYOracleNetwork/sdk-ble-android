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
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class BatteryFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_battery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_battery_refresh.setOnClickListener {
            readBatteryLevel()
        }
    }

    override fun onResume() {
        super.onResume()

        if (deviceData?.level.isNullOrEmpty()) {
            readBatteryLevel()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            throbber?.hide()

            text_battery_level?.text = deviceData?.level
        }
    }

    private fun readBatteryLevel() {
        throbber?.show()

        when (device) {
            is XY4BluetoothDevice -> {
                val x4 = (device as? XY4BluetoothDevice)
                x4?.let {
                    getXY4Values(x4)
                }
            }
            is XY3BluetoothDevice -> {
                val x3 = (device as? XY3BluetoothDevice)
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

        throbber?.hide()
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.level = device.batteryService.level.get().await().format()
                }

                return@connection XYBluetoothResult(true)

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

                deviceData?.let {
                    it.level = device.batteryService.level.get().await().format()
                }

                return@connection XYBluetoothResult(true)

            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                BatteryFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : BatteryFragment {
            val frag = BatteryFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
