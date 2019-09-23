package network.xyo.ble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYMobileBluetoothDevice
import network.xyo.ble.XYBluetoothBase
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@kotlin.ExperimentalUnsignedTypes
abstract class XYSmartScan(context: Context) : XYBluetoothBase(context) {

    var startTime: Long? = null
    var scanResultCount = 0

    enum class Status {
        None,
        Enabled,
        BluetoothDisabled,
        BluetoothUnavailable,
        LocationDisabled
    }

    val resultsPerSecond: Float?
        get() {
            return uptime?.let { scanResultCount / (it / 1000f) }
        }

    val uptime: Long?
        get() {
            startTime?.let {
                return now - it
            }
            return null
        }

    val hostDevice = XYMobileBluetoothDevice.create(context)

    val devices = ConcurrentHashMap<String, XYBluetoothDevice>()

    protected var restartingBluetooth = false

    private var oldStatus = Status.None

    private val recevier = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED == intent?.action) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    log.info(">>>> Bluetooth Adapter Disabled <<<<")
                    if (restartingBluetooth && started()) {
                        BluetoothAdapter.getDefaultAdapter().enable()
                        restartingBluetooth = false
                    }
                } else {
                    log.info(">>>> Bluetooth Adapter Enabled <<<<")
                }
                reportStatusChanged()
            }
        }
    }

    init {
        devices[hostDevice.hash] = hostDevice
        context.registerReceiver(recevier, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        startStatusChecker()
    }

    private fun startStatusChecker() {
        GlobalScope.launch {
            while(true) {
                if (status != oldStatus) {
                    reportStatusChanged()
                }
                delay(1000)
            }
        }
    }

    val status: Status
        get() {
            val bluetoothManager = this.bluetoothManager
            if (bluetoothManager?.adapter == null) {
                return Status.BluetoothUnavailable
            }
            if (!(bluetoothManager.adapter.isEnabled)) {
                return Status.BluetoothDisabled
            }
            if (!areLocationServicesAvailable()) {
                return Status.LocationDisabled
            }
            return Status.Enabled
        }

    fun enableBluetooth(enable: Boolean) {
        if (enable) {
            bluetoothManager?.adapter?.enable()
        } else {
            bluetoothManager?.adapter?.disable()
        }
    }

    fun deviceFromId(id: String): XYBluetoothDevice? {
        for ((_, device) in devices) {
            if (device.id == id) {
                return device
            }
        }
        return null
    }

    fun getDevicesFromScanResult(scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {
        //only add them if they do not already exist
        XYBluetoothDevice.creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)

        //add (or replace) all the found devices
        for ((_, foundDevice) in foundDevices) {
            globalDevices[foundDevice.hash] = foundDevice
        }
    }

    private fun areLocationServicesAvailable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    private fun reportStatusChanged() {
        if (status != oldStatus) {
            oldStatus = status
            GlobalScope.launch {
                synchronized(listeners) {
                    for (listener in listeners) {
                        listener.value.statusChanged(status)
                    }
                }
            }
        }
    }

    private val listeners = HashMap<String, Listener>()

    open class Listener : XYBluetoothDevice.Listener() {
        open fun statusChanged(status: Status) {

        }
    }

    enum class ScanFailed {
        Unknown,
        AlreadyStarted,
        ApplicationRegistrationFailed,
        FeatureUnsupported,
        InternalError
    }

    fun codeToScanFailed(code: Int): ScanFailed {
        return when (code) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> ScanFailed.AlreadyStarted
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> ScanFailed.ApplicationRegistrationFailed
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> ScanFailed.FeatureUnsupported
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> ScanFailed.InternalError
            else -> ScanFailed.Unknown
        }
    }

    private var _background = false
    var background: Boolean
        get() {
            return _background
        }
        set(background) {
            _background = background
        }

    open suspend fun start() = GlobalScope.async {
        log.info("start")
        startTime = now
        return@async true
    }.await()

    open fun started(): Boolean {
        return startTime != null
    }

    open suspend fun stop() = GlobalScope.async {
        log.info("stop")
        startTime = null
        scanResultCount = 0
        return@async true
    }.await()

    protected fun restartBluetooth() {
        log.info(">>>>> restartBluetooth: Restarting Bluetooth Adapter <<<<<")

        restartingBluetooth = true
        BluetoothAdapter.getDefaultAdapter().disable()
    }

    fun addListener(key: String, listener: Listener) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners.put(key, listener)
            }
        }
    }

    fun removeListener(key: String) {
        GlobalScope.launch {
            synchronized(listeners) {
                listeners.remove(key)
            }
        }
    }

    private var handleDeviceNotifyExit = fun(device: XYBluetoothDevice) {
        device.rssi = null
        devices.remove(device.hash)
        reportExited(device)
    }

    internal fun onScanResult(scanResults: List<XYScanResult>): List<XYScanResult> {
        scanResultCount += scanResults.size
        for (scanResult in scanResults) {
            val foundDevices = HashMap<String, XYBluetoothDevice>()
            getDevicesFromScanResult(scanResult, this.devices, foundDevices)
            this.devices.putAll(foundDevices)
            if (foundDevices.size > 0) {
                for ((_, device) in foundDevices) {
                    this.devices[device.hash] = device
                    device.updateBluetoothDevice(scanResult.device)

                    val scanRecord = scanResult.scanRecord

                    if (scanRecord != null) {
                        device.updateAds(scanRecord)
                    }

                    if (device.rssi == null) {
                        reportEntered(device)
                        device.onEnter()
                        device.notifyExit = handleDeviceNotifyExit
                    }
                    device.rssi = scanResult.rssi
                    device.name = scanResult.scanRecord?.deviceName ?: device.name

                    reportDetected(device)
                    device.onDetect(scanResult)
                }
            }
        }
        return scanResults
    }

    private fun reportEntered(device: XYBluetoothDevice) {
        //log.info("reportEntered")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.entered(device)
                }
            }
        }
    }

    private fun reportExited(device: XYBluetoothDevice) {
        //log.info("reportExited")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.exited(device)
                }
            }
        }
    }

    private fun reportDetected(device: XYBluetoothDevice) {
        //log.info("reportDetected")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.detected(device)
                }
            }
        }
    }

}