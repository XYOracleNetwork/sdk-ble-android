package network.xyo.ble.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable
import org.jetbrains.annotations.NotNull

import java.util.Objects

import org.jetbrains.annotations.Nullable

// This is a copy of ScanResult from Android Marshmallow+ to be used with Android versions earlier than Marshmallow

class ScanResultLegacy : Parcelable {
    // Remote bluetooth device.
    /**
     * Returns the remote bluetooth device identified by the bluetooth device address.
     */
    var device: BluetoothDevice? = null
        private set
    // Scan record, including advertising data and scan response data.
    /**
     * Returns the scan record, which is a combination of advertisement and scan response.
     */
    @Nullable
    @get:Nullable
    var scanRecord: ScanRecordLegacy? = null
        private set
    // Received signal strength.
    /**
     * Returns the received signal strength in dBm. The valid range is [-127, 127].
     */
    var rssi: Int = 0
        private set
    // Device timestamp when the result was last seen.
    /**
     * Returns timestamp since boot when the scan record was observed.
     */
    var timestampNanos: Long = 0
        private set

    /**
     * Constructor of scan result.
     *
     * @param device         Remote bluetooth device that is found.
     * @param scanRecord     Scan record including both advertising data and scan response data.
     * @param rssi           Received signal strength.
     * @param timestampNanos Device timestamp when the scan result was observed.
     */
    constructor(device: BluetoothDevice, @Nullable scanRecord: ScanRecordLegacy, rssi: Int,
                timestampNanos: Long) {
        this.device = device
        this.scanRecord = scanRecord
        this.rssi = rssi
        this.timestampNanos = timestampNanos
    }

    private constructor(@NotNull `in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun writeToParcel(@NotNull dest: Parcel, flags: Int) {
        if (device != null) {
            dest.writeInt(1)
            device?.writeToParcel(dest, flags)
        } else {
            dest.writeInt(0)
        }
        if (scanRecord != null) {
            dest.writeInt(1)
            dest.writeByteArray(scanRecord!!.bytes)
        } else {
            dest.writeInt(0)
        }
        dest.writeInt(rssi)
        dest.writeLong(timestampNanos)
    }

    private fun readFromParcel(@NotNull parcel: Parcel) {
        if (parcel.readInt() == 1) {
            device = BluetoothDevice.CREATOR.createFromParcel(parcel)
        }
        if (parcel.readInt() == 1) {
            scanRecord = ScanRecordLegacy.parseFromBytes(parcel.createByteArray())
        }
        rssi = parcel.readInt()
        timestampNanos = parcel.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return Objects.hash(device, rssi, scanRecord, timestampNanos)
    }

    override fun equals(other: Any?): Boolean {

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        if (other is ScanResultLegacy) {
            return (device == other.device && rssi == other.rssi &&
                    scanRecord == other.scanRecord
                    && timestampNanos == other.timestampNanos)
        }
        return false
    }

    @NotNull
    override fun toString(): String {
        return ("ScanResultLegacy{" + "mDevice=" + device + ", mScanRecord="
                + Objects.toString(scanRecord) + ", mRssi=" + rssi + ", mTimestampNanos="
                + timestampNanos + '}'.toString())
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ScanResultLegacy> = object : Parcelable.Creator<ScanResultLegacy> {
            @NotNull
            override fun createFromParcel(@NotNull source: Parcel): ScanResultLegacy {
                return ScanResultLegacy(source)
            }

            @NotNull
            override fun newArray(size: Int): Array<ScanResultLegacy?> {
                return arrayOfNulls(size)
            }
        }
    }

}
