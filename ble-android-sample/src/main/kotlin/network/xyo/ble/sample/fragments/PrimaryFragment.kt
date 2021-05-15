package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentPrimaryBinding
import network.xyo.ble.services.xy.PrimaryService


class PrimaryFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentPrimaryBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPrimaryBinding {
        return FragmentPrimaryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonPrimaryRefresh.setOnClickListener {
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
            activity?.runOnUiThread { binding.textAdConfig.text = adConfig.toString() }

            val buttonConfig = it.buttonConfig.get()
            activity?.runOnUiThread { binding.textButtonConfig.text = buttonConfig.toString() }

            val buzzerConfig = it.buzzerConfig.get()
            activity?.runOnUiThread { binding.textBuzzerConfig.text = buzzerConfig.toString() }

            val buzzer = it.buzzer.get()
            activity?.runOnUiThread { binding.textBuzzer.text = buzzer.toString() }

            val buttonState = it.buttonState.get()
            activity?.runOnUiThread { binding.textButtonState.text = buttonState.toString() }

            val color = it.color.get()
            activity?.runOnUiThread { binding.textColor.text = color.toString() }

            val hardwareCreateDate = it.hardwareCreateDate.get()
            activity?.runOnUiThread { binding.textHardwareCreateDate.text = hardwareCreateDate.toString() }

            val lastError = it.lastError.get()
            activity?.runOnUiThread { binding.textLastError.text = lastError.toString() }

            val leftBehind = it.leftBehind.get()
            activity?.runOnUiThread { binding.textLeftBehind.text = leftBehind.toString() }

            val lock = it.lock.get()
            activity?.runOnUiThread { binding.textLock.text = lock.toString() }

            val unlock = it.unlock.get()
            activity?.runOnUiThread { binding.textUnlock.text = unlock.toString() }

            val major = it.major.get()
            activity?.runOnUiThread { binding.textMajor.text = major.toString() }

            val minor = it.minor.get()
            activity?.runOnUiThread { binding.textMinor.text = minor.toString() }

            val reset = it.reset .get()
            activity?.runOnUiThread { binding.resetLabel.text = reset.toString() }

            val selfTest = it.selfTest.get()
            activity?.runOnUiThread { binding.textSelfTest.text = selfTest.toString() }

            val stayAwake = it.stayAwake.get()
            activity?.runOnUiThread { binding.textStayAwake.text = stayAwake.toString() }

            val uptime = it.uptime.get()
            activity?.runOnUiThread { binding.textUptime.text = uptime.toString() }

            val uuid = it.uuid.get()
            activity?.runOnUiThread { binding.textUuid.text = uuid.toString() }
        }
    }
}
