package network.xyo.ble.sample.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import network.xyo.ble.sample.adapters.XYDeviceAdapter
import network.xyo.ble.sample.databinding.ActivityAppBinding
import network.xyo.ble.sample.fragments.CentralFragment
import network.xyo.ble.sample.fragments.server.ServerFragment

@kotlin.ExperimentalUnsignedTypes
@kotlin.ExperimentalStdlibApi
@Suppress("unused")
class XYOAppActivity : XYOAppBaseActivity() {
    private lateinit var pagerAdapter: SectionsPagerAdapter
    private lateinit var deviceAdapter:  XYDeviceAdapter
    private lateinit var binding: ActivityAppBinding

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceAdapter = XYDeviceAdapter(this)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initServerPagerContainer()
        initServerTabs()
    }

    private fun initServerPagerContainer() {
        binding.serverPagerContainer.adapter = SectionsPagerAdapter(supportFragmentManager)
        binding.serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.serverTabs))
    }

    private fun initServerTabs() {
        binding.serverTabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(binding.serverPagerContainer))
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val size = 2
        private val fragments: SparseArray<Fragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return CentralFragment(deviceAdapter)
                1 -> return ServerFragment()
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
