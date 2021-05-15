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
import network.xyo.ble.sample.ui
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
            ui { binding.textLock.text = lock.toString() }

            val unlock = it.unlock.get()
            ui { binding.textUnlock.text = unlock.toString() }

            val major = it.major.get()
            ui { binding.textMajor.text = major.toString() }

            val minor = it.minor.get()
            ui { binding.textMinor.text = minor.toString() }

            val uuid = it.uuid.get()
            ui { binding.textUuid.text = uuid.toString() }

            val reboot = it.reboot.get()
            ui { binding.textReboot.text = reboot.toString() }

            val interval = it.interval.get()
            ui { binding.textInterval.text = interval.toString() }

            val lockStatus = it.lockStatus.get()
            ui { binding.textLockStatus.text = lockStatus.toString() }

            val otaWrite = it.otaWrite.get()
            ui { binding.textOtaWrite.text = otaWrite.toString() }
        }
    }
}
