package network.xyo.ble.sample.activities

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_device.*
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.devices.XYFinderBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ble.sample.fragments.*
import network.xyo.ble.sample.fragments.core.BackFragmentListener
import network.xyo.ui.XYBaseFragment
import network.xyo.ui.ui

/**
 * Created by arietrouw on 12/28/17.
 */

@kotlin.ExperimentalStdlibApi
@kotlin.ExperimentalUnsignedTypes
class XYODeviceActivity : XYOAppBaseActivity() {

    var device: XYBluetoothDevice? = null
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    lateinit var data: XYDeviceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceHash = intent.getStringExtra(EXTRA_DEVICEHASH)
        log.info("onCreate: $deviceHash")
        device = scanner.devices[deviceHash]
        if (device == null) {
            showToast("Failed to Find Device")
            finish()
        }
        setContentView(R.layout.activity_device)

        data = XYDeviceData()

        sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = sectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

    }

    override fun onStop() {
        super.onStop()
        device!!.removeListener(TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log.info(TAG, "onActivityResult requestCode: $requestCode")

        val frag = (supportFragmentManager.findFragmentById(R.id.container) as FirmwareUpdateFragment?)
        frag?.onFileSelected(requestCode, resultCode, data)
    }

    private val xy3DeviceListener = object : XY3BluetoothDevice.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            update()
            ui {
                showToast("Entered")
            }
        }

        override fun exited(device: XYBluetoothDevice) {
            update()
            ui {
                showToast("Exited")
            }
        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged: $newState")
            update()
            if (newState == 2) {
                ui {
                    showToast("Connected")
                }
            } else {
                ui {
                    showToast("Disconnected")
                }
            }
        }

        override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Single")
            }
        }

        override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Double")
            }
        }

        override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Long")
            }
        }
    }

    private val xy4DeviceListener = object : XY4BluetoothDevice.Listener() {
        override fun entered(device: XYBluetoothDevice) {
            update()
            ui {
                showToast("Entered")
            }
        }

        override fun exited(device: XYBluetoothDevice) {
            update()
            ui {
                showToast("Exited")
            }
        }

        override fun detected(device: XYBluetoothDevice) {
            update()
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            log.info("connectionStateChanged: $newState")
            update()
            ui {
                if (newState == 2) {
                    showToast("Connected")
                } else {
                    showToast("Disconnected")
                }
            }
        }

        override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Single")
            }
        }

        override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Double")
            }
        }

        override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
            ui {
                showToast("Button Pressed: Long")
            }
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
        val activeFrag = sectionsPagerAdapter.getFragmentByPosition(container.currentItem)
        if (activeFrag is BackFragmentListener && (activeFrag as BackFragmentListener).onBackPressed()) {
            //Let the fragment handle the back button.
        } else {
            super.onBackPressed()
        }
    }

    fun update() {
        ui {
            val frag = sectionsPagerAdapter.getFragmentByPosition(container.currentItem)
            (frag as? InfoFragment)?.update()
        }
    }

    companion object {
        var EXTRA_DEVICEHASH = "DeviceHash"
        private val TAG = XYODeviceActivity::class.java.simpleName
    }


    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val size = 11
        private var fragments: SparseArray<XYBaseFragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            lateinit var frag: XYAppBaseFragment

            when (position) {
                0 -> {
                    frag = InfoFragment.newInstance(device, data)
                }
                1 -> {
                    frag = AlertFragment.newInstance(device, data)
                }
                2 -> {
                    frag = BatteryFragment.newInstance(device, data)
                }
                3 -> {
                    frag = CurrentTimeFragment.newInstance(device, data)
                }
                4 -> {
                    frag = DeviceFragment.newInstance(device, data)
                }
                5 -> {
                    frag = GenericAccessFragment.newInstance(device, data)
                }
                6 -> {
                    frag = GenericAttributeFragment.newInstance(device, data)
                }
                7 -> {
                    frag = LinkLossFragment.newInstance(device, data)
                }
                8 -> {
                    frag = TxPowerFragment.newInstance(device, data)
                }
                9 -> {
                    frag = SongFragment.newInstance(device, data)
                }
                10 -> {
                    frag = FirmwareUpdateFragment.newInstance(device, data)
                }
                else -> frag = InfoFragment.newInstance(device, data)
            }

            return frag
        }

        override fun getCount(): Int {
            return size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as XYBaseFragment
            fragments.append(position, fragment)
            return fragment
        }

        fun getFragmentByPosition(position: Int): XYBaseFragment {
            return fragments.get(position)
        }

    }
}
