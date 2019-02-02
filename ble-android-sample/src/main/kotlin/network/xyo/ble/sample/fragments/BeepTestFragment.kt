package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui
import java.lang.Exception

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_async.setOnClickListener {
            scanner?.devices?.forEach { (_, value) ->
                GlobalScope.launch {
                    when (value) {
                        is XY4BluetoothDevice -> {
                            if ((value.rssi ?: -100) > -75) {
                                log.info("BeepTest(Async): ${value.id}: Trying to Beep [${value.rssi}]")
                                startCount++
                                updateUI()
                                try {
                                    //val connectResult = value.connection {
                                    if (value.connect().await().error == null) {
                                        log.info("BeepTest: ${value.id}: Connected")
                                        connectCount++
                                        updateUI()
                                        if (value.unlock().await().error == null) {
                                            log.info("BeepTest: ${value.id}: Unlocked")
                                            unlockCount++
                                            updateUI()
                                            if (value.primary.buzzer.set(11).await().error == null) {
                                                log.info("BeepTest: ${value.id}: Success")
                                                beepCount++
                                                updateUI()
                                            } else {
                                                log.error("BeepTest: ${value.id}: Failed")
                                            }
                                        } else {
                                            log.error("BeepTest: ${value.id}: Failed to Unlock")
                                        }
                                    } else {
                                        log.error("BeepTest: ${value.id}: Failed to Connect")
                                    }
                                    /*}.await()
                                    if (connectResult.error != null) {
                                        log.error("BeepTest: ${value.id}: Failed to Connect: ${connectResult.error?.message}")
                                    }*/
                                } catch (ex: Exception) {
                                    log.error("BeepTest: ${ex.message}")
                                }
                            }
                        }
                        else -> {
                            log.info("BeepTest: Not a XY 4")
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
                            if ((value.rssi ?: -100) > -75) {
                                log.info("BeepTest(Sync): ${value.id}: Trying to Beep [${value.rssi}]")
                                startCount++
                                updateUI()
                                try {
                                    val connectResult = value.connection(90000) {
                                        log.info("BeepTest(Sync): ${value.id}: Connected")
                                        connectCount++
                                        updateUI()
                                        if (value.unlock().await().error == null) {
                                            log.info("BeepTest(Sync): ${value.id}: Unlocked")
                                            unlockCount++
                                            updateUI()
                                            if (value.primary.buzzer.set(11).await().error == null) {
                                                log.info("BeepTest(Sync): ${value.id}: Success")
                                                beepCount++
                                                updateUI()
                                            } else {
                                                log.error("BeepTest(Sync): ${value.id}: Failed")
                                            }
                                        } else {
                                            log.error("BeepTest(Sync): ${value.id}: Failed to Unlock")
                                        }
                                    }.await()
                                    if (connectResult.error != null) {
                                        log.error("BeepTest(Sync): ${value.id}: Failed to Connect: ${connectResult.error?.message}")
                                    }
                                } catch (ex: Exception) {
                                    log.error("BeepTest(Sync): ${ex.message}")
                                }
                            }
                        }
                        else -> {
                            log.info("BeepTest(Sync): Not a XY 4")
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