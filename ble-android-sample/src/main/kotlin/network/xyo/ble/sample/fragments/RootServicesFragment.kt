package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import network.xyo.ble.sample.R

class RootServicesFragment(private var startingServices : Array<BluetoothGattService>) : Fragment() {
    private var servicesFragment: ServicesFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services_root, container, false)
        val transaction = fragmentManager?.beginTransaction()
        servicesFragment = ServicesFragment(startingServices)
        transaction?.replace(R.id.root_frame_services, servicesFragment!!)
        transaction?.commit()
        return view
    }
}
