package network.xyo.ble.sample.fragments

import android.content.Context
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYOFinderDeviceActivity
import network.xyo.ble.services.Service
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

abstract class XYAppBaseFragment : XYBaseFragment() {

    var activity: XYOFinderDeviceActivity? = null
    private var parentJob = Job()

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
        parentJob.cancel()
        activity?.hideProgressSpinner()
    }

    open fun update() {}

    open fun initServiceSetTextView(service: Service.IntegerCharacteristic, textView: TextView) {
        ui {
            activity?.showProgressSpinner()
        }
        parentJob = launch(CommonPool) {
            val result = service.get().await()
            val error = result.error
            when {
                error != null -> ui { textView.text = error.message.toString() }
                result.value != null -> ui { textView.text = result.value.toString() }
                else -> ui { textView.text = getString(R.string.not_available) }
            }

            ui {
                activity?.hideProgressSpinner()
            }
        }
    }

    open fun initServiceSetTextView(service: Service.StringCharacteristic, textView: TextView?) {
        ui {
            activity?.showProgressSpinner()
        }
        parentJob = launch(CommonPool) {
            val result = service.get().await()
            val error = result.error
            when {
                error != null -> ui { textView?.text = error.message.toString() }
                result.value != null -> ui { textView?.text = result.value.toString() }
                else -> ui { textView?.text = getString(R.string.not_available) }
            }

            ui {
                activity?.hideProgressSpinner()
            }
        }
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