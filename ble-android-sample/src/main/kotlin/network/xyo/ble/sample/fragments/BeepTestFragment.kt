package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_basic.*
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.coroutines.*
import network.xyo.ble.devices.xy.XY3BluetoothDevice
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.base.XYBase
import java.lang.Exception

@kotlin.ExperimentalUnsignedTypes
class BeepTestFragment : XYDeviceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    private var startCount = 0
    private var connectCount = 0
    private var unlockCount = 0
    private var beepCount = 0

    fun updateUI() {
        activity?.runOnUiThread {
            start_count?.text = "Starts: $startCount"
            connect_count?.text = "Connects: $connectCount"
            unlock_count?.text = "Unlocks: $unlockCount"
            beep_count?.text = "Beeps: $beepCount"
        }
    }

    private suspend fun doBeepXY4(device: XY4BluetoothDevice) = GlobalScope.async {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().error == XYBluetoothResult.ErrorCode.None) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.primary.buzzer.set(0x11U).error == XYBluetoothResult.ErrorCode.None) {
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
                if (connectResult.error != XYBluetoothResult.ErrorCode.None) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }.await()

    private suspend fun doBeepXY3(device: XY3BluetoothDevice) = GlobalScope.async {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().error == XYBluetoothResult.ErrorCode.None) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.controlService.buzzerSelect.set(0x02U).error == XYBluetoothResult.ErrorCode.None) {
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
                if (connectResult.error != XYBluetoothResult.ErrorCode.None) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }.await()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_async.setOnClickListener {
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

        start_sync.setOnClickListener {
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

    companion object: XYBase() {
        fun newInstance() : BeepTestFragment {
            return BeepTestFragment()
        }
    }
}
