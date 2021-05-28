@file:Suppress("BlockingMethodInNonBlockingContext")

package network.xyo.ble.sample.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.xyo.ble.devices.xy.XY4BluetoothDevice
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.firmware.XYOtaFile
import network.xyo.ble.firmware.XYOtaUpdateListener
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.FragmentFirmwareUpdateBinding
import network.xyo.ble.sample.fragments.core.BackFragmentListener
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import network.xyo.ble.generic.gatt.peripheral.ble

class FirmwareUpdateFragment(device: XYBluetoothDevice, deviceData : XYDeviceData) : XYDeviceFragment<FragmentFirmwareUpdateBinding>(device, deviceData), BackFragmentListener {

    private var firmwareFileName: String? = null
    private var updateInProgress: Boolean = false

    private val folderName = "Xyo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filesDirExists = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.getBoolean("fileDirectoriesCreated", false) ?: false

        if (!filesDirExists) {
            if (XYOtaFile.createFileDirectory(folderName)) {
                context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.edit()?.putBoolean("fileDirectoriesCreated", true)?.apply()
            } else {
                log.info("Failed to create files directory")
            }
        }
    }

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentFirmwareUpdateBinding {
        return FragmentFirmwareUpdateBinding.inflate(inflater, container, false)
    }

    private fun loadImageFromServer() {
        ble.launch {
            readFromServer()
            loadList()
        }
    }

    private fun loadList() {
        val context = context
        if (context != null) {
            val fileListAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
            binding.lvFiles.adapter = fileListAdapter

            val fileList = XYOtaFile.list(folderName)
            if (fileList == null) {
                log.error("No Firmware files found. Add files in device folder 'Xyo'")
            } else {
                for (file in fileList) {
                    fileListAdapter.add(file)
                }

                binding.lvFiles.setOnItemClickListener { _, _, i, _ ->
                    firmwareFileName = fileList[i]
                }
            }
        }
    }

    private suspend fun readFromServer() = withContext(Dispatchers.IO) {
        XYOtaFile.createFileDirectory(folderName)
        val url = URL("https://s3.amazonaws.com/xyfirmware.xyo.network/xy4_585-0-v56.img")
        val connection = url.openConnection()
        connection.connectTimeout = 60000
        val inBuffer = BufferedInputStream(connection.getInputStream())
        val outStream = FileOutputStream("${XYOtaFile.folderPath(folderName)}/xy4_585-0-v56.img")
        val buffer = ByteArray(1024)
        var len = inBuffer.read(buffer)
        while (len > 0) {
            outStream.write(buffer, 0, len)
            len = inBuffer.read(buffer)
        }
        inBuffer.close()
        outStream.close()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonUpdate.setOnClickListener {
            performUpdate()
        }

        binding.buttonLoadFromServer.setOnClickListener {
            loadImageFromServer()
        }

        loadList()
    }

    override fun onBackPressed(): Boolean {
        return if (updateInProgress) {
            //prompt user that update is in progress
            promptCancelUpdate()
            true

        } else {
            // update is not running - allow Activity to handle backPress
            false
        }

    }

    private fun promptCancelUpdate() {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("Update in progress")
        alertDialog.setMessage("Please wait for the update to complete.")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") {dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private val updateListener = object : XYOtaUpdateListener() {
        override fun updated(device: XYBluetoothDevice) {
            log.info("updateListener: updated")
            updateInProgress = false
            activity?.runOnUiThread {
                log.info("Update complete. Rebooting device...")
                activity?.onBackPressed()
            }
        }

        override fun failed(device: XYBluetoothDevice, error: String) {
            log.info("updateListener: failed: $error")
            updateInProgress = false
            val gattError = error.contains("133")

            activity?.runOnUiThread {
                log.error("Update failed: $error")
                if (gattError) {
                    promptRefreshAdapter()
                }

                binding.buttonUpdate.isEnabled = true
                binding.lvFiles.visibility = VISIBLE
                binding.tvFileName.visibility = VISIBLE
            }
        }

        override fun progress(sent: Int, total: Int) {
            val txt = "sending chunk  $sent of $total"
            log.info(txt)
            activity?.runOnUiThread {
                binding.tvFileProgress.text = txt
            }
        }
    }

    private fun promptRefreshAdapter() {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("BLE Adapter Error")
        alertDialog.setMessage("Your BLE adapter may be in a bad state. Would you like to reset your BLE and try again?")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") {dialog, _ ->
            dialog.dismiss()
            refreshAdapter()
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No, cancel update") {dialog, _ ->
            dialog.dismiss()
            refreshAdapter()
        }
        alertDialog.show()
    }

    private fun refreshAdapter() {
        ble.launch {
            //need to connect before refreshing
            val result = device.connect()
            // val result = device?.refreshGatt()?.await()
            if (result.value as Boolean) {
                activity?.runOnUiThread { log.error("BLE adapter was reset, performing update") }
                performUpdate()
            } else {
                activity?.runOnUiThread { log.error("Failed to refresh BLE adapter") }
            }
            log.info(result.toString())
        }
    }

    private fun performUpdate() {
        ble.launch {
            if (firmwareFileName != null) {
                updateInProgress = true
                activity?.runOnUiThread {
                    binding.lvFiles.visibility = GONE
                    binding.tvFileName.visibility = GONE
                    binding.tvFileProgress.visibility = VISIBLE
                    binding.buttonUpdate.isEnabled = false
                    binding.tvFileProgress.text = getString(R.string.update_started)
                }

                log.info("performUpdate started: $String")
                firmwareFileName?.let {
                    (device as? XY4BluetoothDevice)?.updateFirmware(folderName, it, updateListener)
                }
            } else {
                activity?.runOnUiThread { log.error("Select a File first") }
            }
        }
    }

    //Callback from XYODeviceActivity.onActivityResult
    fun onFileSelected(requestCode: Int,  data: Intent?) {
        log.info( "onFileSelected requestCode: $requestCode")

        data?.data.let { uri ->
            binding.tvFileName.text = uri.toString()
        }

    }
}
