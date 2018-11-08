package network.xyo.ble.sample.activities

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activicty_ble_server.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import network.xyo.ble.gatt.server.*
import network.xyo.ble.sample.R
import network.xyo.ble.sample.fragments.AdvertiserFragment
import network.xyo.ble.sample.fragments.RootServicesFragment
import network.xyo.ui.XYBaseFragment
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

class XYOServerActivity : XYOAppBaseActivity() {
    private var bleServer : XYBluetoothGattServer? = null
    private var bleAdvertiser : XYBluetoothAdvertiser? = null
    private val simpleService = XYBluetoothService(UUID.fromString("3079ca44-ae64-4797-b4e5-a31e3304c481"), BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val characteristicRead = XYBluetoothReadCharacteristic(UUID.fromString("01ef8f90-e99f-48ae-87bb-f683b93c692f"))
    private val characteristicWrite = XYBluetoothWriteCharacteristic(UUID.fromString("02ef8f90-e99f-48ae-87bb-f683b93c692f"))

    override fun onBluetoothDisabled() {

    }

    override fun onBluetoothEnabled() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activicty_ble_server)


        val pagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        val serverPagerContainer = findViewById<ViewPager>(R.id.server_pager_container)
        serverPagerContainer.adapter = pagerAdapter
        serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs))
        serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs) as ViewPager.OnPageChangeListener)

        GlobalScope.async {
            spinUpServer().await()
            val fragment = pagerAdapter.getFragmentByPosition(1) as RootServicesFragment
            fragment.addService(simpleService)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleServer?.stopServer()
    }

    private fun createTestServer() = GlobalScope.async {
        val server = XYBluetoothGattServer(applicationContext)
        server.startServer()
        bleServer = server
        server.addService(simpleService).await()
    }

    private fun spinUpServer () = GlobalScope.async {
        simpleService.addCharacteristic(characteristicRead)
        simpleService.addCharacteristic(characteristicWrite)


        createTestServer().await()
        characteristicRead.addResponder("countResponder", countResponder)
        characteristicWrite.addResponder("log Responder",logResponder)
        return@async
    }

    /**
     * A simple write characteristic that logs whenever it is written to.
     */
    private val logResponder = object : XYBluetoothWriteCharacteristic.XYBluetoothWriteCharacteristicResponder {
        override fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean? {
            Log.v("BluetoothGattServer", writeRequestValue?.toString(Charset.defaultCharset()))
            return true
        }
    }

    /**
     * A simple read characteristic that increases one time every time it is read.
     */
    private val countResponder = object : XYBluetoothReadCharacteristic.XYBluetoothReadCharacteristicResponder {
        var count = 0

        override fun onReadRequest(device: BluetoothDevice?): ByteArray? {
            count++
            return ByteBuffer.allocate(4).putInt(count).array()
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val size = 2
        private val fragments: SparseArray<XYBaseFragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            println(bleServer?.getServices()?.size)
            when (position) {
                0 -> return AdvertiserFragment.newInstance(XYBluetoothAdvertiser(applicationContext))
                1 -> return RootServicesFragment.newInstance(bleServer?.getServices())
            }

            return RootServicesFragment.newInstance(bleServer?.getServices())
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