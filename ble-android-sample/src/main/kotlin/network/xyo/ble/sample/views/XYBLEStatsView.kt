package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ble_stats_view.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYApplication
import network.xyo.base.XYBase
import network.xyo.ble.generic.scanner.XYSmartScanListener
import network.xyo.ble.generic.scanner.XYSmartScanStatus
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
                text_pulses.text = scanner.scanResultCount.toString()
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
        text_host_device_name.text = scanner.hostDevice.name.toString()
        text_enters.text = enterCount.toString()
        text_exits.text = exitCount.toString()
        text_net.text = (enterCount - exitCount).toString()
        text_start_time.text = scanner.startTime?.let { Date(it).toString() } ?: "--"
        text_uptime.text = scanner.uptime?.let {("%.2f").format((it / 1000f))} ?: "--"
        text_pulses.text = scanner.scanResultCount.toString()
        text_pulses_per_second.text = scanner.resultsPerSecond?.let {("%.2f").format(it)} ?: "--"
        text_devices.text = scanner.devices.size.toString()
    }

    companion object: XYBase()
}
