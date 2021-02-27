package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ble.sample.databinding.FragmentServicesBinding

@kotlin.ExperimentalUnsignedTypes
class DeviceServicesFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentServicesBinding>(device, deviceData) {
    private val serviceList = XYServiceListAdapter(arrayOf())

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentServicesBinding {
        return FragmentServicesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.serviceList

        val manager = LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = serviceList

        serviceList.addListener(this.toString(), object : XYServiceListAdapter.Companion.XYServiceListAdapterListener {
            override fun onClick(service: BluetoothGattService) {
                val transition = fragmentManager?.beginTransaction()
                val serviceFragment = ServiceFragment(service)
                transition?.add(R.id.root_frame_services, serviceFragment)
                transition?.commit()
            }
        })
        GlobalScope.launch {
            updateList()
        }
    }

    init {
        println("newServices: ${serviceList.itemCount}")
    }

    private suspend fun updateList() {
        val result = device.connection {
            device.services().let {
                serviceList.clear()
                for (item in it.iterator()) {
                    serviceList.addItem(item)
                }
            }
            return@connection XYBluetoothResult(true)
        }
        activity?.runOnUiThread {
            result.let {
                if (it.hasError()) {
                    log.error("Error: ${it.error}")
                } else {
                    log.info("Loaded Services")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        device.reporter.removeListener("services")
    }

    override fun onResume() {
        super.onResume()
        device.reporter.addListener("services", object: XYBluetoothDeviceListener() {
            override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
                super.connectionStateChanged(device, newState)
                GlobalScope.launch {
                    updateList()
                }
            }
        })
        GlobalScope.launch {
            updateList()
        }
    }
}
