package network.xyo.ble.sample.fragments

import androidx.fragment.app.Fragment
import network.xyo.base.XYBase
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.generic.scanner.XYSmartScan

@kotlin.ExperimentalUnsignedTypes
abstract class XYAppBaseFragment : Fragment() {

    val log = XYBase.log("XYAppBaseFragment")

    val scanner: XYSmartScan
        get() {
            return (this.activity!!.applicationContext as XYApplication).scanner
        }

    open fun update() {}


    fun checkConnectionError(hasConnectionError: Boolean) {
        if (hasConnectionError) {
            log.error("Connection failed. Try Refresh")
        }
    }

    companion object: XYBase()
}
