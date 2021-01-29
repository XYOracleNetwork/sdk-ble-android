package network.xyo.ble.sample.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.sample.views.XYDeviceItemView
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.generic.scanner.XYSmartScanListener
import java.util.concurrent.locks.ReentrantLock

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class XYDeviceAdapter(private val activity: Activity) : BaseAdapter() {
    private var devices: List<XYBluetoothDevice>
    private var lastSort = System.currentTimeMillis()

    private val scanner: XYSmartScan
        get() {
            return (activity.applicationContext as XYApplication).scanner
        }

    private val smartScannerListener = object : XYSmartScanListener() {
        override fun entered(device: XYBluetoothDevice) {
            refreshDevices()
        }

        override fun exited(device: XYBluetoothDevice) {
            refreshDevices()
        }

        override fun detected(device: XYBluetoothDevice) {
            //refreshDevices()
        }
    }

    private val sortLock = ReentrantLock()

    fun refreshDevices() = GlobalScope.launch {
        //we want to prevent multiple updates to go at the same time
        if (sortLock.tryLock()) {
            devices = XYBluetoothDevice.sortedList(scanner.devices.values.toList())
            activity.runOnUiThread {
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
            view = XYDeviceItemView(this.activity)
        }
        (view as XYDeviceItemView).setDevice(getItem(position) as XYBluetoothDevice)

        return view
    }

    companion object {

        private val TAG = XYDeviceAdapter::class.java.simpleName
    }
}
