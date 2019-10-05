package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_control.*
import kotlinx.android.synthetic.main.fragment_primary.*
import kotlinx.android.synthetic.main.fragment_primary.text_buzzer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.services.xy.ControlService
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class ControlFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_primary_refresh.setOnClickListener {
            GlobalScope.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var control: ControlService? = null

        (device as? XY2BluetoothDevice)?.let {
            control = it.controlService
        }

        (device as? XY3BluetoothDevice)?.let {
            control = it.controlService
        }

        control?.let {
            val buzzer = it.buzzer.get()
            ui { text_buzzer.text = buzzer.toString() }

            val handShake = it.handshake.get()
            ui { text_hand_shake.text = handShake.toString() }

            val version = it.version.get()
            ui { text_version.text = version.toString() }

            val buzzerSelect = it.buzzerSelect.get()
            ui { text_buzzer_select.text = buzzerSelect.toString() }

            val surge = it.surge.get()
            ui { text_surge.text = surge.toString() }

            val button = it.button.get()
            ui { text_button.text = button.toString() }

            val disconnect = it.disconnect.get()
            ui { text_disconnect.text = disconnect.toString() }
        }
    }

    companion object {

        fun newInstance() =
                ControlFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : ControlFragment {
            val frag = ControlFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
