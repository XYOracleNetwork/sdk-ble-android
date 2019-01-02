package network.xyo.ble.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import network.xyo.core.XYBase
import network.xyo.core.XYBase.Companion.logInfo

class BluetoothIntentReceiver : BroadcastReceiver() {
    private val listeners = HashMap<String, BluetoothIntentReceiverListener>()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            null -> return
            BluetoothDevice.ACTION_ACL_CONNECTED -> actionAclConnected(intent)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> actionAclDisconnected(intent)
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> actionAclDisconnectRequested(intent)
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> actionBondStateChanged(intent)
            BluetoothDevice.ACTION_CLASS_CHANGED -> actionClassChanged(intent)
            BluetoothDevice.ACTION_FOUND -> actionFound(intent)
            BluetoothDevice.ACTION_NAME_CHANGED -> actionNameChanged(intent)
            BluetoothDevice.ACTION_PAIRING_REQUEST -> actionPairingRequest(intent)
            BluetoothDevice.ACTION_UUID -> actionUuid(intent)
            BluetoothDevice.EXTRA_BOND_STATE -> extraBondState(intent)
            BluetoothDevice.EXTRA_CLASS -> extraClass(intent)
            BluetoothDevice.EXTRA_DEVICE -> extraDevice(intent)
            BluetoothDevice.EXTRA_NAME -> extraName(intent)
            BluetoothDevice.EXTRA_PAIRING_KEY -> extraPairingKey(intent)
            BluetoothDevice.EXTRA_RSSI -> extraRssi(intent)
            BluetoothDevice.EXTRA_PAIRING_VARIANT -> extraPairingVagrant(intent)
        }
    }

    open class BluetoothIntentReceiverListener {
        open fun actionAclConnected(intent: Intent?) {

        }

        open fun actionAclDisconnected(intent: Intent?) {

        }

        open fun actionAclDisconnectRequested(intent: Intent?) {

        }

        open fun actionBondStateChanged(intent: Intent?) {

        }

        open fun actionClassChanged(intent: Intent?) {

        }

        open fun actionFound(intent: Intent?) {

        }

        open fun actionNameChanged(intent: Intent?) {

        }

        open fun actionPairingRequest(intent: Intent?) {

        }

        open fun actionUuid(intent: Intent?) {

        }

        open fun extraBondState(intent: Intent?) {

        }

        open fun extraClass(intent: Intent?) {

        }

        open fun extraDevice(intent: Intent?) {

        }

        open fun extraName(intent: Intent?) {

        }

        open fun extraRssi(intent: Intent?) {

        }

        open fun extraPairingKey(intent: Intent?) {

        }

        open fun extraPairingVagrant(intent: Intent?) {

        }
    }

    private fun actionAclConnected (intent: Intent?) {
        logInfo(TAG, "actionAclConnected")
    }

    private fun actionAclDisconnected (intent: Intent?) {
        XYBase.logInfo(TAG, "actionAclDisconnected")
    }

    private fun actionAclDisconnectRequested(intent: Intent?) {
        XYBase.logInfo(TAG, "actionAclDisconnectRequested")
    }

    private fun actionBondStateChanged(intent: Intent?) {
        XYBase.logInfo(TAG, "actionBondStateChanged")
    }

    private fun actionClassChanged (intent: Intent?) {
        XYBase.logInfo(TAG, "actionClassChanged")
    }

    private fun actionFound (intent: Intent?) {
        XYBase.logInfo(TAG, "actionFound")
    }

    private fun actionNameChanged (intent: Intent?) {
        XYBase.logInfo(TAG, "actionNameChanged")
    }

    private fun actionPairingRequest (intent: Intent?) {
        XYBase.logInfo(TAG, "actionPairingRequest")
    }

    private fun actionUuid(intent: Intent?) {
        XYBase.logInfo(TAG, "actionUuid")
    }

    private fun extraBondState (intent: Intent?) {
        XYBase.logInfo(TAG, "extraBondState")
    }

    private fun extraClass (intent: Intent?) {
        XYBase.logInfo(TAG, "extraClass")
    }

    private fun extraDevice (intent: Intent?) {
        XYBase.logInfo(TAG, "extraDevice")
    }

    private fun extraName (intent: Intent?) {
        XYBase.logInfo(TAG, "extraName")
    }

    private fun extraRssi (intent: Intent?) {
        XYBase.logInfo(TAG, "extraRssi")
    }

    private fun extraPairingKey (intent: Intent?) {
        XYBase.logInfo(TAG, "extraPairingKey")
    }

    private fun extraPairingVagrant (intent: Intent?) {
        XYBase.logInfo(TAG, "extraPairingVagrant")
    }


    companion object {
        val bluetoothDeviceIntentFilter = object : IntentFilter() {
            init {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_CLASS_CHANGED)
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothDevice.ACTION_NAME_CHANGED)
                addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
                addAction(BluetoothDevice.ACTION_UUID)
                addAction(BluetoothDevice.EXTRA_BOND_STATE)
                addAction(BluetoothDevice.EXTRA_CLASS)
                addAction(BluetoothDevice.EXTRA_DEVICE)
                addAction(BluetoothDevice.EXTRA_NAME)
                addAction(BluetoothDevice.EXTRA_PAIRING_KEY)
                addAction(BluetoothDevice.EXTRA_PAIRING_VARIANT)
                addAction(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE)
                addAction(BluetoothDevice.EXTRA_RSSI)
                addAction(BluetoothDevice.EXTRA_UUID)
            }
        }

        const val TAG = "BluetoothIntentReceiver"
    }
}