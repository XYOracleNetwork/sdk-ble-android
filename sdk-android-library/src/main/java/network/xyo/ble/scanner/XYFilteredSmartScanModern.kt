package network.xyo.ble.scanner

import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.core.guard
import java.util.*

@TargetApi(21)
class XYFilteredSmartScanModern(context: Context) : XYFilteredSmartScan(context) {
    override fun start() {
        logInfo("start")
        super.start()

        GlobalScope.launch(BluetoothThread) {

            val bluetoothAdapter = bluetoothManager?.adapter

            bluetoothAdapter.guard {
                logInfo("Bluetooth Disabled")
                return@launch
            }

            val scanner = bluetoothAdapter?.bluetoothLeScanner
            if (scanner == null) {
                logInfo("startScan:Failed to get Bluetooth Scanner. Disabled?")
                return@launch
            } else {
                val filters = ArrayList<ScanFilter>()
                scanner.startScan(filters, getSettings(), callback)
            }
        }
    }

    val callback = object : ScanCallback() {
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
            logError("onScanFailed: ${codeToScanFailed(errorCode)}", false)
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

    override fun stop() {
        logInfo("stop")
        super.stop()
        GlobalScope.launch(BluetoothThread, CoroutineStart.DEFAULT) {
            val bluetoothAdapter = this@XYFilteredSmartScanModern.bluetoothAdapter

            if (bluetoothAdapter == null) {
                logInfo("stop: Bluetooth Disabled")
                return@launch
            }

            val scanner = bluetoothAdapter.bluetoothLeScanner
            if (scanner == null) {
                logInfo("stop:Failed to get Bluetooth Scanner. Disabled?")
                return@launch
            }

            scanner.stopScan(callback)
        }
    }
}