package network.xyo.ble.sample.fragments

import android.content.Context
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.sample.activities.XYODeviceActivity
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

abstract class XYAppBaseFragment : XYBaseFragment() {

    var activity: XYODeviceActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is XYODeviceActivity) {
            activity = context
        } else {
            log.error("context is not instance of XYODeviceActivity!", true)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.hideProgressSpinner()
    }

    open fun update() {}


    fun checkConnectionError(hasConnectionError: Boolean) {
        if (hasConnectionError) {
            ui {
                activity?.hideProgressSpinner()
                showToast("Connection failed. Try Refresh")
            }

        }
    }

    companion object: XYBase()
}