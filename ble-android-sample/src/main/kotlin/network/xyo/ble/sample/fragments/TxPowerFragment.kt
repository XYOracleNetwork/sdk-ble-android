package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XY2BluetoothDevice
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentTxPowerBinding


@kotlin.ExperimentalUnsignedTypes
class TxPowerFragment : XYDeviceFragment<FragmentTxPowerBinding>() {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTxPowerBinding {
        return FragmentTxPowerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTxRefresh.setOnClickListener {
            setTxValues()
        }
    }

    override fun onResume() {
        super.onResume()

        if (deviceData?.txPowerLevel.isNullOrEmpty()) {
            setTxValues()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        activity?.runOnUiThread {
            binding.textTxPower.text = deviceData?.txPowerLevel
        }
    }

    private fun setTxValues() {
        activity?.runOnUiThread {
            binding.textTxPower.text = ""

            when (device) {
                is XY4BluetoothDevice -> {
                    val xy4 = (device as? XY4BluetoothDevice)
                    xy4?.let {
                        getXY4Values(xy4)
                    }
                }
                is XY3BluetoothDevice -> {
                    val xy3 = (device as? XY3BluetoothDevice)
                    xy3?.let {
                        getXY3Values(xy3)
                    }
                }
                is XY2BluetoothDevice -> {
                    val xy2 = (device as? XY2BluetoothDevice)
                    xy2?.let {
                        getXY2Values(xy2)
                    }
                }
                else -> {
                    binding.textTxPower.text = getString(R.string.unknown_device)
                }
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.txPowerLevel = device.txPowerService.txPowerLevel.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.txPowerLevel = device.txPowerService.txPowerLevel.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY2Values(device: XY2BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false

                deviceData?.let {
                    it.txPowerLevel = device.txPowerService.txPowerLevel.get().format()
                }

                return@connection XYBluetoothResult(true)

            }

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                TxPowerFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : TxPowerFragment {
            val frag = TxPowerFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
