package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_battery.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData

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
        activity?.runOnUiThread {
            text_battery_level?.text = deviceData?.level
        }
    }

    private fun readBatteryLevel() {
        when (device) {
            is XY4BluetoothDevice -> {
                val xy4 = (device as? XY4BluetoothDevice)
                xy4?.let {
                    getXY4Values(xy4)
                }
            }
            is XY3BluetoothDevice -> {
                val xy3 = (device as? XY3BluetoothDevice)
                xy3?.let {
                    getXY3Values(xy3)
                }
            }
            is XY2BluetoothDevice -> {
                val xy2 = (device as? XY2BluetoothDevice)
                xy2?.let {
                    getXY2Values(xy2)
                }
            }
            else -> {
                text_battery_level.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.level = device.batteryService.level.get().format()
                }

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

                deviceData?.let {
                    it.level = device.batteryService.level.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY2Values(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.level = device.batteryService.level.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

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
