package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import network.xyo.base.XYBase
import network.xyo.ble.sample.XYApplication
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.sample.R
import network.xyo.ble.sample.databinding.FragmentBasicBinding
import network.xyo.ble.sample.databinding.FragmentBatteryBinding

@kotlin.ExperimentalUnsignedTypes
abstract class XYAppBaseFragment<T> : Fragment() where T: ViewBinding {

    val log = XYBase.log("XYAppBaseFragment")

    private var _binding: T? = null
    val binding get() = _binding!!

    val scanner: XYSmartScan
        get() {
            return (this.activity!!.applicationContext as XYApplication).scanner
        }

    abstract fun inflate(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = this.inflate(inflater, container)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    open fun update() {}


    fun checkConnectionError(hasConnectionError: Boolean) {
        if (hasConnectionError) {
            log.error("Connection failed. Try Refresh")
        }
    }

    companion object: XYBase()
}
