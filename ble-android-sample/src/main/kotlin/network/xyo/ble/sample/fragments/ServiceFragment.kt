package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_SECONDARY
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYCharacteristicAdapter
import network.xyo.ble.sample.databinding.FragmentServiceBinding

class ServiceFragment(private var service: BluetoothGattService) : XYAppBaseFragment<FragmentServiceBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentServiceBinding {
        return FragmentServiceBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.serviceUuidTitle.text = service.uuid.toString()
        binding.serviceType.text = getServiceType()

        val recyclerView = binding.characteristicList
        val characteristicList = XYCharacteristicAdapter(service.characteristics?.toTypedArray()
                ?: arrayOf())
        val manager = LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
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

    private fun getServiceType(): String {
        when (service.type) {
            SERVICE_TYPE_PRIMARY -> return "SERVICE_TYPE_PRIMARY"
            SERVICE_TYPE_SECONDARY -> return "SERVICE_TYPE_SECONDARY"
        }
        return "UNKNOWN"
    }
}
