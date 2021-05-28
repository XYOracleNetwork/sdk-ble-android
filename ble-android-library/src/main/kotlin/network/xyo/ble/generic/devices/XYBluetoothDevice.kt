package network.xyo.ble.generic.devices

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.ParcelUuid
import android.util.SparseArray
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.xyo.base.XYBase
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.ads.XYBleAd
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothGattClient
import network.xyo.ble.generic.gatt.peripheral.ble
import network.xyo.ble.generic.scanner.XYScanRecord
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.listeners.XYBluetoothDeviceListener
import network.xyo.ble.generic.reporters.XYBluetoothDeviceReporter

open class XYBluetoothDevice(context: Context, device: BluetoothDevice?, val hash: String, transport: Int? = null) : XYBluetoothGattClient(context, device, false, null, transport, null, null), Comparable<XYBluetoothDevice> {

    // hash - the reason for the hash system is that some devices rotate MAC addresses or polymorph in other ways
    // the user generally wants to treat a single physical device as a single logical device so the
    // hash that is passed in to create the class is used to make sure that the reuse of existing instances
    // is done based on device specific logic on "sameness"

    open val reporter = XYBluetoothDeviceReporter<XYBluetoothDevice, XYBluetoothDeviceListener>()
    open val ads = SparseArray<XYBleAd>()

    open var detectCount = 0
    open var enterCount = 0
    open var exitCount = 0
    open var averageDetectGap = 0L
    open var lastDetectGap = 0L
    open var maxDetectTime = 0L
    open var exitEnabled = false

    open var lastScannerActivityTime = now

    // set this to true if the device should report that it is out of
    // range right after disconnect.  Generally used for devices
    // with rotating MAC addresses
    open var exitAfterDisconnect = false

    private var addressValue: String? = null
    var address: String
        get() {
            return device?.address ?: addressValue ?: "00:00:00:00:00:00"
        }
        set(address) {
            addressValue = address
        }

    private var nameValue: String = ""
    var name: String?
        get() {
            return device?.name ?: nameValue
        }
        set(name) {
            nameValue = name ?: nameValue
        }

    open val connected: Boolean
        get() {
            return state == BluetoothGatt.STATE_CONNECTED
        }

    open val id: String
        get() {
            return ""
        }

    open var outOfRangeDelay = OUT_OF_RANGE_DELAY

    var connectable = false

    var notifyExit: ((device: XYBluetoothDevice) -> (Unit))? = null

