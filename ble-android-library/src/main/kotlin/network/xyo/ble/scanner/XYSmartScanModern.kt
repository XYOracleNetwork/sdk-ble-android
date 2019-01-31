package network.xyo.ble.scanner

import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import network.xyo.ble.CallByVersion
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import java.util.*

@TargetApi(21)
class XYSmartScanModern(context: Context) : XYSmartScan(context) {
    override suspend fun start(): Boolean {
        log.info("start")
        super.start()

        val result = asyncBle {

            val bluetoothAdapter = bluetoothManager?.adapter

            bluetoothAdapter?.let {
                val scanner = it.bluetoothLeScanner
                if (scanner == null) {
                    log.info("startScan:Failed to get Bluetooth Scanner. Disabled?")
                    return@asyncBle XYBluetoothResult(false)
                } else {
                    val filters = ArrayList<ScanFilter>()
                    scanner.startScan(filters, getSettings(), callback)
                }

                return@asyncBle XYBluetoothResult(true)
            }

            log.info("Bluetooth Disabled")
            return@asyncBle XYBluetoothResult(false)
        }.await()

        if (result.error != null) {
            return false
        }
        return result.value!!
    }

    private val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            //log.info("onBatchScanResults: $results")
            results?.let {
                val xyResults = ArrayList<XYScanResult>()
                for (result in it) {
                    xyResults.add(XYScanResultModern(result))
                }
                onScanResult(xyResults)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            log.error("onScanFailed: $errorCode, ${codeToScanFailed(errorCode)}", false)
            if (ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED == errorCode) {
                restartBluetooth()
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            //log.info("onScanResult: $result")
            result?.let {
                val xyResults = ArrayList<XYScanResult>()
                xyResults.add(XYScanResultModern(it))
                onScanResult(xyResults)
            }
        }
    }

    private fun getSettings(): ScanSettings {
        var result: ScanSettings? = null
        CallByVersion()
                .add(Build.VERSION_CODES.M) {
                    result = getSettings23()
                }
                .add(Build.VERSION_CODES.LOLLIPOP) {
                    result = getSettings21()
                }.call()
        return result!!
    }

    private fun getSettings21(): ScanSettings {
        return ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_BALANCED)
                .build()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getSettings23(): ScanSettings {
        return ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(android.bluetooth.le.ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(android.bluetooth.le.ScanSettings.MATCH_MODE_STICKY)
                .build()
    }

    override suspend fun stop(): Boolean {
        log.info("stop")
        super.stop()
        val result = asyncBle {
            val bluetoothAdapter = this@XYSmartScanModern.bluetoothAdapter

            if (bluetoothAdapter == null) {
                log.info("stop: Bluetooth Disabled")
                return@asyncBle XYBluetoothResult(false)
            }

            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                log.info("stop:Failed to get Bluetooth Scanner. Disabled?")
                return@asyncBle XYBluetoothResult(false)
            }

            scanner.stopScan(callback)
            return@asyncBle XYBluetoothResult(true)
        }.await()

        if (result.error != null) {
            return false
        }
        return result.value!!

    }
}