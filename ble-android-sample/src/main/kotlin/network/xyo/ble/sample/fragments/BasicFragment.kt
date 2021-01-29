package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentBasicBinding
import network.xyo.ble.services.xy.BasicConfigService

@kotlin.ExperimentalUnsignedTypes
class BasicFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentBasicBinding>(device, deviceData) {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentBasicBinding {
        return FragmentBasicBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBasicRefresh.setOnClickListener {
            GlobalScope.launch {
                readCharacteristics()
            }
        }
    }

    private suspend fun readCharacteristics() {
        var basic: BasicConfigService? = null
        (device as? XY3BluetoothDevice)?.let {
            basic = it.basicConfigService
        }

        (device as? XY2BluetoothDevice)?.let {
            basic = it.basicConfigService
        }

        basic?.let {

            val lock = it.lock.get()
            activity?.runOnUiThread { binding.textLock.text = lock.toString() }

            val unlock = it.unlock.get()
            activity?.runOnUiThread { binding.textUnlock.text = unlock.toString() }

            val major = it.major.get()
            activity?.runOnUiThread { binding.textMajor.text = major.toString() }

            val minor = it.minor.get()
            activity?.runOnUiThread { binding.textMinor.text = minor.toString() }

            val uuid = it.uuid.get()
            activity?.runOnUiThread { binding.textUuid.text = uuid.toString() }

            val reboot = it.reboot.get()
            activity?.runOnUiThread { binding.textReboot.text = reboot.toString() }

            val interval = it.interval.get()
            activity?.runOnUiThread { binding.textInterval.text = interval.toString() }

            val lockStatus = it.lockStatus.get()
            activity?.runOnUiThread { binding.textLockStatus.text = lockStatus.toString() }

            val otaWrite = it.otaWrite.get()
            activity?.runOnUiThread { binding.textOtaWrite.text = otaWrite.toString() }
        }
    }
}
