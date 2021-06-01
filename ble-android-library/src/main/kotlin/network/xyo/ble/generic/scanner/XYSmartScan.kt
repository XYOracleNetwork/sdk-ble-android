package network.xyo.ble.generic.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.xyo.ble.devices.xy.XYMobileBluetoothDevice
import network.xyo.ble.generic.XYBluetoothBase
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.gatt.peripheral.ble

enum class XYSmartScanListenerScanFailed {
    Unknown,
    AlreadyStarted,
    ApplicationRegistrationFailed,
    FeatureUnsupported,
    InternalError
}

open class XYSmartScanListener : XYBluetoothDeviceListener() {
    open fun statusChanged(status: XYSmartScanStatus) {
    }
}

enum class XYSmartScanStatus {
    None,
    Enabled,
    BluetoothDisabled,
    BluetoothUnavailable,
    LocationDisabled
}

@Suppress("unused")
abstract class XYSmartScan(context: Context) : XYBluetoothBase(context) {

    var startTime: Long? = null
    var scanResultCount = 0

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

    private var oldStatus = XYSmartScanStatus.None

    private val receiver = object : BroadcastReceiver() {
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
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        startStatusChecker()
    }

    private fun startStatusChecker() {
        ble.launch {
            while (true) {
                if (status != oldStatus) {
                    reportStatusChanged()
                }
                delay(1000)
            }
        }
    }

    val status: XYSmartScanStatus
        get() {
            val bluetoothManager = this.bluetoothManager
            if (bluetoothManager?.adapter == null) {
                return XYSmartScanStatus.BluetoothUnavailable
            }
            if (!(bluetoothManager.adapter.isEnabled)) {
                return XYSmartScanStatus.BluetoothDisabled
            }
            if (!areLocationServicesAvailable()) {
                return XYSmartScanStatus.LocationDisabled
            }
            return XYSmartScanStatus.Enabled
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

    open fun getDevicesFromScanResult(scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {
        // only add them if they do not already exist
        XYBluetoothDevice.creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)

        // add (or replace) all the found devices
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
            ble.launch {
                synchronized(listeners) {
                    for (listener in listeners) {
                        listener.value.statusChanged(status)
                    }
                }
            }
        }
    }

    private val listeners = HashMap<String, XYSmartScanListener>()

    fun codeToScanFailed(code: Int): XYSmartScanListenerScanFailed {
        return when (code) {
            ScanCallback.SCAN_FAILED_ALREADY_STARTED -> XYSmartScanListenerScanFailed.AlreadyStarted
            ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> XYSmartScanListenerScanFailed.ApplicationRegistrationFailed
            ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> XYSmartScanListenerScanFailed.FeatureUnsupported
            ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> XYSmartScanListenerScanFailed.InternalError
            else -> XYSmartScanListenerScanFailed.Unknown
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

    open suspend fun start() = ble.async {
        log.info("start")
        startTime = now
        return@async true
    }.await()

    open fun started(): Boolean {
        return startTime != null
    }

    open suspend fun stop() = ble.async {
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

    fun addListener(key: String, listener: XYSmartScanListener) {
        ble.launch {
            synchronized(listeners) {
                listeners[key] = listener
            }
        }
    }

    fun removeListener(key: String) {
        ble.launch {
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

    //we send the scanner active times to the devices so they know if there
    //has been dead time that they should ignore for exit detection
    internal fun updateLastScannerActivityTimes() {
        val time = now
        this.devices.values.forEach { device ->
            device.lastScannerActivityTime = time
        }
    }

    internal fun onScanResult(scanResults: List<XYScanResult>): List<XYScanResult> {
        updateLastScannerActivityTimes()
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
        // log.info("reportEntered")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                ble.launch {
                    listener.entered(device)
                }
            }
        }
    }

    private fun reportExited(device: XYBluetoothDevice) {
        // log.info("reportExited")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                ble.launch {
                    listener.exited(device)
                }
            }
        }
    }

    private fun reportDetected(device: XYBluetoothDevice) {
        // log.info("reportDetected")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                ble.launch {
                    listener.detected(device)
                }
            }
        }
    }
}
