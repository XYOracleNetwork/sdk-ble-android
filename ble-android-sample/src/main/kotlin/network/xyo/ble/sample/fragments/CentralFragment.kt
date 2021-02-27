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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.*
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.activities.XYOTestActivity
import network.xyo.ble.generic.scanner.XYSmartScanStatus
import network.xyo.ble.sample.databinding.FragmentCentralBinding

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class CentralFragment(var adapter: BaseAdapter) : XYAppBaseFragment<FragmentCentralBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentCentralBinding {
        return FragmentCentralBinding.inflate(inflater, container, false)
    }

    private fun enableDevices() {
        XY4BluetoothDevice.enable(true)
        XY3BluetoothDevice.enable(true)
        XY2BluetoothDevice.enable(true)
        XYBluetoothDevice.enable(true, canCreate = true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        enableDevices()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.active.setOnCheckedChangeListener {_, isChecked ->
            GlobalScope.launch {
                if (isChecked) {
                    scanner.start()
                } else {
                    scanner.stop()
                }
            }

        }

        binding.listView.adapter = adapter

        binding.launchTest.setOnClickListener { startActivity(Intent(this@CentralFragment.context, XYOTestActivity::class.java)) }
    }

    private fun checkStatus() {
        when (scanner.status) {
            XYSmartScanStatus.Enabled -> {
                onBluetoothEnabled()
            }
            XYSmartScanStatus.BluetoothDisabled -> {
                onBluetoothDisabled()
            }
            XYSmartScanStatus.BluetoothUnavailable -> {
                onBluetoothUnavailable()
            }
            XYSmartScanStatus.LocationDisabled -> {
            }
            XYSmartScanStatus.None -> {
            }
        }
    }

    private fun disconnectListeners() {
        XY4BluetoothDevice.globalReporter.removeListener(tag!!)
    }

    private fun checkPermissions() {
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
    }

    override fun onResume() {
        log.info("onResume")
        super.onResume()

        checkPermissions()

        activity?.runOnUiThread { adapter.notifyDataSetChanged() }

        checkStatus()
    }

    override fun onPause() {
        super.onPause()
        disconnectListeners()
    }

    private fun onBluetoothEnabled() {
        binding.llDisabled.visibility = GONE
    }

    private fun onBluetoothDisabled() {
        binding.llDisabled.visibility = VISIBLE
    }

    private fun onBluetoothUnavailable() {
        binding.llDeviceNoBluetooth.visibility = VISIBLE
    }
}
