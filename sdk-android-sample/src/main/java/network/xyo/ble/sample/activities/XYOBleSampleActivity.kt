package network.xyo.ble.sample.activities

import android.os.Bundle
import android.widget.BaseAdapter
import network.xyo.core.XYBase
import network.xyo.core.XYPermissions
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import kotlinx.android.synthetic.main.activity_xyo_ble_sample.*

class XYOBleSampleActivity : XYOAppBaseActivity() {
    private var adapter: BaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        logInfo("onCreate")
        super.onCreate(savedInstanceState)
        XYBase.init(this)
        setContentView(R.layout.activity_xyo_ble_sample)

        adapter = XYDeviceAdapter(this)
        listview!!.adapter = adapter
    }

    override fun onResume() {
        logInfo("onResume")
        super.onResume()
        val permissions = XYPermissions(this)
        permissions.requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,
                "Location services are needed to connection and track your finders.",
                XYPermissions.LOCATION_PERMISSIONS_REQ_CODE)
        adapter?.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        scanner.start()
    }
}
