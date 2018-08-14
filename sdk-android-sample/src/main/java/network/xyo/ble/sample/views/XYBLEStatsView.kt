package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYApplication

import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.core.XYBase

/**
 * Created by arietrouw on 12/28/17.
 */

class XYBLEStatsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val scanner : XYFilteredSmartScan

    private val smartScanListener = object : XYFilteredSmartScan.Listener() {
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
            XYBase.logInfo(TAG, "connectionStateChanged")
        }

        override fun statusChanged(status: XYFilteredSmartScan.BluetoothStatus) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    init {
        scanner = (context.applicationContext as XYApplication).scanner
        scanner.addListener(TAG, smartScanListener)
    }

    fun update() {
        post {
            val uptimeView = findViewById<TextView>(R.id.text_uptime)!!
            uptimeView.text = ("%.2f").format(scanner.uptimeSeconds)

            val pulseCountView = findViewById<TextView>(R.id.text_pulses)!!
            pulseCountView.text = scanner.scanResultCount.toString()

            val pulsePerSecView = findViewById<TextView>(R.id.text_pulses_per_second)!!
            pulsePerSecView.text = ("%.2f").format(scanner.resultsPerSecond)

            val devices = findViewById<TextView>(R.id.text_devices)
            devices.text = scanner.devices.size.toString()
        }
    }

    companion object {
        private val TAG = XYBLEStatsView::class.java.simpleName
    }
}
