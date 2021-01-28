package network.xyo.ble.sample.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.devices.apple.XYIBeaconBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYODeviceActivity
import network.xyo.base.XYBase
import network.xyo.ble.devices.xy.XYFinderBluetoothDeviceListener
import network.xyo.ble.sample.databinding.DeviceItemBinding

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class XYDeviceItemView(context: Context) : LinearLayout(context) {

    private var device: XYBluetoothDevice? = null
    private var binding: DeviceItemBinding

    init {
        setOnClickListener {
            val device = device
            if (device != null) {
                openDevice(device)
            }
        }
        binding = DeviceItemBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private fun openDevice(device: XYBluetoothDevice) {
        val intent = Intent(context, XYODeviceActivity::class.java)
        intent.putExtra(XYODeviceActivity.EXTRA_DEVICE_HASH, device.hash)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun update() {
        post {
            binding.textFamily.text = device?.javaClass?.simpleName
            binding.textName.text = device?.name
            binding.textConnected.text = (device?.connected == true).toString()
            binding.textAddress.text = device?.address
            binding.textRssi.text = device?.rssi.toString()

            val ibeacon = device as? XYIBeaconBluetoothDevice
            if (ibeacon != null) {
                binding.textMajor.text = String.format(context.getString(R.string.hex_placeholder), ibeacon.major.toInt().toString(16))
                binding.textMinor.text = String.format(context.getString(R.string.hex_placeholder), ibeacon.minor.toInt().toString(16))
                binding.textUuid.text = ibeacon.uuid.toString()
                binding.textMajor.visibility = View.VISIBLE
                binding.textMinor.visibility = View.VISIBLE
                binding.majorLabel.visibility = View.VISIBLE
                binding.minorLabel.visibility = View.VISIBLE
            } else {
                binding.textUuid.text = "N/A"
                binding.textMajor.visibility = View.GONE
                binding.textMinor.visibility = View.GONE
                binding.majorLabel.visibility = View.GONE
                binding.minorLabel.visibility = View.GONE
            }

            binding.textPulses.text = device!!.detectCount.toString()
        }
    }

    private val deviceListener = object : XYFinderBluetoothDeviceListener() {
        override fun entered(device: XYBluetoothDevice) {

        }

        override fun exited(device: XYBluetoothDevice) {

        }

        override fun detected(device: XYBluetoothDevice) {
            GlobalScope.launch(Dispatchers.Main) {
                update()
            }
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info(TAG,"connectionStateChangedX-DeviceItemView")
        }
    }

    fun setDevice(device: XYBluetoothDevice?) {
        device?.reporter?.removeListener(TAG) ?: log.error("Setting NULL device")

        this.device = device

        device?.reporter?.addListener(TAG, deviceListener)
        update()
    }

    companion object : XYBase() {
        private val TAG = XYDeviceItemView::class.java.simpleName
    }
}
