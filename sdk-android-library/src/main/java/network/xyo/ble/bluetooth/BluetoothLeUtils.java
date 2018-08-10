package network.xyo.ble.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// This is a copy of BluetoothLeUtils from Android Marshmallow+ to be used with Android versions earlier than Marshmallow

public class BluetoothLeUtils {

    /**
     * Returns a string composed from a {@link SparseArray}.
     */
    static String toString(@Nullable SparseArray<byte[]> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < array.size(); ++i) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * Returns a string composed from a {@link Map}.
     */
    static <T> String toString(@Nullable Map<T, byte[]> map) {
        if (map == null) {
            return "null";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        Iterator<Map.Entry<T, byte[]>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, byte[]> entry = it.next();
            Object key = entry.getKey();
            buffer.append(key).append("=").append(Arrays.toString(map.get(key)));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * Check whether two {@link SparseArray} equal.
     */
    static boolean equals(@Nullable SparseArray<byte[]> array, @Nullable SparseArray<byte[]> otherArray) {
        if (array == otherArray) {
            return true;
        }
        if (array == null || otherArray == null) {
            return false;
        }
        if (array.size() != otherArray.size()) {
            return false;
        }

        // Keys are guaranteed in ascending order when indices are in ascending order.
        for (int i = 0; i < array.size(); ++i) {
            if (array.keyAt(i) != otherArray.keyAt(i) ||
                    !Arrays.equals(array.valueAt(i), otherArray.valueAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether two {@link Map} equal.
     */
    static <T> boolean equals(@Nullable Map<T, byte[]> map, @Nullable Map<T, byte[]> otherMap) {
        if (map == otherMap) {
            return true;
        }
        if (map == null || otherMap == null) {
            return false;
        }
        if (map.size() != otherMap.size()) {
            return false;
        }
        Set<T> keys = map.keySet();
        if (!keys.equals(otherMap.keySet())) {
            return false;
        }
        for (T key : keys) {
            if (!Objects.deepEquals(map.get(key), otherMap.get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ensure Bluetooth is turned on.
     *
     * @throws IllegalStateException If {@code adapter} is null or Bluetooth state is not
     *                               {@link BluetoothAdapter#STATE_ON}.
     */
    static void checkAdapterStateOn(@Nullable BluetoothAdapter adapter) {
        if (adapter == null || adapter.getState() != BluetoothAdapter.STATE_ON) {
            throw new IllegalStateException("BT Adapter is not turned ON");
        }
    }

}
