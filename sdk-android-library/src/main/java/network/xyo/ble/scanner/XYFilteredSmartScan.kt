package network.xyo.ble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYMobileBluetoothDevice
import network.xyo.ble.gatt.XYBluetoothBase
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class XYFilteredSmartScan(context: Context) : XYBluetoothBase(context) {

    var startTime = 0L
    var scanResultCount = 0

    enum class Status {
        Enabled,
        BluetoothDisabled,
        BluetoothUnavailable,
        LocationDisabled
    }

    val resultsPerSecond: Float
        get() {
            if (startTime == 0L) {
                return 0F
            }
            return scanResultCount / (uptimeSeconds)
        }

    val uptime: Long
        get() {
            return if (startTime == 0L) {
                0
            } else {
                now - startTime
            }
        }

    val uptimeSeconds: Float
        get() {
            return uptime / 1000F
        }

    val hostDevice = XYMobileBluetoothDevice.create(context)

    val devices = ConcurrentHashMap<Int, XYBluetoothDevice>()

    init {
        devices[hostDevice.hashCode()] = hostDevice

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        val bluetoothAdapterReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getAction()
                logInfo("onReceive: ${action}")
                when (action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                            if (restartingBluetooth)
                            {
                                restartingBluetooth = false
                                BluetoothAdapter.getDefaultAdapter().enable()
                            }
                        }
                    }
                }
            }
        }

        context.applicationContext.registerReceiver(bluetoothAdapterReceiver, filter)
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

    fun getDevicesFromScanResult(scanResult: XYScanResult, globalDevices: ConcurrentHashMap<Int, XYBluetoothDevice>, foundDevices: HashMap<Int, XYBluetoothDevice>) {
        //only add them if they do not already exist
        XYBluetoothDevice.creator.getDevicesFromScanResult(context, scanResult, globalDevices, foundDevices)

        //add (or replace) all the found devices
        for ((_, foundDevice) in foundDevices) {
            globalDevices[foundDevice.hashCode()] = foundDevice
        }
    }

    fun areLocationServicesAvailable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }

    private val listeners = HashMap<String, Listener>()

    open class Listener : XYBluetoothDevice.Listener() {
        open fun statusChanged(status: BluetoothStatus) {

        }
    }

    enum class BluetoothStatus {
        None,
        Enabled,
        BluetoothUnavailable,
        BluetoothUnstable,
        BluetoothDisabled,
        LocationDisabled
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

    open suspend fun start(): Boolean {
        logInfo("start")
        startTime = now
        return true
    }

    open suspend fun stop(): Boolean {
        logInfo("stop")
        startTime = 0
        scanResultCount = 0
        return true
    }

    private var restartingBluetooth = false
    protected fun restartBluetooth() {
        logInfo(">>>>> restartBluetooth: Restarting Bluetooth Adapter <<<<<")
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
        devices.remove(device.hashCode())
        reportExited(device)
    }

    internal fun onScanResult(scanResults: List<XYScanResult>): List<XYScanResult> {
        scanResultCount += scanResults.size
        for (scanResult in scanResults) {
            val foundDevices = HashMap<Int, XYBluetoothDevice>()
            getDevicesFromScanResult(scanResult, this.devices, foundDevices)
            this.devices.putAll(foundDevices)
            if (foundDevices.size > 0) {
                for ((_, device) in foundDevices) {
                    this.devices[device.hashCode()] = device
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
                    reportDetected(device)
                    device.onDetect(scanResult)
                }
            }
        }
        return scanResults
    }

    private fun reportEntered(device: XYBluetoothDevice) {
        logInfo("reportEntered")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.entered(device)
                }
            }
        }
    }

    private fun reportExited(device: XYBluetoothDevice) {
        logInfo("reportExited")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.exited(device)
                }
            }
        }
    }

    private fun reportDetected(device: XYBluetoothDevice) {
        //logInfo("reportDetected")
        synchronized(listeners) {
            for ((_, listener) in listeners) {
                GlobalScope.launch {
                    listener.detected(device)
                }
            }
        }
    }

}