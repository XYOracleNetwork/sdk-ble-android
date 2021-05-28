package network.xyo.ble.sample.fragments.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import network.xyo.ble.generic.bluetooth.BluetoothIntentReceiver
import network.xyo.ble.generic.gatt.peripheral.XYBluetoothResult
import network.xyo.ble.generic.gatt.server.*
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothReadResponder
import network.xyo.ble.generic.gatt.server.responders.XYBluetoothWriteResponder
import network.xyo.ble.sample.databinding.FragmentPeripheralBinding
import network.xyo.ble.sample.fragments.XYAppBaseFragment
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.UUID
import network.xyo.ble.generic.gatt.peripheral.ble


class ServerFragment : XYAppBaseFragment<FragmentPeripheralBinding>() {
    private var bleServer : XYBluetoothGattServer? = null
    private var bleAdvertiser : XYBluetoothAdvertiser? = null
    private val bluetoothIntentReceiver = BluetoothIntentReceiver()
    private lateinit var pagerAdapter: SectionsPagerAdapter

    private val simpleService = XYBluetoothService(
            UUID.fromString("3079ca44-ae64-4797-b4e5-a31e3304c481"),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    private val characteristicRead = XYBluetoothCharacteristic(
            UUID.fromString("01ef8f90-e99f-48ae-87bb-f683b93c692f"),
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
    )

    private val characteristicWrite = XYBluetoothCharacteristic(
            UUID.fromString("02ef8f90-e99f-48ae-87bb-f683b93c692f"),
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    val services = arrayOf<BluetoothGattService>(simpleService)

    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentPeripheralBinding {
        return FragmentPeripheralBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabAdapter = SectionsPagerAdapter(this.childFragmentManager, services, bleAdvertiser)
        pagerAdapter = tabAdapter
        binding.serverPagerContainer.adapter = pagerAdapter
        binding.serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.serverTabs))
        binding.serverPagerContainer.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.serverTabs) as ViewPager.OnPageChangeListener)
        binding.serverTabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(binding.serverPagerContainer))
        bleAdvertiser = XYBluetoothAdvertiser(context!!.applicationContext)

        activity!!.registerReceiver(bluetoothIntentReceiver, BluetoothIntentReceiver.bluetoothDeviceIntentFilter)

        ble.launch {
            spinUpServer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity!!.unregisterReceiver(bluetoothIntentReceiver)
        bleServer?.stopServer()
    }

    private suspend fun createTestServer(): XYBluetoothResult<Int> {
        val server = XYBluetoothGattServer(context!!.applicationContext)
        server.startServer()
        bleServer = server
        return server.addService(simpleService)
    }

    private suspend fun spinUpServer () = ble.async {
        simpleService.addCharacteristic(characteristicRead)
        simpleService.addCharacteristic(characteristicWrite)
        characteristicRead.addReadResponder("countResponder", countResponder)
        characteristicWrite.addWriteResponder("log Responder",logResponder)
        return@async createTestServer()
    }.await()

    /**
     * A simple write characteristic that logs whenever it is written to.
     */
    private val logResponder = object : XYBluetoothWriteResponder {
        override fun onWriteRequest(writeRequestValue: ByteArray?, device: BluetoothDevice?): Boolean {
            Log.v("BluetoothGattServer", writeRequestValue?.toString(Charset.defaultCharset())!!)
            return true
        }
    }

    /**
     * A simple read characteristic that increases one time every time it is read.
     */
    private val countResponder = object : XYBluetoothReadResponder {
        var count = 0

        override fun onReadRequest(device: BluetoothDevice?, offset: Int): XYBluetoothGattServer.XYReadRequest {
            count++
            return XYBluetoothGattServer.XYReadRequest( ByteBuffer.allocate(4).putInt(count).array(), 0)
        }
    }
}
