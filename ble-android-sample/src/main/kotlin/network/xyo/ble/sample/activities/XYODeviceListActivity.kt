package network.xyo.ble.sample.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.BaseAdapter
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.ui.ui

class XYODeviceListActivity : XYOAppBaseActivity() {
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_list)

        progress_spinner_scanner.visibility = VISIBLE

        adapter = XYDeviceAdapter(this)
        listview!!.adapter = adapter

        launchServer.setOnClickListener { startActivity(Intent(this@XYODeviceListActivity, XYOServerActivity::class.java)) }
        launchTest.setOnClickListener { startActivity(Intent(this@XYODeviceListActivity, XYOTestActivity::class.java)) }
    }

    private fun openDevice(device: XYBluetoothDevice) {
        val intent = Intent(this, XYODeviceActivity::class.java)
        intent.putExtra(XYODeviceActivity.EXTRA_DEVICEHASH, device.hash)
        this.startActivity(intent)
    }

    private fun connectListeners() {
        XY4BluetoothDevice.addGlobalListener(tag, object : XY4BluetoothDevice.Listener() {
            override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
                super.buttonSinglePressed(device)
                showToast("XY4 Button Single Pressed: ${device.address}")
                openDevice(device)
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
        XY3BluetoothDevice.addGlobalListener(tag, object : XY3BluetoothDevice.Listener() {
            override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
                super.buttonSinglePressed(device)
                showToast("XY3 Button Single Pressed: ${device.address}")
                openDevice(device)
            }
            override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
                super.buttonDoublePressed(device)
                showToast("XY3 Button Double Pressed")
            }

            override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
                super.buttonLongPressed(device)
                showToast("XY3 Button Long Pressed")
            }
        })
    }

    private fun checkStatus() {
        when (scanner.status) {
            XYSmartScan.Status.Enabled -> {
                onBluetoothEnabled()
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
            XYSmartScan.Status.None -> {
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

    private fun onBluetoothEnabled() {
        ll_disabled.visibility = GONE
        GlobalScope.launch {
            //scanner.start()
        }
    }

    private fun onBluetoothDisabled() {
        ll_disabled.visibility = VISIBLE
        GlobalScope.launch {
            scanner.stop()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        GlobalScope.launch {
            //scanner.start()
        }
    }
}
