package network.xyo.ble.sample.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.BaseAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.activity_xyo_ble_sample.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.ui.ui
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions


class XYOBleSampleActivity : XYOAppBaseActivity() {
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_xyo_ble_sample)

        progress_spinner_scanner.visibility = VISIBLE

        adapter = XYDeviceAdapter(this)
        listview!!.adapter = adapter

        val launchServerButton = findViewById<Button>(R.id.launchServer)
        launchServerButton.setOnClickListener { startActivity(Intent(this@XYOBleSampleActivity, XYOServerActivity::class.java)) }
    }

    private fun connectListeners() {
        XY4BluetoothDevice.addGlobalListener(tag, object : XY4BluetoothDevice.Listener() {
            override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
                super.buttonSinglePressed(device)
                showToast("XY4 Button Single Pressed: ${device.address}")
            }

            override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
                super.buttonDoublePressed(device)
                showToast("XY4 Button Double Pressed")
            }

            override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
                super.buttonLongPressed(device)
                showToast("XY4 Button Long Pressed")
            }
        })
    }

    private fun checkStatus() {
        when (scanner.status) {
            XYSmartScan.Status.Enabled -> {
            }
            XYSmartScan.Status.BluetoothDisabled -> {
                onBluetoothDisabled()
                progress_spinner_scanner.visibility = GONE
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Bluetooth Disabled")
                alertDialog.setMessage("Please enable Bluetooth to see a list of devices.")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            XYSmartScan.Status.BluetoothUnavailable -> {
                onBluetoothDisabled()
                progress_spinner_scanner.visibility = GONE
                val alertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Bluetooth Unavailable")
                alertDialog.setMessage("It seems like your device may not support Bluetooth, or you are using an emulator")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            XYSmartScan.Status.LocationDisabled -> {
            }
        }
    }

    private fun disconnectListeners() {
        XY4BluetoothDevice.removeGlobalListener(tag)
    }

    override fun onResume() {
        log.info("onResume")
        super.onResume()
        connectListeners()

        Permissions.check(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
                "Location services are needed to connection and track your finders.",
            object : PermissionHandler() {
                override fun onGranted() {
                }
            }
        )

        Permissions.check(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                "Allow app to access your storage in order to load firmware files?",
                object : PermissionHandler() {
                    override fun onGranted() {
                    }
                }
        )

        ui { adapter?.notifyDataSetChanged() }

        checkStatus()
    }

    override fun onPause() {
        super.onPause()
        disconnectListeners()
    }

    override fun onBluetoothEnabled() {
        ll_disabled.visibility = GONE
        GlobalScope.async {
            scanner.start()
        }
    }

    override fun onBluetoothDisabled() {
        ll_disabled.visibility = VISIBLE
        GlobalScope.async {
            scanner.stop()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        GlobalScope.async {
            scanner.start()
        }
    }
}
