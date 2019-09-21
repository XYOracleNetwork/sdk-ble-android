package network.xyo.ble.sample.fragments

import network.xyo.base.XYBase
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYSmartScan
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

@kotlin.ExperimentalUnsignedTypes
abstract class XYAppBaseFragment : XYBaseFragment() {

    val scanner: XYSmartScan
        get() {
            return (this.activity!!.applicationContext as XYApplication).scanner
        }

    override fun onPause() {
        super.onPause()
        throbber?.hide()
    }

    open fun update() {}


    fun checkConnectionError(hasConnectionError: Boolean) {
        if (hasConnectionError) {
            ui {
                showToast("Connection failed. Try Refresh")
            }

        }
    }

    companion object: XYBase()
}