package network.xyo.ble.sample.activities

import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import network.xyo.ble.sample.R
import kotlinx.android.synthetic.main.activity_app.*
import network.xyo.base.XYBase
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.ble.sample.fragments.CentralFragment
import network.xyo.ble.sample.fragments.ServerFragment

@kotlin.ExperimentalUnsignedTypes
@kotlin.ExperimentalStdlibApi
class XYOAppActivity : XYOAppBaseActivity() {
    private lateinit var pagerAdapter: SectionsPagerAdapter
    private var deviceAdapter: BaseAdapter? = null
    private val log = XYBase.log("XYOAppActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        log.info("onCreate")
        deviceAdapter = XYDeviceAdapter(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app)

        val tabAdapter = SectionsPagerAdapter(supportFragmentManager)
        pagerAdapter = tabAdapter
        val pagerContainer = findViewById<ViewPager>(R.id.server_pager_container)
        pagerContainer.adapter = pagerAdapter
        pagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs))
        pagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs) as ViewPager.OnPageChangeListener)
        server_tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(server_pager_container))
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val size = 2
        private val fragments: SparseArray<Fragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return CentralFragment.newInstance(deviceAdapter!!)
                1 -> return ServerFragment.newInstance()
            }

            throw Exception("Position out of index!")
        }

        override fun getCount(): Int {
            return size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            fragments.append(position, fragment)
            return fragment
        }

        fun getFragmentByPosition(position: Int): Fragment? {
            return fragments.get(position)
        }
    }
}
