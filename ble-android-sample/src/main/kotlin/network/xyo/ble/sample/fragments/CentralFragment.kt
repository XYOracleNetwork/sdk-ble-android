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
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.*
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.activities.XYOTestActivity
import network.xyo.ble.generic.scanner.XYSmartScanStatus
import network.xyo.ble.sample.databinding.FragmentCentralBinding
import network.xyo.ble.generic.gatt.peripheral.ble

@kotlin.ExperimentalStdlibApi
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
            ble.launch {
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

    override fun onResume() {
        log.info("onResume")
        super.onResume()

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
