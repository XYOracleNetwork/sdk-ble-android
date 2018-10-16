package network.xyo.ble.sample.activities

import android.Manifest
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_xyo_ble_sample.*
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.core.XYPermissions
import network.xyo.ui.ui


class XYOBleSampleActivity : XYOAppBaseActivity() {
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        logInfo("onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_xyo_ble_sample)

        progress_spinner_scanner.visibility = VISIBLE

        adapter = XYDeviceAdapter(this)
        listview!!.adapter = adapter
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
            XYFilteredSmartScan.Status.Enabled -> {
            }
            XYFilteredSmartScan.Status.BluetoothDisabled -> {
                showToast("Bluetooth Disabled")
                progress_spinner_scanner.visibility = GONE
            }
            XYFilteredSmartScan.Status.BluetoothUnavailable -> {
                showToast("Bluetooth Unavailable")
                progress_spinner_scanner.visibility = GONE
            }
            XYFilteredSmartScan.Status.LocationDisabled -> {
            }
        }
    }

    private fun disconnectListeners() {
        XY4BluetoothDevice.removeGlobalListener(tag)
    }

    override fun onResume() {
        logInfo("onResume")
        super.onResume()
        connectListeners()
        val permissions = XYPermissions(this)
        permissions.requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                "Location services are needed to connection and track your finders.",
                XYPermissions.LOCATION_PERMISSIONS_REQ_CODE)


        permissions.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Allow app to access your storage in order to load firmware files?", 0)

        ui {  adapter?.notifyDataSetChanged() }

        checkStatus()
    }

    override fun onPause() {
        super.onPause()
        disconnectListeners()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        scanner.start()
    }
}
