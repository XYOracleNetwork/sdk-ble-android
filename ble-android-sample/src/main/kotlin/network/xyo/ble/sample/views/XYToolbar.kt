package network.xyo.ble.sample.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import network.xyo.ble.sample.R
import network.xyo.ble.sample.activities.XYOAppBaseActivity

@ExperimentalUnsignedTypes
open class XYToolbar : Toolbar {

    open var isBackNavigationEnabled = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        if (context is XYOAppBaseActivity) {
            context.setSupportActionBar(this)
            val actionBar = context.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayShowHomeEnabled(true)
                actionBar.setDisplayHomeAsUpEnabled(false)
                actionBar.setDisplayShowCustomEnabled(true)
                actionBar.setDisplayShowTitleEnabled(false)
            }
        }
    }

    fun enableMenuNavigation(onClickListener: OnClickListener) {
        isBackNavigationEnabled = false
        navigationIcon = ContextCompat.getDrawable(context, R.mipmap.xy_ui_toolbar_menu)
        setNavigationOnClickListener(onClickListener)
    }

    fun enableBackNavigation(activity: XYOAppBaseActivity) {
        isBackNavigationEnabled = true
        navigationIcon = ContextCompat.getDrawable(context, R.mipmap.xy_ui_toolbar_back)
        setNavigationOnClickListener { activity.onBackPressed() }
    }

    fun enableBackNavigation(onClickListener: OnClickListener) {
        isBackNavigationEnabled = true
        navigationIcon = ContextCompat.getDrawable(context, R.mipmap.xy_ui_toolbar_back)
        setNavigationOnClickListener(onClickListener)
    }

    companion object {

        private val TAG = XYToolbar::class.java.simpleName
    }
}
