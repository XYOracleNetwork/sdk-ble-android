package network.xyo.ble.sample.fragments


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
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.firmware.OtaFile
import network.xyo.ble.firmware.OtaUpdate
import network.xyo.ble.sample.R
import network.xyo.ui.ui


class FirmwareUpdateFragment : XYAppBaseFragment() {

    private var firmwareFileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filesDirExists = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.getBoolean("fileDirectoriesCreated", false) ?: false

        if (!filesDirExists) {
            if (OtaFile.createFileDirectory()) {
                context?.getSharedPreferences("settings", Context.MODE_PRIVATE)?.edit()?.putBoolean("fileDirectoriesCreated", true)?.apply()
            } else {
                logInfo(TAG, "Failed to create files directory")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_firmware_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_update.setOnClickListener {
            performUpdate()
        }

        //setup file listview
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

    private val updateListener = object : OtaUpdate.Listener() {
        override fun updated(device: XYBluetoothDevice) {
            logInfo("updateListener: updated")
            ui {
                activity?.hideProgressSpinner()
                showToast("Update complete. Rebooting device...")
                activity?.onBackPressed()
            }
        }

        override fun failed(device: XYBluetoothDevice, error: String) {
            logInfo("updateListener: failed: $error")
            ui {
                tv_file_progress?.text = "Update failed: $error"
                activity?.hideProgressSpinner()
                button_update?.isEnabled = true
                lv_files?.visibility = VISIBLE
                tv_file_name?.visibility = VISIBLE
            }
        }

        override fun progress(sent: Int, total: Int) {
            val txt = "sending chunk  $sent of $total"
            logInfo(txt)
            ui {
                tv_file_progress?.text = txt
            }
        }
    }

    private fun performUpdate() {
        GlobalScope.launch {
            if (firmwareFileName != null) {
                ui {
                    lv_files?.visibility = GONE
                    tv_file_name?.visibility = GONE
                    tv_file_progress?.visibility = VISIBLE
                    button_update?.isEnabled = false
                    activity?.showProgressSpinner()
                    tv_file_progress?.text = getString(R.string.update_started)
                }

                logInfo(TAG, "testFirmware start: $String")
                (activity?.device as? XY4BluetoothDevice)?.updateFirmware(firmwareFileName!!, updateListener)
            } else {
                ui { showToast("Select a File first") }
            }
        }
    }

    //Callback from XYOFinderDeviceActivity.onActivityResult
    fun onFileSelected(requestCode: Int, resultCode: Int, data: Intent?) {
        logInfo(TAG, "onFileSelected requestCode: $requestCode")

        data?.data.let { uri ->
            tv_file_name?.text = uri.toString()
        }

    }

    companion object {
        private val TAG = FirmwareUpdateFragment::class.java.simpleName

        fun newInstance() =
                FirmwareUpdateFragment()
    }
}
