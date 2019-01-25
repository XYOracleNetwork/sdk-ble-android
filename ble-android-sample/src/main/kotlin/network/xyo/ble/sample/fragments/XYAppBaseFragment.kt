package network.xyo.ble.sample.fragments


import network.xyo.ble.sample.fragments.core.ProgressListener
import network.xyo.core.XYBase
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

abstract class XYAppBaseFragment : XYBaseFragment() {
    var progressListener : ProgressListener? = null

    override fun onPause() {
        super.onPause()
        progressListener?.hideProgress()
    }

    open fun update() {}


    fun checkConnectionError(hasConnectionError: Boolean) {
        if (hasConnectionError) {
            ui {
                progressListener?.showProgress()
                showToast("Connection failed. Try Refresh")
            }

        }
    }

    companion object: XYBase()
}