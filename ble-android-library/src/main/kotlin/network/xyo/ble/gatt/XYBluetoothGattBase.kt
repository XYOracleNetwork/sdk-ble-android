package network.xyo.ble.gatt

import android.annotation.TargetApi
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import kotlinx.coroutines.*
import network.xyo.ble.CallByVersion
import network.xyo.ble.scanner.XYScanResult
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

//XYBluetoothGatt is a pure wrapper that does not add any functionality
//other than the ability to call the BluetoothGatt functions using coroutines

open class XYBluetoothGattBase protected constructor(
        context: Context
) : XYBluetoothBase(context) {

    enum class ConnectionState(val state: Int) {
        Unknown(-1),
        Disconnected(BluetoothGatt.STATE_DISCONNECTED),
        Connected(BluetoothGatt.STATE_CONNECTED),
        Connecting(BluetoothGatt.STATE_CONNECTING),
        Disconnecting(BluetoothGatt.STATE_DISCONNECTING)
    }

    var _connectionState: Int? = null
    val connectionState: ConnectionState
        get() {
            return when (_connectionState) {
                BluetoothGatt.STATE_DISCONNECTED -> ConnectionState.Disconnected
                BluetoothGatt.STATE_CONNECTING -> ConnectionState.Connecting
                BluetoothGatt.STATE_CONNECTED -> ConnectionState.Connected
                BluetoothGatt.STATE_DISCONNECTING -> ConnectionState.Disconnecting
                else -> ConnectionState.Unknown
            }
        }
}