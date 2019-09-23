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
            ui { text_ad_config.text = adConfig.display }

            val buttonConfig = it.buttonConfig.get()
            ui { text_button_config.text = buttonConfig.display }

            val buzzerConfig = it.buzzerConfig.get()
            ui { text_buzzer_config.text = buzzerConfig.display }

            val buzzer = it.buzzer.get()
            ui { text_buzzer.text = buzzer.display }

            val buttonState = it.buttonState.get()
            ui { text_button_state.text = buttonState.display }

            val color = it.color.get()
            ui { text_color.text = color.display }

            val hardwareCreateDate = it.hardwareCreateDate.get()
            ui { text_hardware_create_date.text = hardwareCreateDate.display }

            val lastError = it.lastError.get()
            ui { text_last_error.text = lastError.display }

            val leftBehind = it.leftBehind.get()
            ui { text_left_behind.text = leftBehind.display }

            val lock = it.lock.get()
            ui { text_lock.text = lock.display }

            val unlock = it.unlock.get()
            ui { text_unlock.text = unlock.display }

            val major = it.major.get()
            ui { text_major.text = major.display }

            val minor = it.minor.get()
            ui { text_minor.text = minor.display }

            val reset = it.reset .get()
            ui { text_reset.text = reset.display }

            val selfTest = it.selfTest.get()
            ui { text_self_test.text = selfTest.display }

            val stayAwake = it.stayAwake.get()
            ui { text_stay_awake.text = stayAwake.display }

            val uptime = it.uptime.get()
            ui { text_uptime.text = uptime.display }

            val uuid = it.uuid.get()
            ui { text_uuid.text = uuid.display }
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
