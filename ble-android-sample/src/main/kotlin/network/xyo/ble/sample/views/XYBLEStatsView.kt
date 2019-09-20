package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.blestats_view.view.*
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.base.XYBase

/**
 * Created by arietrouw on 12/28/17.
 */

@kotlin.ExperimentalUnsignedTypes
class XYBLEStatsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val scanner = (context.applicationContext as XYApplication).scanner

    private val smartScanListener = object : XYSmartScan.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            update()
        }

        override fun exited(device: XYBluetoothDevice) {
            update()
        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged")
        }

        override fun statusChanged(status: XYSmartScan.Status) {
            log.info("statusChanged")
        }
    }

    init {
        scanner.addListener("XYBLEStatsView", smartScanListener)
    }

    fun update() {
        post {
            text_uptime.text = ("%.2f").format(scanner.uptimeSeconds)
            text_pulses.text = scanner.scanResultCount.toString()
            text_pulses_per_second.text = ("%.2f").format(scanner.resultsPerSecond)
            text_devices.text = scanner.devices.size.toString()
        }
    }

    companion object: XYBase()
}
