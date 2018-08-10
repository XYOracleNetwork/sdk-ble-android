package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_info.*
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.devices.XYIBeaconBluetoothDevice
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.sample.R
import network.xyo.ui.ui
import kotlinx.coroutines.experimental.*
import kotlin.experimental.and


class InfoFragment : XYAppBaseFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_startTest.setOnClickListener(this)
        button_find.setOnClickListener(this)
        button_stay_awake.setOnClickListener(this)
        button_fall_asleep.setOnClickListener(this)
        button_lock.setOnClickListener(this)
        button_unlock.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        logInfo("onResume: InfoFragment")
        updateAdList()
        update()
    }

    override fun update() {
        ui {
            logInfo("update")
            if (activity?.device != null) {
                text_family.text = activity?.device!!.name
                text_rssi.text = activity?.device!!.rssi.toString()

                val iBeaconDevice = activity?.device as XYIBeaconBluetoothDevice?
                if (iBeaconDevice != null) {
                    text_major.text = iBeaconDevice.major.toInt().toString()
                    text_minor.text = iBeaconDevice.minor.toInt().toString()
                }

                test_pulse_count.text = activity?.device!!.detectCount.toString()
                text_enter_count.text = activity?.device!!.enterCount.toString()
                text_exit_count.text = activity?.device!!.exitCount.toString()
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button_startTest -> {
                startTest()
            }
            R.id.button_find -> {
                find()
            }
            R.id.button_stay_awake -> {
                wake()
            }
            R.id.button_fall_asleep -> {
                sleep()
            }
            R.id.button_lock -> {
                lock()
            }
            R.id.button_unlock -> {
                unlock()
            }
        }
    }

    private fun startTest() {
        val xy4 = activity?.device as? XY4BluetoothDevice
        if (xy4 != null) {
            testXy4()
        } else {
            val xy3 = activity?.device as? XY3BluetoothDevice
            if (xy3 != null) {
                testXy3()
            } else {

            }
        }
    }

    private fun find() {
        logInfo("beepButton: got xyDevice")
        ui {
            button_find.isEnabled = false
        }

        launch(CommonPool) {
            (activity?.device as? XYFinderBluetoothDevice)?.find()?.await()
            ui {
                button_find.isEnabled = true
            }
        }
    }

    private fun wake() {
        logInfo("stayAwakeButton: onClick")
        ui {
            button_stay_awake.isEnabled = false
        }

        launch(CommonPool) {
            val stayAwake = (activity?.device as? XYFinderBluetoothDevice)?.stayAwake()?.await()
            if (stayAwake == null) {
                activity?.showToast("Stay Awake Failed to Complete Call")
            } else {
                activity?.showToast("Stay Awake Set")
            }
            ui {
                button_stay_awake.isEnabled = true
            }
        }
    }

    private fun sleep() {
        logInfo("fallAsleepButton: onClick")
        ui {
            button_fall_asleep.isEnabled = false
        }

        launch(CommonPool) {
            val fallAsleep = (activity?.device as? XYFinderBluetoothDevice)?.fallAsleep()
            if (fallAsleep == null) {
                activity?.showToast("Fall Asleep Failed to Complete Call")
            } else {
                activity?.showToast("Fall Asleep Set")
            }
            ui {
                button_fall_asleep.isEnabled = true
            }
        }
    }

    private fun lock() {
        logInfo("lockButton: onClick")
        ui {
            button_lock.isEnabled = false
        }

        launch(CommonPool) {
            val locked = (activity?.device as? XYFinderBluetoothDevice)?.lock()?.await()
            when {
                locked == null -> showToast("Device does not support Lock")
                locked.error == null -> {
                    activity?.showToast(locked.toString())
                    updateStayAwakeEnabledStates() //TODO
                }
                else -> activity?.showToast("Lock Error: ${locked.error}")
            }
            ui {
                button_lock.isEnabled = true
            }
        }
    }

    private fun unlock() {
        logInfo("unlockButton: onClick")
        ui {
            button_unlock.isEnabled = false
        }

        launch(CommonPool) {
            val unlocked = (activity?.device as? XYFinderBluetoothDevice)?.unlock()?.await()
            when {
                unlocked == null -> showToast("Device does not support Unlock")
                unlocked.error == null -> {
                    activity?.showToast("Unlocked: ${unlocked}")
                    updateStayAwakeEnabledStates() //TODO
                }
                else -> activity?.showToast("Unlock Error: ${unlocked.error}")
            }
            ui {
                button_unlock.isEnabled = true
            }
        }
    }


    private fun updateStayAwakeEnabledStates(): Deferred<Unit> {
        return async(CommonPool) {
            logInfo("updateStayAwakeEnabledStates")
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val stayAwake = xy4.primary.stayAwake.get().await()
                logInfo("updateStayAwakeEnabledStates: $stayAwake")
                ui {
                    if (stayAwake.value != 0) {
                        button_fall_asleep.isEnabled = true
                        button_stay_awake.isEnabled = false
                    } else {
                        button_fall_asleep.isEnabled = false
                        button_stay_awake.isEnabled = true
                    }
                }
            } else {
                logError("updateStayAwakeEnabledStates: Not an XY4!", false)
            }
            return@async
        }
    }

    fun enableButtonNotify(): Deferred<Unit> {
        return async(CommonPool) {
            logInfo("enableButtonNotify")
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val notify = xy4.primary.buttonState.enableNotify(true).await()
                showToast(notify.toString())
            } else {
                val xy3 = activity?.device as? XY3BluetoothDevice
                if (xy3 != null) {
                    val notify = xy3.controlService.button.enableNotify(true).await()
                    showToast(notify.toString())
                }
            }
            return@async
        }
    }

    private fun updateAdList() {
        ui {
            // val adList: XYTextView? = findViewById(R.id.adList)
            var txt = ""
            for ((_, ad) in activity?.device!!.ads) {
                txt = txt + ad.toString() + "\r\n"
            }
            adList?.text = txt
        }
    }

    //it is possible that reading the lock value is not implemented in the firmware
    private fun updateLockValue(): Deferred<Unit> {
        return async(CommonPool) {
            logInfo("updateLockValue")
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val lock = xy4.primary.lock.get().await()

                logInfo("updateLock: ${lock}")
                ui {
                    if (lock.error != null || lock.error != null) {
                        edit_lock_value.setText("----")
                    } else {
                        edit_lock_value.setText(bytesToHex(lock.value!!))
                    }
                }
            }
        }
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = (bytes[j] and 0xFF.toByte()).toInt()

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun testXy4() {
        //TODO - disable btn, show progress
        logInfo("textXy4")
        launch {
            val xy4 = activity?.device as? XY4BluetoothDevice
            xy4?.connection {
                for (i in 0..10000) {
                    val text = "Hello+$i"
                    val write = xy4.primary.lock.set(XY4BluetoothDevice.DefaultLockCode).await()
                    if (write.error == null) {
                        logInfo("testXy4: Success: $text")
                    } else {
                        logInfo("testXy4: Fail: $text : ${write.error}")
                    }
                }
                return@connection XYBluetoothResult(true)
            }
        }
    }

    private fun testXy3() {

    }

    private fun testXy2() {

    }

    private fun testXyGps() {

    }

    companion object {
        private const val TAG = "InfoFragment"

        fun newInstance() =
                InfoFragment()
    }
}
