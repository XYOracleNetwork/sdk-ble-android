package network.xyo.ble.sample.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.BaseAdapter
import android.widget.Button
import kotlinx.android.synthetic.main.activity_xyo_ble_sample.*
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.core.XYPermissions


class XYOBleSampleActivity : XYOAppBaseActivity() {
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        logInfo("onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_xyo_ble_sample)

        progress_spinner_scanner.visibility = VISIBLE

        adapter = XYDeviceAdapter(this)
        listview!!.adapter = adapter

        val launchServerButton = findViewById<Button>(R.id.launchServer)
        launchServerButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivity(Intent(this@XYOBleSampleActivity, XYOServerActivity::class.java))
            }
        })
    }

    private fun connectListeners() {
        XY4BluetoothDevice.addGlobalListener(tag, object : XY4BluetoothDevice.Listener() {
            override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
                super.buttonSinglePressed(device)
                showToast("XY4 Button Single Pressed")
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
        adapter?.notifyDataSetChanged()
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
