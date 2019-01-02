package network.xyo.ble.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import network.xyo.core.XYBase

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
        log.info(TAG, "actionAclConnected $intent")
    }

    private fun actionAclDisconnected (intent: Intent?) {
        log.info(TAG, "actionAclDisconnected $intent")
    }

    private fun actionAclDisconnectRequested(intent: Intent?) {
        log.info(TAG, "actionAclDisconnectRequested $intent")
    }

    private fun actionBondStateChanged(intent: Intent?) {
        log.info(TAG, "actionBondStateChanged $intent")
    }

    private fun actionClassChanged (intent: Intent?) {
        log.info(TAG, "actionClassChanged $intent")
    }

    private fun actionFound (intent: Intent?) {
        log.info(TAG, "actionFound $intent")
    }

    private fun actionNameChanged (intent: Intent?) {
        log.info(TAG, "actionNameChanged $intent")
    }

    private fun actionPairingRequest (intent: Intent?) {
        log.info(TAG, "actionPairingRequest $intent")
    }

    private fun actionUuid(intent: Intent?) {
        log.info(TAG, "actionUuid $intent")
    }

    private fun extraBondState (intent: Intent?) {
        log.info(TAG, "extraBondState $intent")
    }

    private fun extraClass (intent: Intent?) {
        log.info(TAG, "extraClass $intent")
    }

    private fun extraDevice (intent: Intent?) {
        log.info(TAG, "extraDevice $intent")
    }

    private fun extraName (intent: Intent?) {
        log.info(TAG, "extraName $intent")
    }

    private fun extraRssi (intent: Intent?) {
        log.info(TAG, "extraRssi $intent")
    }

    private fun extraPairingKey (intent: Intent?) {
        log.info(TAG, "extraPairingKey $intent")
    }

    private fun extraPairingVagrant (intent: Intent?) {
        log.info(TAG, "extraPairingVagrant $intent")
    }


    companion object : XYBase() {
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