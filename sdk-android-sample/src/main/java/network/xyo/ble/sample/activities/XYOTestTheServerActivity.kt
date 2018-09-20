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
import kotlinx.coroutines.experimental.async
import network.xyo.ble.gatt.server.*
import network.xyo.ble.sample.R
import network.xyo.ble.sample.adapters.XYServiceListAdapter
import network.xyo.ble.sample.fragments.*
import network.xyo.ui.XYBaseFragment
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

class XYOTestTheServerActivity : XYOAppBaseActivity() {
    private var bleServer : XYBluetoothGattServer? = null
    private var bleAdvertiser : XYBluetoothAdvertiser? = null
    private val serviceList = XYServiceListAdapter()

    private val simpleService = XYBluetoothService(UUID.fromString("3079ca44-ae64-4797-b4e5-a31e3304c481"), BluetoothGattService.SERVICE_TYPE_PRIMARY)
    private val characteristicRead = XYBluetoothReadCharacteristic(UUID.fromString("01ef8f90-e99f-48ae-87bb-f683b93c692f"))
    private val characteristicWrite = XYBluetoothWriteCharacteristic(UUID.fromString("02ef8f90-e99f-48ae-87bb-f683b93c692f"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activicty_ble_server)


        val serverPagerContainer = findViewById<ViewPager>(R.id.server_pager_container)
        serverPagerContainer.adapter = SectionsPagerAdapter(supportFragmentManager)
        serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs))
        serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(server_tabs) as ViewPager.OnPageChangeListener)


//        val recyclerView = findViewById<RecyclerView>(R.id.serviceList)
//        val manager = LinearLayoutManager(this.applicationContext, LinearLayout.VERTICAL, false)
//        manager.reverseLayout = true
//        manager.stackFromEnd = true
//        recyclerView.layoutManager = manager
//        recyclerView.setHasFixedSize(true)
//        recyclerView.adapter = serviceList
//
         spinUpServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleServer?.stopServer()
    }


    private fun createTestServer() = async {
        val server = XYBluetoothGattServer(applicationContext)
        server.startServer()
        bleServer = server
        return@async server.addService(simpleService).await()
    }

    private fun spinUpServer () = async {
        simpleService.addCharacteristic(characteristicRead)
        simpleService.addCharacteristic(characteristicWrite)

        characteristicRead.addResponder("countResponder", countResponder)
        characteristicWrite.addResponder("log Responder",logResponder)

        createTestServer().await()

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
        private val size = 1
        private var fragments: SparseArray<XYBaseFragment> = SparseArray(size)

        override fun getItem(position: Int): Fragment {
            return AdvertiserFragment.newInstance(XYBluetoothAdvertiser(applicationContext))
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