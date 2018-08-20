package network.xyo.ble.sample.fragments

import android.content.Context
import network.xyo.ble.sample.activities.XYOFinderDeviceActivity
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

abstract class XYAppBaseFragment : XYBaseFragment() {

    var activity: XYOFinderDeviceActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is XYOFinderDeviceActivity) {
            activity = context
        } else {
            logError(TAG, "context is not instance of XYOFinderDeviceActivity!", true)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.hideProgressSpinner()
    }

    open fun unsupported(text: String) {
        activity?.showToast(text)
        ui {
            activity?.hideProgressSpinner()
        }
    }

    companion object {
        private val TAG = XYAppBaseFragment::class.java.simpleName
    }
}