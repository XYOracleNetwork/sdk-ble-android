package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_services.view.*
import network.xyo.ble.gatt.server.XYBluetoothService
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

class ServiceFragment(service : BluetoothGattService) : XYBaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance (service : BluetoothGattService) : ServiceFragment {
            return ServiceFragment(service)
        }
    }
}