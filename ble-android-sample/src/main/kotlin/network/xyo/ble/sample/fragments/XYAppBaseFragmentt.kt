package network.xyo.ble.sample.fragments

import android.content.Context
import network.xyo.ble.sample.activities.XYOFinderDeviceActivity
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

abstract class XYAppBaseFragment : XYBaseFragment() {

    var activity: XYOFinderDeviceActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is XYOFinderDeviceActivity) {
            activity = context
        } else {
            log.error("context is not instance of XYOFinderDeviceActivity!", true)
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