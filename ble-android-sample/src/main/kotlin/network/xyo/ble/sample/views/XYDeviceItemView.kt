package network.xyo.ble.sample.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.device_item.view.*
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYODeviceActivity
import network.xyo.base.XYBase
import network.xyo.ble.devices.xy.XYFinderBluetoothDeviceListener

/**
 * Created by arietrouw on 12/27/17.
 */

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class XYDeviceItemView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var device: XYBluetoothDevice? = null

    init {
        setOnClickListener {
            val device = device
            if (device != null) {
                openDevice(device)
            }
        }
    }

    private fun openDevice(device: XYBluetoothDevice) {
        val intent = Intent(context, XYODeviceActivity::class.java)
        intent.putExtra(XYODeviceActivity.EXTRA_DEVICEHASH, device.hash)
        context.startActivity(intent)
    }

    fun update() {
        post {
            text_family.text = device?.javaClass?.simpleName
            text_name.text = device?.name
            text_connected.text = (device?.connected == true).toString()
            text_address.text = device?.address
            text_rssi.text = device?.rssi.toString()

            val ibeacon = device as? XYIBeaconBluetoothDevice
            if (ibeacon != null) {
                text_major.text = String.format(context.getString(R.string.hex_placeholder), ibeacon.major.toInt().toString(16))
                text_minor.text = String.format(context.getString(R.string.hex_placeholder), ibeacon.minor.toInt().toString(16))
                text_uuid.text = ibeacon.uuid.toString()
                text_major.visibility = View.VISIBLE
                text_minor.visibility = View.VISIBLE
                majorLabel.visibility = View.VISIBLE
                minorLabel.visibility = View.VISIBLE
            } else {
                text_uuid.text = "N/A"
                text_major.visibility = View.GONE
                text_minor.visibility = View.GONE
                majorLabel.visibility = View.GONE
                minorLabel.visibility = View.GONE
            }

            text_pulses.text = device!!.detectCount.toString()
        }
    }

    private val deviceListener = object : XYFinderBluetoothDeviceListener() {
        override fun entered(device: XYBluetoothDevice) {

        }

        override fun exited(device: XYBluetoothDevice) {

        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info(TAG,"connectionStateChanged")
        }
    }

    fun setDevice(device: XYBluetoothDevice?) {
        if (device != null) {
            device.removeListener(TAG)
        } else {
            log.error("Setting NULL device")
        }

        this.device = device

        device?.addListener(TAG, deviceListener)
        update()
    }

    companion object : XYBase() {
        private val TAG = XYDeviceItemView::class.java.simpleName
    }
}
