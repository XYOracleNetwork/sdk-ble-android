package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_services.view.*
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

class ServicesFragment : XYBaseFragment() {
    var services : Array<BluetoothGattService>? = null
    private val serviceList = XYServiceListAdapter(services ?: arrayOf())

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
                transition?.replace(R.id.root_frame_services, serviceFragment)
                transition?.addToBackStack(null)
                transition?.commit()
            }
        })
    }

    fun addService(service : BluetoothGattService) {
        ui {
            serviceList.addItem(service)
        }
    }

    companion object {
        fun newInstance (services : Array<BluetoothGattService>?) : ServicesFragment {
            val frag = ServicesFragment()
            frag.services = services
            return frag
        }
    }
}