package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_advertiser.view.*
import kotlinx.android.synthetic.main.fragment_services.view.*
import network.xyo.ble.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.gatt.server.XYBluetoothService
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui
import java.util.*

class ServicesFragment(services : Array<BluetoothGattService>?) : XYBaseFragment() {
    private val serviceList = XYServiceListAdapter(services ?: arrayOf())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.service_list

        val manager = LinearLayoutManager(activity?.applicationContext, LinearLayout.VERTICAL, false)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = serviceList

        serviceList.addListener(this.toString(), object : XYServiceListAdapter.Companion.XYServiceListAdapterListener {
            override fun onClick(service: BluetoothGattService) {
                val transition = fragmentManager?.beginTransaction()
                val serviceFragment = ServiceFragment(service)
                transition?.replace(R.layout.fragment_services, serviceFragment)
                transition?.addToBackStack(null)
                transition?.commit()
            }
        })
    }

    fun addService(service : XYBluetoothService) {
        ui {
            serviceList.addItem(service)
        }
    }

    companion object {
        fun newInstance (services : Array<BluetoothGattService>?) : ServicesFragment {
            return ServicesFragment(services)
        }
    }
}