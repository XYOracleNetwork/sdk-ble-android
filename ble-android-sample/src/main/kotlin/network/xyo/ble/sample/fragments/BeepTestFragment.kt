package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import java.lang.Exception

class BeepTestFragment : XYBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    val scanner: XYSmartScan?
        get() {
            return (activity?.applicationContext as? XYApplication)?.scanner
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start.setOnClickListener {
            scanner?.devices?.forEach { (_, value) ->
                GlobalScope.launch {
                    when (value) {
                        is XY4BluetoothDevice -> {
                            log.info("BeepTest: ${value.id}: Trying to Beep")
                            try {
                                //val connectResult = value.connection {
                                    log.info("BeepTest: ${value.id}: Connected")
                                    if (value.unlock().await().error == null) {
                                        log.info("BeepTest: ${value.id}: Unlocked")
                                        if (value.primary.buzzer.set(11).await().error == null) {
                                            log.info("BeepTest: ${value.id}: Success")
                                        } else {
                                            log.error("BeepTest: ${value.id}: Failed")
                                        }
                                    } else {
                                        log.error("BeepTest: ${value.id}: Failed to Unlock")
                                    }
                                /*}.await()
                                if (connectResult.error != null) {
                                    log.error("BeepTest: ${value.id}: Failed to Connect: ${connectResult.error?.message}")
                                }*/
                            } catch (ex: Exception) {
                                log.error("BeepTest: ${ex.message}")
                            }
                        }
                        else -> {
                            log.info("BeepTest: Not a XY 4")
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