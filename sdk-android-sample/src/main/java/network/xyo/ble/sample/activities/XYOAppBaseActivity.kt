package network.xyo.ble.sample.activities

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.ui.XYBaseActivity


abstract class XYOAppBaseActivity : XYBaseActivity() {

    abstract fun onBluetoothEnabled()
    abstract fun onBluetoothDisabled()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    val scanner: XYFilteredSmartScan
        get() {
            return (this.applicationContext as XYApplication).scanner
        }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> onBluetoothDisabled()
                    BluetoothAdapter.STATE_ON -> onBluetoothEnabled()
                }
            }
        }
    }
}