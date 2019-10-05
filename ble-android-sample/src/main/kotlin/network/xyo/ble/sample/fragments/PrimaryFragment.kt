package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_primary.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.services.xy.PrimaryService
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class PrimaryFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_primary, container, false)
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
        var primary: PrimaryService? = null
        (device as? XY4BluetoothDevice)?.let {
            primary = it.primary
        }

        primary?.let {
            val adConfig = it.adConfig.get()
            ui { text_ad_config.text = adConfig.toString() }

            val buttonConfig = it.buttonConfig.get()
            ui { text_button_config.text = buttonConfig.toString() }

            val buzzerConfig = it.buzzerConfig.get()
            ui { text_buzzer_config.text = buzzerConfig.toString() }

            val buzzer = it.buzzer.get()
            ui { text_buzzer.text = buzzer.toString() }

            val buttonState = it.buttonState.get()
            ui { text_button_state.text = buttonState.toString() }

            val color = it.color.get()
            ui { text_color.text = color.toString() }

            val hardwareCreateDate = it.hardwareCreateDate.get()
            ui { text_hardware_create_date.text = hardwareCreateDate.toString() }

            val lastError = it.lastError.get()
            ui { text_last_error.text = lastError.toString() }

            val leftBehind = it.leftBehind.get()
            ui { text_left_behind.text = leftBehind.toString() }

            val lock = it.lock.get()
            ui { text_lock.text = lock.toString() }

            val unlock = it.unlock.get()
            ui { text_unlock.text = unlock.toString() }

            val major = it.major.get()
            ui { text_major.text = major.toString() }

            val minor = it.minor.get()
            ui { text_minor.text = minor.toString() }

            val reset = it.reset .get()
            ui { text_reset.text = reset.toString() }

            val selfTest = it.selfTest.get()
            ui { text_self_test.text = selfTest.toString() }

            val stayAwake = it.stayAwake.get()
            ui { text_stay_awake.text = stayAwake.toString() }

            val uptime = it.uptime.get()
            ui { text_uptime.text = uptime.toString() }

            val uuid = it.uuid.get()
            ui { text_uuid.text = uuid.toString() }
        }
    }

    companion object {

        fun newInstance() =
                PrimaryFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : PrimaryFragment {
            val frag = PrimaryFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
