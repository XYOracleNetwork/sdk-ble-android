package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_basic.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.services.xy.BasicConfigService
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
class BasicFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_basic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_basic_refresh.setOnClickListener {
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
            ui { text_lock.text = lock.toString() }

            val unlock = it.unlock.get()
            ui { text_unlock.text = unlock.toString() }

            val major = it.major.get()
            ui { text_major.text = major.toString() }

            val minor = it.minor.get()
            ui { text_minor.text = minor.toString() }

            val uuid = it.uuid.get()
            ui { text_uuid.text = uuid.toString() }

            val reboot = it.reboot.get()
            ui { text_uuid.text = reboot.toString() }

            val interval = it.interval.get()
            ui { text_interval.text = interval.toString() }

            val lockStatus = it.lockStatus.get()
            ui { text_lock_status.text = lockStatus.toString() }

            val otaWrite = it.otaWrite.get()
            ui { text_ota_write.text = otaWrite.toString() }
        }
    }

    companion object {

        fun newInstance() =
                BasicFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : BasicFragment {
            val frag = BasicFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
