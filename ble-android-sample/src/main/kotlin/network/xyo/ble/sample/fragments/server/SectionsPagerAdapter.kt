package network.xyo.ble.sample.fragments.server

import android.bluetooth.BluetoothGattService
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import network.xyo.ble.generic.gatt.server.XYBluetoothAdvertiser
import network.xyo.ble.sample.fragments.AdvertiserFragment
import network.xyo.ble.sample.fragments.RootServicesFragment

@ExperimentalUnsignedTypes
class SectionsPagerAdapter(
        fm: FragmentManager,
        private var startingServices : Array<BluetoothGattService>,
        private var bleAdvertiser : XYBluetoothAdvertiser?
        ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val size = 2
    private val fragments: SparseArray<Fragment> = SparseArray(size)

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return AdvertiserFragment(bleAdvertiser)
            1 -> return RootServicesFragment(startingServices)
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
}
