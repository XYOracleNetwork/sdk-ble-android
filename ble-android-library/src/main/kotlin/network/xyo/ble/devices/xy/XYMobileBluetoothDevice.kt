package network.xyo.ble.devices.xy

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import java.util.UUID
import network.xyo.ble.generic.scanner.XYScanResult
import network.xyo.ble.generic.scanner.XYScanResultManual

// this is a "fake" device that represents the device that is doing the scanning
open class XYMobileBluetoothDevice(context: Context, scanResult: XYScanResult, hash: String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    init {
        // we use this since the user would prefer for it to service a reinstall.  DO NOT USE for advertising!
        val uniqueId = Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID).hashCode().toUInt()
        majorValue = uniqueId.and(0xffff0000.toUInt()).shr(16).toUShort()
        minorValue = uniqueId.and(0xffff.toUInt()).toUShort()
        name = "Mobile Device"
        address = device?.address ?: (context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.address ?: "00:00:00:00:00:00"
    }

    override val prefix = "xy:mobile"

    override val uuid: UUID
        get() = FAMILY_UUID

    companion object {

        val FAMILY_UUID = UUID.fromString("735344c9-e820-42ec-9da7-f43a2b6802b9")!!

        fun create(context: Context, device: BluetoothDevice? = null): XYMobileBluetoothDevice {
            val fakeScanResult = XYScanResultManual(device, -20, null, SystemClock.elapsedRealtimeNanos())
            return XYMobileBluetoothDevice(context, fakeScanResult, FAMILY_UUID.toString())
        }
    }
}
