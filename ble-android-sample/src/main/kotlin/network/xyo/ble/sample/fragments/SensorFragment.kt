package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_sensor.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.services.xy.SensorService

@kotlin.ExperimentalUnsignedTypes
class SensorFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_sensor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_sensor_refresh.setOnClickListener {
            GlobalScope.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var sensor: SensorService? = null
        (device as? XY3BluetoothDevice)?.let {
            sensor = it.sensorService
        }

        (device as? XY2BluetoothDevice)?.let {
            sensor = it.sensorService
        }

        sensor?.let {

            val raw = it.raw.get()
            activity?.runOnUiThread { text_raw.text = raw.toString() }

            val timeout = it.timeout.get()
            activity?.runOnUiThread { text_timeout.text = timeout.toString() }

            val threshold = it.threshold.get()
            activity?.runOnUiThread { text_threshold.text = threshold.toString() }

            val inactive = it.inactive.get()
            activity?.runOnUiThread { text_inactive.text = inactive.toString() }

            val movementCount = it.movementCount.get()
            activity?.runOnUiThread { text_movement_count.text = movementCount.toString() }

        }
    }

    companion object {

        fun newInstance() =
                SensorFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : SensorFragment {
            val frag = SensorFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
