package network.xyo.ble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.util.SparseArray

import java.util.Arrays
import java.util.Objects

import org.jetbrains.annotations.Nullable

// This is a copy of BluetoothLeUtils from Android Marshmallow+ to be used with Android versions earlier than Marshmallow

object BluetoothLeUtils {

    /**
     * Returns a string composed from a [SparseArray].
     */
    internal fun toString(@Nullable array: SparseArray<ByteArray>?): String {
        if (array == null) {
            return "null"
        }
        if (array.size() == 0) {
            return "{}"
        }
        val buffer = StringBuilder()
        buffer.append('{')
        for (i in 0 until array.size()) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)))
        }
        buffer.append('}')
        return buffer.toString()
    }

    /**
     * Returns a string composed from a [Map].
     */
    internal fun <T> toString(@Nullable map: Map<T, ByteArray>?): String {
        if (map == null) {
            return "null"
        }
        if (map.isEmpty()) {
            return "{}"
        }
        val buffer = StringBuilder()
        buffer.append('{')
        val it = map.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val key = entry.key
            buffer.append(key).append("=").append(Arrays.toString(map[key]))
            if (it.hasNext()) {
                buffer.append(", ")
            }
        }
        buffer.append('}')
        return buffer.toString()
    }

    /**
     * Check whether two [SparseArray] equal.
     */
    internal fun equals(@Nullable array: SparseArray<ByteArray>?, @Nullable otherArray: SparseArray<ByteArray>?): Boolean {
        if (array === otherArray) {
            return true
        }
        if (array == null || otherArray == null) {
            return false
        }
        if (array.size() != otherArray.size()) {
            return false
        }

        // Keys are guaranteed in ascending order when indices are in ascending order.
        for (i in 0 until array.size()) {
            if (array.keyAt(i) != otherArray.keyAt(i) || !Arrays.equals(array.valueAt(i), otherArray.valueAt(i))) {
                return false
            }
        }
        return true
    }

    /**
     * Check whether two [Map] equal.
     */
    internal fun <T> equals(@Nullable map: Map<T, ByteArray>?, @Nullable otherMap: Map<T, ByteArray>?): Boolean {
        if (map === otherMap) {
            return true
        }
        if (map == null || otherMap == null) {
            return false
        }
        if (map.size != otherMap.size) {
            return false
        }
        val keys = map.keys
        if (keys != otherMap.keys) {
            return false
        }
        for (key in keys) {
            if (!Objects.deepEquals(map[key], otherMap[key])) {
                return false
            }
        }
        return true
    }

    /**
     * Ensure Bluetooth is turned on.
     *
     * @throws IllegalStateException If `adapter` is null or Bluetooth state is not
     * [BluetoothAdapter.STATE_ON].
     */
    internal fun checkAdapterStateOn(@Nullable adapter: BluetoothAdapter?) {
        check(!(adapter == null || adapter.state != BluetoothAdapter.STATE_ON)) { "BT Adapter is not turned ON" }
    }

}
