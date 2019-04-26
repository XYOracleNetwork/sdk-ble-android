package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

class BeepTestFragment : XYBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    val scanner: XYSmartScan?
        get() {
            return (activity?.applicationContext as? XYApplication)?.scanner
        }

    var startCount = 0
    var connectCount = 0
    var unlockCount = 0
    var beepCount = 0

    fun updateUI() {
        ui {
            start_count?.text = "Starts: $startCount"
            connect_count?.text = "Connects: $connectCount"
            unlock_count?.text = "Unlocks: $unlockCount"
            beep_count?.text = "Beeps: $beepCount"
        }
    }

    private fun doBeepXY4(device: XY4BluetoothDevice) = GlobalScope.async {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().await().error == null) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.primary.buzzer.set(11).await().error == null) {
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
                }.await()
                if (connectResult.error != null) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error?.message}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }

    private fun doBeepXY3(device: XY3BluetoothDevice) = GlobalScope.async {
        if ((device.rssi ?: -100) > -60) {
            log.info("BeepTest(Async): ${device.id}: Trying to Beep [${device.rssi}]")
            startCount++
            updateUI()
            try {
                val connectResult = device.connection {
                    log.info("BeepTest: ${device.id}: Connected")
                    connectCount++
                    updateUI()
                    if (device.unlock().await().error == null) {
                        log.info("BeepTest: ${device.id}: Unlocked")
                        unlockCount++
                        updateUI()
                        if (device.controlService.buzzerSelect.set(2).await().error == null) {
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
                }.await()
                if (connectResult.error != null) {
                    log.error("BeepTest: ${device.id}: Failed to Connect: ${connectResult.error?.message}")
                }
            } catch (ex: Exception) {
                log.error("BeepTest: ${ex.message}")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_async.setOnClickListener {
            scanner?.devices?.forEach { (_, value) ->
                GlobalScope.launch {
                    when (value) {
                        is XY4BluetoothDevice -> {
                            doBeepXY4(value).await()
                        }
                        is XY3BluetoothDevice -> {
                            doBeepXY3(value).await()
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
                scanner?.devices?.forEach { (_, value) ->
                    when (value) {
                        is XY4BluetoothDevice -> {
                            doBeepXY4(value).await()
                        }
                        is XY3BluetoothDevice -> {
                            doBeepXY3(value).await()
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