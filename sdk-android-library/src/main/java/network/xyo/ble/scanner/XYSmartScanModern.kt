package network.xyo.ble.scanner

import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import network.xyo.ble.gatt.XYBluetoothResult
import network.xyo.ble.gatt.asyncBle
import network.xyo.core.guard
import java.util.*

@TargetApi(21)
class XYSmartScanModern(context: Context) : XYSmartScan(context) {
    override suspend fun start(): Boolean {
        logInfo("start")
        super.start()

        val result = asyncBle {

            val bluetoothAdapter = bluetoothManager?.adapter

            bluetoothAdapter.guard {
                logInfo("Bluetooth Disabled")
                return@asyncBle XYBluetoothResult(false)
            }

            val scanner = bluetoothAdapter?.bluetoothLeScanner
            if (scanner == null) {
                logInfo("startScan:Failed to get Bluetooth Scanner. Disabled?")
                return@asyncBle XYBluetoothResult(false)
            } else {
                val filters = ArrayList<ScanFilter>()
                scanner.startScan(filters, getSettings(), callback)
            }

            return@asyncBle XYBluetoothResult(true)
        }.await()

        if (result.error != null) {
            return false
        }
        return result.value!!
    }

    private val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            //logInfo("onBatchScanResults: $results")
            results.guard { return }
            val xyResults = ArrayList<XYScanResult>()
            for (result in results!!) {
                xyResults.add(XYScanResultModern(result))
            }
            onScanResult(xyResults)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            logError("onScanFailed: ${errorCode}, ${codeToScanFailed(errorCode)}", false)
            if (ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED == errorCode) {
                restartBluetooth()
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            //logInfo("onBatchScanResults: $result")
            result.guard { return }
            val xyResults = ArrayList<XYScanResult>()
            xyResults.add(XYScanResultModern(result!!))
            onScanResult(xyResults)
        }
    }

    private fun getSettings(): ScanSettings {
        return ScanSettings.Builder().setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY).build()
    }

    override suspend fun stop(): Boolean {
        logInfo("stop")
        super.stop()
        val result = asyncBle {
            val bluetoothAdapter = this@XYSmartScanModern.bluetoothAdapter

            if (bluetoothAdapter == null) {
                logInfo("stop: Bluetooth Disabled")
                return@asyncBle XYBluetoothResult(false)
            }

            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                logInfo("stop:Failed to get Bluetooth Scanner. Disabled?")
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