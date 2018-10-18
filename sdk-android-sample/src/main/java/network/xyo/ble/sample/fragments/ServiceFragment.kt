package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_service.view.*
import kotlinx.android.synthetic.main.fragment_services.view.*
import network.xyo.ble.gatt.server.XYBluetoothService
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYCharacteristicAdapter
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

class ServiceFragment(private val service : BluetoothGattService) : XYBaseFragment() {
    private val characteristicList = XYCharacteristicAdapter(service.characteristics.toTypedArray())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.service_uuid_title.text = service.uuid.toString()
        view.service_type.text = getServiceType()

        val recyclerView = view.characteristic_list

        val manager = LinearLayoutManager(activity?.applicationContext, LinearLayout.VERTICAL, false)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = characteristicList

        characteristicList.addListener(this.toString(), object : XYCharacteristicAdapter.Companion.XYCharacteristicAdapterListener {
            override fun onClick(service: BluetoothGattCharacteristic) {
                val transition = fragmentManager?.beginTransaction()
                val serviceFragment = CharacteristicFragment(service)
                transition?.replace(R.id.root_frame_services, serviceFragment)
                transition?.addToBackStack(null)
                transition?.commit()
            }
        })
    }

    private fun getServiceType () : String {
        when (service.type) {
            SERVICE_TYPE_PRIMARY -> return "SERVICE_TYPE_PRIMARY"
            SERVICE_TYPE_SECONDARY -> return "SERVICE_TYPE_SECONDARY"
        }
        return "UNKNOWN"
    }

    companion object {
        fun newInstance (service : BluetoothGattService) : ServiceFragment {
            return ServiceFragment(service)
        }
    }
}