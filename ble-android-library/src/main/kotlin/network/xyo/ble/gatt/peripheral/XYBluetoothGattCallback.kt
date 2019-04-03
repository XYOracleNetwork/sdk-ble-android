package network.xyo.ble.gatt.peripheral

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.core.XYBase
import java.util.HashMap

// This class is just a callback broadcast version of the standard BluetoothGattCallback

open class XYBluetoothGattCallback: BluetoothGattCallback() {
    private val gattListeners = HashMap<String, BluetoothGattCallback>()
    private val _lock = Any()

    fun addListener(key: String, listener: BluetoothGattCallback) {
        synchronized(_lock) {
            gattListeners[key] = listener
        }
    }

    fun removeListener(key: String) {
        synchronized(_lock) {
            gattListeners.remove(key)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        log.info("onCharacteristicChanged: $characteristic")

        // there is a possible race condition when getting the value from a characteristic that is
        // changing quickly, so there is a flag to change this. If blockNotificationCallback is set to true,
        // all of the notifications callbacks will be blocking, preventing the race condition
        // https://bugs.chromium.org/p/chromium/issues/detail?id=647673&desc=2
        synchronized(_lock) {
            for ((key, listener) in gattListeners) {
                log.info("onCharacteristicChanged: $key")
                if (blockNotificationCallback) {
                    listener.onCharacteristicChanged(gatt, characteristic)
                } else {
                    GlobalScope.launch {
                        listener.onCharacteristicChanged(gatt, characteristic)
                    }
                }
            }
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        log.info("onCharacteristicRead: $characteristic : $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onCharacteristicRead(gatt, characteristic, status)
                }
            }
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        log.info("onCharacteristicWrite: $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onCharacteristicWrite(gatt, characteristic, status)
                }
            }
        }
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        log.info("onConnectionStateChange: ${gatt?.device?.address} $newState : $status")
        synchronized(_lock) {
            for ((tag, listener) in gattListeners) {
                GlobalScope.launch {
                    log.info("onConnectionStateChange: $tag")
                    listener.onConnectionStateChange(gatt, status, newState)
                }
            }
        }
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorRead(gatt, descriptor, status)
        log.info("onDescriptorRead: $descriptor : $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onDescriptorRead(gatt, descriptor, status)
                }
            }
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        log.info("onDescriptorWrite: $descriptor : $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onDescriptorWrite(gatt, descriptor, status)
                }
            }
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        log.info("onMtuChanged: $mtu : $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onMtuChanged(gatt, mtu, status)
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        log.info("onPhyRead: $txPhy : $rxPhy : $status")

        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onPhyRead(gatt, txPhy, rxPhy, status)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        log.info("onPhyUpdate: $txPhy : $rxPhy : $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onPhyUpdate(gatt, txPhy, rxPhy, status)
                }
            }
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        log.info("onReadRemoteRssi: $rssi : $status")
        //AT:this@XYBluetoothGatt.rssi = rssi
        //AT:onDetect(null)
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onReadRemoteRssi(gatt, rssi, status)
                }
            }
        }
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
        log.info("onReliableWriteCompleted: $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onReliableWriteCompleted(gatt, status)
                }
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        log.info("onServicesDiscovered: $status")
        synchronized(_lock) {
            for ((_, listener) in gattListeners) {
                GlobalScope.launch {
                    listener.onServicesDiscovered(gatt, status)
                }
            }
        }
    }

    companion object: XYBase() {
        var blockNotificationCallback = false
    }
}