    private var checkingForExit = false

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this.hashCode() == other?.hashCode()
    }

    override fun updateBluetoothDevice(device: BluetoothDevice?) {

        if (device?.address != this.device?.address || this.device == null) {
            // log.info("updateBluetoothDevice: Updating Device [$hash]")
            // log.info("updateBluetoothDevice: Updating Device [new = ${device?.address}, old = ${this.device?.address}]")
            // log.info("updateBluetoothDevice: Updating Device [new = 0x${device?.hashCode()?.absoluteValue?.toString(16)}, old = 0x${this.device?.hashCode()?.absoluteValue?.toString(16)}]")
            this.device = device
        }
    }

    // this should only be called from the onEnter function so that
    // there is one onExit for every onEnter
    private fun checkForExit() {
        if (!exitEnabled) {
            return
        }
        val lastActivityTimeGap = now - lastScannerActivityTime
        lastAccessTime = now
        if (checkingForExit) {
            return
        }
        checkingForExit = true
        ble.launch {
            while (checkingForExit && !cancelNotifications) {
                // log.info("checkForExit: $id : $rssi : $now : $outOfRangeDelay : $lastAdTime : $lastAccessTime")
                delay(outOfRangeDelay)

                // check if something else has already marked it as exited
                // this should only happen if another system (exit on connection drop for example)
                // marks this as out of range
                now.let { now ->
                    outOfRangeDelay.let { outOfRangeDelay ->
                        lastAccessTime.let { lastAccessTime ->
                            lastAdTime.let { lastAdTime ->
                                val adjustedNow = now - lastActivityTimeGap
                                adjustedNow.let { now ->
                                    if ((now - (lastAdTime
                                            ?: now) - lastActivityTimeGap) > outOfRangeDelay && (now - (lastAccessTime
                                            ?: now) - lastActivityTimeGap) > outOfRangeDelay
                                    ) {
                                        if (rssi != null) {
                                            rssi = null
                                            onExit()

                                            when(this) {
                                                is XY4BluetoothDevice -> {
                                                    val functionName = "checkForExit"

                                                    log.info(functionName, "Exiting: now: $now")
                                                    log.info(
                                                        functionName,
                                                        "Exiting: lastAdTime: $lastAdTime [$now - $lastAdTime]"
                                                    )
                                                    log.info(
                                                        functionName,
                                                        "Exiting: lastActivityTimeGap: $lastActivityTimeGap"
                                                    )
                                                    log.info(
                                                        functionName,
                                                        "Exiting: lastAccessTime: $lastAccessTime [$now - $lastAccessTime]"
                                                    )
                                                    log.info(
                                                        functionName,
                                                        "Exiting: outOfRangeDelay: $outOfRangeDelay"
                                                    )
                                                }
                                            }

                                            // make it thread safe
                                            val localNotifyExit = notifyExit
                                            if (localNotifyExit != null) {
                                                ble.launch {
                                                    localNotifyExit(this@XYBluetoothDevice)
                                                }
                                            }
                                            checkingForExit = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal open fun onEnter() {
        // log.info("onEnter: $address")
        enterCount++
        enterTime = now
        reporter.enter(this)
        checkForExit()
    }

    internal open fun onExit() {
        // log.info("onExit: $address")
        exitCount++
        enterTime = 0
        reporter.exit(this)
        if (!closed) {
            close()
        }
    }

    override fun onDetect(scanResult: XYScanResult?) {
        detectCount++
        lastAdTime = lastAdTime ?: enterTime ?: now
        lastDetectGap = now - (lastAdTime ?: now)
        if (lastDetectGap > maxDetectTime) {
            maxDetectTime = lastDetectGap
        }
        averageDetectGap = ((lastAdTime ?: now) - (enterTime ?: now)) / detectCount
        lastAdTime = now

        connectable = ((scanResult?.scanRecord?.advertiseFlags ?: 0) and 0x02) == 0x02

        reporter.detected(this)
    }

    override fun onConnectionStateChange(newState: Int) {
        reporter.connectionStateChanged(this, newState)
        // if a connection drop means we should mark it as out of range, then lets do it!
        if (exitAfterDisconnect) {
            ble.launch {
                rssi = null
                onExit()

                // make it thread safe
                val localNotifyExit = notifyExit
                if (localNotifyExit != null) {
                    ble.launch {
                        localNotifyExit(this@XYBluetoothDevice)
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated", ReplaceWith("reporter.addListener(key, listener)"))
    fun addListener(key: String, listener: XYBluetoothDeviceListener) {
        reporter.addListener(key, listener)
    }

    @Deprecated("Deprecated", ReplaceWith("reporter.removeListener(key)"))
    fun removeListener(key: String) {
        reporter.removeListener(key)
    }

    internal fun updateAds(record: XYScanRecord) {
        val buffer = ByteBuffer.wrap(record.bytes)
        while (buffer.hasRemaining()) {
            val ad = XYBleAd(buffer)
            ads.append(ad.hashCode(), ad)
        }
    }

    override fun compareTo(other: XYBluetoothDevice): Int {
        val d1 = rssi
        val d2 = other.rssi
        if (d1 == null) {
            if (d2 == null) {
                return 0
            }
            return -1
        }
        return when {
            d2 == null -> 1
            d1 == d2 -> 0
            d1 > d2 -> -1
            else -> 1
        }
    }

    companion object : XYBase() {

        // the period of time to wait for marking something as out of range
        // if we have not gotten any ads or been connected to it
        const val OUT_OF_RANGE_DELAY = 15000L

        fun enable(enable: Boolean, canCreate: Boolean? = null) {
            enabled = enable
            this.canCreate = canCreate ?: this.canCreate
        }

        internal var canCreate = false
        internal var enabled = false
        val manufacturerToCreator = SparseArray<XYCreator>()

        // Do not set serviceToCreator as Private. It's called by other apps
        private val serviceToCreator = HashMap<UUID, XYCreator>()

        // cancel the checkForExit routine so we don't get notifications after service is stopped
        var cancelNotifications: Boolean = false

        private fun getDevicesFromManufacturers(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, newDevices: HashMap<String, XYBluetoothDevice>) {
            for (i in 0 until manufacturerToCreator.size()) {
                val manufacturerId = manufacturerToCreator.keyAt(i)
                val bytes = scanResult.scanRecord?.getManufacturerSpecificData(manufacturerId)
                if (bytes != null) {
                    manufacturerToCreator.get(manufacturerId)?.getDevicesFromScanResult(context, scanResult, globalDevices, newDevices)
                }
            }
        }

        private fun getDevicesFromServices(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, newDevices: HashMap<String, XYBluetoothDevice>) {
            for ((uuid, creator) in serviceToCreator) {
                if (scanResult.scanRecord?.serviceUuids != null) {
                    if (scanResult.scanRecord?.serviceUuids?.contains(ParcelUuid(uuid)) == true) {
                        creator.getDevicesFromScanResult(context, scanResult, globalDevices, newDevices)
                    }
                }
            }
        }

        internal val creator = object : XYCreator() {
            override fun getDevicesFromScanResult(context: Context, scanResult: XYScanResult, globalDevices: ConcurrentHashMap<String, XYBluetoothDevice>, foundDevices: HashMap<String, XYBluetoothDevice>) {

                getDevicesFromServices(context, scanResult, globalDevices, foundDevices)
                getDevicesFromManufacturers(context, scanResult, globalDevices, foundDevices)

                if (foundDevices.size == 0) {
                    val hash = hashFromScanResult(scanResult)
                    val device = scanResult.device

                    val existingDevice = globalDevices[hash]

                    if (existingDevice != null) {
                        existingDevice.onDetect(scanResult)
                    } else {
                        if (canCreate && device != null) {
                            val createdDevice = XYBluetoothDevice(context, device, hash)
                            foundDevices[hash] = createdDevice
                            globalDevices[hash] = createdDevice
                        }
                    }
                } else {
                    foundDevices.forEach {
                        val existingDevice = globalDevices[it.value.hash]
                        if (existingDevice != null) {
                            existingDevice.onDetect(scanResult)
                        } else {
                            foundDevices[it.value.hash] = it.value
                            globalDevices[it.value.hash] = it.value
                        }
                    }
                }
            }
        }

        internal fun hashFromScanResult(scanResult: XYScanResult): String {
            return scanResult.address
        }

        private val compareDistance = kotlin.Comparator<XYBluetoothDevice> { o1, o2 ->
            if (o1 == null || o2 == null) {
                if (o1 != null && o2 == null) return@Comparator -1
                if (o2 != null && o1 == null) return@Comparator 1
                return@Comparator 0
            }
            o1.compareTo(o2)
        }

        fun sortedList(devices: List<XYBluetoothDevice>): List<XYBluetoothDevice> {
            return devices.sortedWith(compareDistance)
        }
    }
}
