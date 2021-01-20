package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYApplication
import network.xyo.base.XYBase
import network.xyo.ble.generic.scanner.XYSmartScanListener
import network.xyo.ble.generic.scanner.XYSmartScanStatus
import network.xyo.ble.sample.databinding.BleStatsViewBinding
import java.util.Date

@kotlin.ExperimentalUnsignedTypes
class XYBLEStatsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val scanner = (context.applicationContext as XYApplication).scanner
    private var enterCount = 0
    private var exitCount = 0

    private val smartScanListener = object : XYSmartScanListener() {
        override fun entered(device: XYBluetoothDevice) {
            enterCount++
            GlobalScope.launch(Dispatchers.Main) {
                update()
            }
        }

        override fun exited(device: XYBluetoothDevice) {
            exitCount++
            GlobalScope.launch(Dispatchers.Main) {
                update()
            }
        }

        override fun detected(device: XYBluetoothDevice) {
            GlobalScope.launch(Dispatchers.Main) {
                val binding = BleStatsViewBinding.bind(this@XYBLEStatsView.rootView)
                binding.textPulses.text = scanner.scanResultCount.toString()
            }
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged")
            GlobalScope.launch(Dispatchers.Main) {
                update()
            }
        }

        override fun statusChanged(status: XYSmartScanStatus) {
            log.info("statusChanged")
            GlobalScope.launch(Dispatchers.Main) {
                update()
            }
        }
    }

    init {
        scanner.addListener("XYBLEStatsView", smartScanListener)
        GlobalScope.launch(Dispatchers.Main) {
            update()
        }
    }

    fun update() {
        val binding = BleStatsViewBinding.bind(this@XYBLEStatsView)
        binding.textHostDeviceName.text = scanner.hostDevice.name.toString()
        binding.textEnters.text = enterCount.toString()
        binding.textExits.text = exitCount.toString()
        binding.textNet.text = (enterCount - exitCount).toString()
        binding.textStartTime.text = scanner.startTime?.let { Date(it).toString() } ?: "--"
        binding.textUptime.text = scanner.uptime?.let {("%.2f").format((it / 1000f))} ?: "--"
        binding.textPulses.text = scanner.scanResultCount.toString()
        binding.textPulsesPerSecond.text = scanner.resultsPerSecond?.let {("%.2f").format(it)} ?: "--"
        binding.textDevices.text = scanner.devices.size.toString()
    }

    companion object: XYBase()
}
