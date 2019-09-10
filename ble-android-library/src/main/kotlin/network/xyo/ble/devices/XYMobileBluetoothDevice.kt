package network.xyo.ble.devices

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import network.xyo.ble.scanner.XYScanResult
import network.xyo.ble.scanner.XYScanResultManual
import java.util.*

//this is a "fake" device that represents the device that is doing the scanning
open class XYMobileBluetoothDevice(context: Context, scanResult: XYScanResult, hash:String) : XYFinderBluetoothDevice(context, scanResult, hash) {

    init {
        //we use this since the user would prefer for it to survice a resinstall.  DO NOT USE for advertising!
        val uniqueId = Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID).hashCode().toUInt()
        _major = uniqueId.and(0xffff0000.toUInt()).shr(16).toUShort()
        _minor = uniqueId.and(0xffff.toUInt()).toUShort()
        _name = "Mobile Device"

        var address = device?.address
        if (address == null) {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            address = bluetoothManager?.adapter?.address
        }
        _address = address ?: "00:00:00:00:00:00"
    }

    override val prefix = "xy:mobile"


    override val uuid: UUID
        get() = FAMILY_UUID

    companion object {

        val FAMILY_UUID = UUID.fromString("735344c9-e820-42ec-9da7-f43a2b6802b9")!!

        fun create(context: Context, device: BluetoothDevice? = null) : XYMobileBluetoothDevice {
            val fakeScanResult = XYScanResultManual(device, -20, null, SystemClock.elapsedRealtimeNanos())
            return XYMobileBluetoothDevice(context, fakeScanResult, FAMILY_UUID.toString())
        }
    }
}