package network.xyo.ble.sample.activities

import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import network.xyo.ble.sample.R
import network.xyo.ble.sample.fragments.BeepTestFragment

@kotlin.ExperimentalUnsignedTypes
@Suppress("unused")
class XYOTestActivity : XYOAppBaseActivity() {
    private lateinit var pagerAdapter: SectionsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val serverTabs = findViewById<TabLayout>(R.id.server_tabs)
        val testPagerContainer = findViewById<ViewPager>(R.id.test_pager_container)

        val tabAdapter = SectionsPagerAdapter(supportFragmentManager)
        pagerAdapter = tabAdapter
        testPagerContainer.adapter = pagerAdapter
        testPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(serverTabs))
        testPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(serverTabs) as ViewPager.OnPageChangeListener)
        serverTabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(testPagerContainer))
    }

    class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val size = 1
        private val fragments: SparseArray<Fragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return BeepTestFragment.newInstance()
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
