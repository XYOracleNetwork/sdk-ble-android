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
import kotlinx.android.synthetic.main.fragment_firmware_update.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.firmware.OtaFile
import network.xyo.ble.firmware.OtaUpdate
import network.xyo.ble.sample.R
import network.xyo.ui.ui
import network.xyo.ble.sample.fragments.core.BackFragmentListener
import network.xyo.core.XYBase

class FirmwareUpdateFragment : XYAppBaseFragment(), BackFragmentListener {

    private var firmwareFileName: String? = null
    private var updateInProgress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filesDirExists = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.getBoolean("fileDirectoriesCreated", false) ?: false

        if (!filesDirExists) {
            if (OtaFile.createFileDirectory()) {
                context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.edit()?.putBoolean("fileDirectoriesCreated", true)?.apply()
            } else {
                log.info("Failed to create files directory")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_firmware_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_update.setOnClickListener {
            performUpdate()
        }

        //setup file listview
        val context = context
        if (context != null) {
            val fileListAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
            lv_files?.adapter = fileListAdapter

            val fileList = OtaFile.list()
            if (fileList == null) {
                showToast("No Firmware files found. Add files in device folder 'Xyo'")
            } else {
                for (file in fileList) {
                    fileListAdapter.add(file)
                }

                lv_files.setOnItemClickListener { _, _, i, _ ->
                    firmwareFileName = fileList[i]
                }
            }
        }
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

    private val updateListener = object : OtaUpdate.Listener() {
        override fun updated(device: XYBluetoothDevice) {
            log.info("updateListener: updated")
            updateInProgress = false
            ui {
                activity?.hideProgressSpinner()
                showToast("Update complete. Rebooting device...")
                activity?.onBackPressed()
            }
        }

        override fun failed(device: XYBluetoothDevice, error: String) {
            log.info("updateListener: failed: $error")
            updateInProgress = false
            val gattError = error.contains("133")

            ui {
                showToast("Update failed: $error")
                if (gattError) {
                    promptRefreshAdapter()
                }

                activity?.hideProgressSpinner()
                button_update?.isEnabled = true
                lv_files?.visibility = VISIBLE
                tv_file_name?.visibility = VISIBLE
            }
        }

        override fun progress(sent: Int, total: Int) {
            val txt = "sending chunk  $sent of $total"
            log.info(txt)
            ui {
                tv_file_progress?.text = txt
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
        activity?.showProgressSpinner()
        GlobalScope.launch {
            val device: XYBluetoothDevice? = activity?.device
            //need to connect before refreshing
            device?.connectGatt()?.await()
            val result = device?.refreshGatt()?.await()
            ui { activity?.hideProgressSpinner() }
            if (result?.value as Boolean) {
                ui { showToast("BLE adapter was reset, performing update") }
                performUpdate()
            } else {
                ui { showToast("Failed to refresh BLE adapter") }
            }
            activity?.showToast(result.toString())

        }
    }

    private fun performUpdate() {
        GlobalScope.launch {
            if (firmwareFileName != null) {
                updateInProgress = true
                ui {
                    lv_files?.visibility = GONE
                    tv_file_name?.visibility = GONE
                    tv_file_progress?.visibility = VISIBLE
                    button_update?.isEnabled = false
                    activity?.showProgressSpinner()
                    tv_file_progress?.text = getString(R.string.update_started)
                }

                log.info("performUpdate started: $String")
                (activity?.device as? XY4BluetoothDevice)?.updateFirmware(firmwareFileName!!, updateListener)
            } else {
                ui { showToast("Select a File first") }
            }
        }
    }

    //Callback from XYODeviceActivity.onActivityResult
    // TODO - Why are we making this dependency? [AT] --
    @Suppress("UNUSED_PARAMETER")
    fun onFileSelected(requestCode: Int, resultCode: Int, data: Intent?) {
        log.info( "onFileSelected requestCode: $requestCode")

        data?.data.let { uri ->
            tv_file_name?.text = uri.toString()
        }

    }

    companion object: XYBase() {
        fun newInstance() = FirmwareUpdateFragment()
    }
}
