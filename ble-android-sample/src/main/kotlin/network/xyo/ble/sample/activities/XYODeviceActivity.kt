package network.xyo.ble.sample.activities

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import network.xyo.base.XYBase
import network.xyo.ble.devices.xy.*
import network.xyo.ble.generic.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.databinding.ActivityDeviceBinding
import network.xyo.ble.sample.fragments.*
import network.xyo.ble.sample.fragments.core.BackFragmentListener

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class XYODeviceActivity : XYOAppBaseActivity() {

    var device: XYBluetoothDevice? = null
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    lateinit var data: XYDeviceData
    private val log = XYBase.log("XYODeviceActivity")

    private lateinit var binding: ActivityDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        val deviceHash = intent.getStringExtra(EXTRA_DEVICE_HASH)!!
        log.info("onCreate: $deviceHash")
        device = scanner.devices[deviceHash]
        if (device == null) {
            log.error("Device not found")
            finish()
        }
        setContentView(R.layout.activity_device)

        data = XYDeviceData()

        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        binding.container.adapter = sectionsPagerAdapter

        binding.container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabs))
        binding.tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(binding.container))

    }

    override fun onStop() {
        super.onStop()
        device!!.removeListener(TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.info(TAG, "onActivityResult requestCode: $requestCode")

        val frag = (supportFragmentManager.findFragmentById(R.id.container) as FirmwareUpdateFragment?)
        frag?.onFileSelected(requestCode, data)
    }

    private val xy3DeviceListener = object : XY3BluetoothDeviceListener() {
        override fun entered(device: XYBluetoothDevice) {
            update()
            log.info("Entered")
        }

        override fun exited(device: XYBluetoothDevice) {
            update()
            log.info("Exited")
        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged: $newState")
            update()
            if (newState == 2) {
                log.info("Connected")
            } else {
                log.info("Disconnected")
            }
        }

        override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Single")
        }

        override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Double")
        }

        override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Long")
        }
    }

    private val xy4DeviceListener = object : XY4BluetoothDeviceListener() {
        override fun entered(device: XYBluetoothDevice) {
            update()
            log.info("Entered")
        }

        override fun exited(device: XYBluetoothDevice) {
            update()
            log.info("Exited")
        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged: $newState")
            update()
            if (newState == 2) {
                log.info("Connected")
            } else {
                log.info("Disconnected")
            }
        }

        override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Single")
        }

        override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Double")
        }

        override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
            log.info("Button Pressed: Long")
        }
    }

    private fun addListener() {
        log.info("addListener: $device")
        (device as? XY4BluetoothDevice)?.addListener(TAG, xy4DeviceListener)
        (device as? XY3BluetoothDevice)?.addListener(TAG, xy3DeviceListener)
    }

    private fun removeListener() {
        log.info("removeListener: $device")
        (device as? XY4BluetoothDevice)?.removeListener(TAG)
        (device as? XY3BluetoothDevice)?.removeListener(TAG)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addListener()
    }

    override fun onPause() {
        super.onPause()
        removeListener()
    }

    override fun onResume() {
        super.onResume()
        addListener()
    }

    override fun onBackPressed() {
        val activeFrag = sectionsPagerAdapter.getFragmentByPosition(binding.container.currentItem)
        if (!(activeFrag is BackFragmentListener && (activeFrag as BackFragmentListener).onBackPressed())) {
            super.onBackPressed()
        }
    }

    fun update() {
        runOnUiThread {
            val frag = sectionsPagerAdapter.getFragmentByPosition(binding.container.currentItem)
            (frag as? InfoFragment)?.update()
        }
    }

    companion object {
        var EXTRA_DEVICE_HASH = "DeviceHash"
        private val TAG = XYODeviceActivity::class.java.simpleName
    }


    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val size = 17
        private var fragments: SparseArray<Fragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            lateinit var frag: Fragment

            when (position) {
                0 -> {
                    frag = InfoFragment.newInstance(device, data)
                }
                1 -> {
                    frag = DeviceServicesFragment.newInstance(device, data)
                }
                2 -> {
                    frag = AlertFragment.newInstance(device, data)
                }
                3 -> {
                    frag = BatteryFragment.newInstance(device, data)
                }
                4 -> {
                    frag = CurrentTimeFragment.newInstance(device, data)
                }
                5 -> {
                    frag = DeviceFragment.newInstance(device, data)
                }
                6 -> {
                    frag = GenericAccessFragment.newInstance(device, data)
                }
                7 -> {
                    frag = GenericAttributeFragment.newInstance(device, data)
                }
                8 -> {
                    frag = LinkLossFragment.newInstance(device, data)
                }
                9 -> {
                    frag = TxPowerFragment.newInstance(device, data)
                }
                10 -> {
                    frag = SongFragment.newInstance(device, data)
                }
                11 -> {
                    frag = FirmwareUpdateFragment.newInstance(device, data)
                }
                12 -> {
                    frag = PrimaryFragment.newInstance(device, data)
                }
                13 -> {
                    frag = BasicFragment.newInstance(device, data)
                }
                14 -> {
                    frag = ExtendedConfigFragment.newInstance(device, data)
                }
                15 -> {
                    frag = ControlFragment.newInstance(device, data)
                }
                16 -> {
                    frag = SensorFragment.newInstance(device, data)
                }
                else -> frag = InfoFragment.newInstance(device, data)
            }

            return frag
        }

        override fun getCount(): Int {
            return size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            fragments.append(position, fragment)
            return fragment
        }

        fun getFragmentByPosition(position: Int): Fragment {
            return fragments.get(position)
        }

    }
}
