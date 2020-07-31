package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.ble_stats_view.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.base.XYBase
import java.util.Date

/**
 * Created by arietrouw on 12/28/17.
 */

@kotlin.ExperimentalUnsignedTypes
class XYBLEStatsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val scanner = (context.applicationContext as XYApplication).scanner
    private var enterCount = 0
    private var exitCount = 0

    private val smartScanListener = object : XYSmartScan.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            enterCount++
            update()
        }

        override fun exited(device: XYBluetoothDevice) {
            exitCount++
            update()
        }

        override fun detected(device: XYBluetoothDevice) {
            text_pulses.text = scanner.scanResultCount.toString()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged")
            update()
        }

        override fun statusChanged(status: XYSmartScan.Status) {
            log.info("statusChanged")
            update()
        }
    }

    init {
        scanner.addListener("XYBLEStatsView", smartScanListener)
        GlobalScope.launch {
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
