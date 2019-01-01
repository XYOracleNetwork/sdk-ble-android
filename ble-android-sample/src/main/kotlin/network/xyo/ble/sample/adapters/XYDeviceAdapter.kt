package network.xyo.ble.sample.adapters

import android.app.Activity
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_xyo_ble_sample.*
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.sample.views.XYDeviceItemView
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.ui.ui
import java.util.*

class XYDeviceAdapter(private val activity: Activity) : BaseAdapter() {
    private var devices: List<XYFinderBluetoothDevice>
    private var lastSort = System.currentTimeMillis()

    private val scanner: XYSmartScan
        get() {
            return (activity.applicationContext as XYApplication).scanner
        }

    private val smartScannerListener = object : XYSmartScan.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            refreshDevices()
        }

        override fun exited(device: XYBluetoothDevice) {
            refreshDevices()
        }

        override fun detected(device: XYBluetoothDevice) {
            refreshDevices()
        }
    }

    fun refreshDevices() {
        if ((System.currentTimeMillis() - lastSort) > 5000) {
            devices = XYFinderBluetoothDevice.sortedList(scanner.devices)
            ui {
                activity.progress_spinner_scanner.visibility = GONE
                notifyDataSetChanged()
            }
            lastSort = System.currentTimeMillis()
        }
    }

    init {
        devices = ArrayList()
        scanner.addListener(TAG, smartScannerListener)
    }

    override fun getCount(): Int {
        return devices.size
    }

    override fun getItem(position: Int): Any {
        return devices[position]
    }

    override fun getItemId(position: Int): Long {
        return devices[position].address.hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = activity.layoutInflater.inflate(R.layout.device_item, parent, false)
        }
        (view as XYDeviceItemView).setDevice(getItem(position) as XYBluetoothDevice)

        return view
    }

    companion object {

        private val TAG = XYDeviceAdapter::class.java.simpleName
    }
}
