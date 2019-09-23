package network.xyo.ble.sample.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.fragment_central.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.devices.xy.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYODeviceActivity
import network.xyo.ble.sample.activities.XYOTestActivity
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ui.ui

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class CentralFragment : XYDeviceFragment() {
    var adapter: BaseAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_central, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        active.setOnCheckedChangeListener {_, isChecked ->
            GlobalScope.launch {
                if (isChecked) {
                    scanner.start()
                } else {
                    scanner.stop()
                }
            }

        }

        list_view!!.adapter = adapter

        launchTest.setOnClickListener { startActivity(Intent(this@CentralFragment.context, XYOTestActivity::class.java)) }
    }

    private fun openDevice(device: XYBluetoothDevice) {
        val intent = Intent(this.context, XYODeviceActivity::class.java)
        intent.putExtra(XYODeviceActivity.EXTRA_DEVICEHASH, device.hash)
        this.startActivity(intent)
    }

    private fun connectListeners() {
        XY4BluetoothDevice.addGlobalListener(tag!!, object : XY4BluetoothDevice.Listener() {
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
        XY3BluetoothDevice.addGlobalListener(tag!!, object : XY3BluetoothDevice.Listener() {
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
            }
            XYSmartScan.Status.BluetoothUnavailable -> {
                onBluetoothUnavailable()
            }
            XYSmartScan.Status.LocationDisabled -> {
            }
            XYSmartScan.Status.None -> {
            }
        }
    }

    private fun disconnectListeners() {
        XY4BluetoothDevice.removeGlobalListener(tag!!)
    }

    override fun onResume() {
        log.info("onResume")
        super.onResume()
        connectListeners()

        Permissions.check(
                this.context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                "Location services are needed to connection and track your finders.",
                object : PermissionHandler() {
                    override fun onGranted() {
                    }
                }
        )

        Permissions.check(
                this.context,
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
    }

    private fun onBluetoothDisabled() {
        ll_disabled.visibility = VISIBLE
    }

    private fun onBluetoothUnavailable() {
        ll_device_nobluetooth.visibility = VISIBLE
    }

    companion object {
        fun newInstance (adapter: BaseAdapter) : CentralFragment {
            val frag = CentralFragment()
            frag.adapter = adapter
            return frag
        }
    }
}
