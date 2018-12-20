package network.xyo.ble.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import network.xyo.ble.devices.*
import network.xyo.ble.gatt.XYBluetoothGatt
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class InfoFragment : XYAppBaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_startTest.setOnClickListener(this)
        button_connect.setOnClickListener(this)
        button_disconnect.setOnClickListener(this)
        button_find.setOnClickListener(this)
        button_stay_awake.setOnClickListener(this)
        button_fall_asleep.setOnClickListener(this)
        button_lock.setOnClickListener(this)
        button_unlock.setOnClickListener(this)
        button_enable_notify.setOnClickListener(this)
        button_stayConnected.setOnCheckedChangeListener(this)

        when (activity?.device) {
            is XY4BluetoothDevice -> {
                button_enable_notify.visibility = VISIBLE
                button_disable_notify.visibility = VISIBLE
            }
            is XY3BluetoothDevice -> {
                button_enable_notify.visibility = VISIBLE
                button_disable_notify.visibility = VISIBLE
            }

            else -> {
                button_enable_notify.visibility = GONE
                button_disable_notify.visibility = GONE
            }

        }
    }

    override fun onResume() {
        super.onResume()
        logInfo("onResume: InfoFragment")
        updateAdList()
        updateUI()
    }

    override fun update() {
        updateUI()
    }

    private fun updateUI() {
        ui {
            logInfo("update")
            if (activity?.device != null) {

                text_family.text = activity?.device?.name
                text_rssi.text = activity?.device?.rssi.toString()

                val iBeaconDevice = activity?.device as XYIBeaconBluetoothDevice?
                if (iBeaconDevice != null) {
                    text_major.text = String.format(getString(R.string.hex_placeholder), iBeaconDevice.major.toInt().toString(16))
                    text_minor.text = String.format(getString(R.string.hex_placeholder), iBeaconDevice.minor.toInt().toString(16))
                }

                text_pulse_count.text = activity?.device?.detectCount.toString()
                text_enter_count.text = activity?.device?.enterCount.toString()
                text_exit_count.text = activity?.device?.exitCount.toString()
                text_avg_gap_size.text = activity?.device?.averageDetectGap.toString()
                text_last_gap_size.text = activity?.device?.lastDetectGap.toString()
                text_max_gap_size.text = activity?.device?.maxDetectTime.toString()
            }

            if (activity?.device?.connectionState == XYBluetoothGatt.ConnectionState.Connected) {
                button_connect?.visibility = GONE
                button_disconnect?.visibility = VISIBLE
            } else {
                button_connect?.visibility = VISIBLE
                button_disconnect?.visibility = GONE
            }

        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.button_stayConnected -> {
                val device: XYBluetoothDevice? = activity?.device
                GlobalScope.launch {
                    device?.setStayConnected(isChecked)?.await()
                    updateUI()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button_connect -> {
                connect()
            }
            R.id.button_disconnect -> {
                disconnect()
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
            R.id.button_enable_notify -> {
                enableButtonNotify(true)
            }
            R.id.button_disable_notify -> {
                enableButtonNotify(false)
            }
            R.id.button_startTest -> {
                testRefreshGatt()
            }
        }
    }

    private fun testRefreshGatt() {
        GlobalScope.launch {
            val device: XYBluetoothDevice? = activity?.device
            val connection = device?.connectGatt()?.await()
            val result = device?.refreshGatt()?.await()
            activity?.showToast(result.toString())

        }
        //val v = activity?.device.connect()
        // activity?.scanner?.refreshGatt()
    }

    private fun connect() {
        val device: XYBluetoothDevice? = activity?.device
        GlobalScope.launch {
            val result = device?.connect()?.await()
            val error = result?.error
            if (error?.message.isNullOrEmpty()) {
                activity?.showToast(error?.message.toString())
            }
            updateUI()
        }
    }

    private fun disconnect() {
        val device: XYBluetoothDevice? = activity?.device
        GlobalScope.launch {
            val result = device?.disconnect()?.await()
            val error = result?.error
            if (error?.message.isNullOrEmpty()) {
                activity?.showToast(error?.message.toString())
            }
            updateUI()
        }
    }

    private fun find() {
        logInfo("beepButton: got xyDevice")
        ui {
            button_find?.isEnabled = false
        }

        GlobalScope.launch {
            (activity?.device as? XYFinderBluetoothDevice)?.find()?.await()
            ui {
                this@InfoFragment.isVisible.let { button_find?.isEnabled = true }

            }
        }
    }

    private fun wake() {
        logInfo("stayAwakeButton: onClick")
        ui {
            button_stay_awake?.isEnabled = false
        }

        GlobalScope.launch {
            val stayAwake = (activity?.device as? XYFinderBluetoothDevice)?.stayAwake()?.await()
            if (stayAwake == null) {
                activity?.showToast("Stay Awake Failed to Complete Call")
            } else {
                activity?.showToast("Stay Awake Set")
            }
            ui {
                this@InfoFragment.isVisible.let { button_stay_awake?.isEnabled = true }
            }
        }
    }

    private fun sleep() {
        logInfo("fallAsleepButton: onClick")
        ui {
            button_fall_asleep?.isEnabled = false
        }

        GlobalScope.launch {
            val fallAsleep = (activity?.device as? XYFinderBluetoothDevice)?.fallAsleep()
            if (fallAsleep == null) {
                activity?.showToast("Fall Asleep Failed to Complete Call")
            } else {
                activity?.showToast("Fall Asleep Set")
            }
            ui {
                this@InfoFragment.isVisible.let { button_fall_asleep?.isEnabled = true }
            }
        }
    }

    private fun lock() {
        logInfo("lockButton: onClick")
        ui {
            button_lock?.isEnabled = false
        }

        GlobalScope.launch {
            val locked = (activity?.device as? XYFinderBluetoothDevice)?.lock()?.await()
            when {
                locked == null -> showToast("Device does not support Lock")
                locked.error == null -> {
                    activity?.showToast("Locked: ${locked.value}")
                    updateStayAwakeEnabledStates() //TODO
                }
                else -> activity?.showToast("Lock Error: ${locked.error}")
            }
            ui {
                this@InfoFragment.isVisible.let { button_lock?.isEnabled = true }
            }
        }
    }

    private fun unlock() {
        logInfo("unlockButton: onClick")
        ui {
            button_unlock?.isEnabled = false
        }

        GlobalScope.launch {
            val unlocked = (activity?.device as? XYFinderBluetoothDevice)?.unlock()?.await()
            when {
                unlocked == null -> showToast("Device does not support Unlock")
                unlocked.error == null -> {
                    activity?.showToast("Unlocked: ${unlocked.value}")
                    updateStayAwakeEnabledStates() //TODO
                }
                else -> activity?.showToast("Unlock Error: ${unlocked.error}")
            }
            ui {
                this@InfoFragment.isVisible.let { button_unlock?.isEnabled = true }
            }
        }
    }


    private fun updateStayAwakeEnabledStates(): Deferred<Unit> {
        return GlobalScope.async {
            logInfo("updateStayAwakeEnabledStates")
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val stayAwake = xy4.primary.stayAwake.get().await()
                logInfo("updateStayAwakeEnabledStates: ${stayAwake.value}")
                ui {
                    this@InfoFragment.isVisible.let {
                        if (stayAwake.value != 0) {
                            button_fall_asleep?.isEnabled = true
                            button_stay_awake?.isEnabled = false
                        } else {
                            button_fall_asleep?.isEnabled = false
                            button_stay_awake?.isEnabled = true
                        }
                    }
                }
            } else {
                logError("updateStayAwakeEnabledStates: Not an XY4!", false)
            }
            return@async
        }
    }

    private fun enableButtonNotify(enable: Boolean): Deferred<Unit> {
        return GlobalScope.async {
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val notify = xy4.primary.buttonState.enableNotify(enable).await()
                ui {
                    showToast(notify.toString())
                }

            } else {
                val xy3 = activity?.device as? XY3BluetoothDevice
                if (xy3 != null) {
                    val notify = xy3.controlService.button.enableNotify(enable).await()
                    ui {
                        showToast(notify.toString())
                    }
                }
            }
            return@async
        }
    }

    private fun updateAdList() {
        ui {
            var txt = ""
            for ((_, ad) in activity?.device!!.ads) {
                txt = txt + ad.data?.toHex() + "\r\n"
            }
            adList?.text = txt
        }
    }

    //it is possible that reading the lock value is not implemented in the firmware
    private fun updateLockValue(): Deferred<Unit> {
        return GlobalScope.async {
            logInfo("updateLockValue")
            val xy4 = activity?.device as? XY4BluetoothDevice
            if (xy4 != null) {
                val lock = xy4.primary.lock.get().await()

                logInfo("updateLock: $lock.value")
                ui {
                    this@InfoFragment.isVisible.let {
                        if (lock.error != null) {
                            edit_lock_value.setText(getString(R.string.not_supported))
                        } else {
                            edit_lock_value.setText(lock.value?.toHex())
                        }
                    }
                }
            }
        }
    }

    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()
    private fun ByteArray.toHex(): String {
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }

    private fun testXy4() {
        GlobalScope.launch {
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
            }
        }
    }

    companion object {

        fun newInstance() =
                InfoFragment()
    }
}
