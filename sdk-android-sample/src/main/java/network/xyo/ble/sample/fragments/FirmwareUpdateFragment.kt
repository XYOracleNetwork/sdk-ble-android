package network.xyo.ble.sample.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_firmware_update.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.sample.R
import java.io.InputStream
import java.net.URL


class FirmwareUpdateFragment : XYAppBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_firmware_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_select_file.setOnClickListener {
            performFileSearch()
        }

        button_update.setOnClickListener {
            if (!tv_file_name.text.isBlank()) {
                performUpdate(tv_file_name.text.toString())
            } else {
                showToast("Select a file.")
            }
        }
    }

    private fun performUpdate(filename: String) {
        launch(CommonPool) {
            logInfo(TAG, "testFirmware start: $String")

            val stream = getRemoteFile(filename)
            val result = stream?.let { (activity?.device as? XY4BluetoothDevice)?.updateFirmware(it)?.await() }
            logInfo(TAG, "testFirmware result: $result")

        }
    }

    fun getRemoteFile(location: String, useCache: Boolean = true): InputStream? {
        val url: URL = if (useCache) {
            URL(location)
        } else {
            URL(location + "?t=" + Math.random())
        }
        val inputStream = url.openStream()
        val tmp = inputStream
        inputStream.close()

        return tmp
    }

    fun performFileSearch() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        intent.type = "*/*"

        startActivityForResult(intent, FILE_REQUEST)
    }

    //Callback from XYOFinderDeviceActivity.onActivityResult
    fun onFileSelected(requestCode: Int, resultCode: Int, data: Intent?) {
        logInfo(TAG, "onFileSelected requestCode: $requestCode")

        data?.data.let { uri ->
            tv_file_name.text = uri.toString()
        }

    }

    companion object {
        private val TAG = FirmwareUpdateFragment::class.java.simpleName
        const val FILE_REQUEST = 555

        fun newInstance() =
                FirmwareUpdateFragment()
    }
}
