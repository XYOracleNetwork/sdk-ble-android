package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.*
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResultErrorCode
import network.xyo.ble.sample.databinding.FragmentTestBinding
import java.lang.Exception

@kotlin.ExperimentalUnsignedTypes
class BeepTestFragment : XYAppBaseFragment<FragmentTestBinding>() {

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentTestBinding {
        return FragmentTestBinding.inflate(inflater, container, false)
    }

    private var startCount = 0
    private var connectCount = 0
    private var unlockCount = 0
    private var beepCount = 0

    fun updateUI() {
        activity?.runOnUiThread {
            binding.startCount.text = "Starts: $startCount"
            binding.connectCount.text = "Connects: $connectCount"
            binding.unlockCount.text = "Unlocks: $unlockCount"
            binding.beepCount.text = "Beeps: $beepCount"
        }
    }

    private suspend fun doBeepXY4(device: XY4BluetoothDevice) {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().error == XYBluetoothResultErrorCode.None) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.primary.buzzer.set(0x11U).error == XYBluetoothResultErrorCode.None) {
                            log.info("BeepTest: ${device.id}: Success")
                            beepCount++
                            updateUI()
                        } else {
                            log.error("BeepTest: ${device.id}: Failed")
                        }
                    } else {
                        log.error("BeepTest: ${device.id}: Failed to Unlock")
                    }
                    return@connection XYBluetoothResult(true)
                }
                if (connectResult.error != XYBluetoothResultErrorCode.None) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }

    private suspend fun doBeepXY3(device: XY3BluetoothDevice) {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().error == XYBluetoothResultErrorCode.None) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.controlService.buzzerSelect.set(0x02U).error == XYBluetoothResultErrorCode.None) {
                            log.info("BeepTest: ${device.id}: Success")
                            beepCount++
                            updateUI()
                        } else {
                            log.error("BeepTest: ${device.id}: Failed")
                        }
                    } else {
                        log.error("BeepTest: ${device.id}: Failed to Unlock")
                    }
                    return@connection XYBluetoothResult(true)
                }
                if (connectResult.error != XYBluetoothResultErrorCode.None) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startAsync.setOnClickListener {
            scanner.devices.forEach { (_, value) ->
                GlobalScope.launch {
                    when (value) {
                        is XY4BluetoothDevice -> {
                            doBeepXY4(value)
                        }
                        is XY3BluetoothDevice -> {
                            doBeepXY3(value)
                        }
                        else -> {
                            log.info("BeepTest: Not a XY 4/3")
                        }
                    }
                }
            }
        }

        binding.startAsync.setOnClickListener {
            GlobalScope.launch {
                scanner.devices.forEach { (_, value) ->
                    when (value) {
                        is XY4BluetoothDevice -> {
                            doBeepXY4(value)
                        }
                        is XY3BluetoothDevice -> {
                            doBeepXY3(value)
                        }
                        else -> {
                            log.info("BeepTest(Sync): Not a XY 4/3")
                        }
                    }
                }
            }
        }
    }
}
