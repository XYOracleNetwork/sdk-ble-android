package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_services.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDeviceListener
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.adapters.XYServiceListAdapter

@kotlin.ExperimentalUnsignedTypes
class DeviceServicesFragment : XYDeviceFragment() {
    private val serviceList = XYServiceListAdapter(arrayOf())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.service_list

        val manager = LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = serviceList

        serviceList.addListener(this.toString(), object : XYServiceListAdapter.Companion.XYServiceListAdapterListener {
            override fun onClick(service: BluetoothGattService) {
                val transition = fragmentManager?.beginTransaction()
                val serviceFragment = ServiceFragment.newInstance(service)
                transition?.add(R.id.root_frame_services, serviceFragment)
                transition?.commit()
            }
        })
    }

    init {
        println("newServices: ${serviceList.itemCount}")
    }

    private suspend fun updateList() {
        val result = device?.connection {
            device?.services()?.let {
                serviceList.clear()
                for (item in it.iterator()) {
                    serviceList.addItem(item)
                }
            }
            return@connection XYBluetoothResult(true)
        }
        activity?.runOnUiThread {
            result?.let {
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
        device?.removeListener("services")
    }

    override fun onResume() {
        super.onResume()
        device?.addListener("services", object: XYBluetoothDeviceListener() {
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

    companion object {
        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : DeviceServicesFragment {
            val frag = DeviceServicesFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }
}
