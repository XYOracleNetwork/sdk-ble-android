package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_characteristic.view.*
import network.xyo.ble.gatt.server.XYBluetoothCharacteristic
import network.xyo.ble.sample.R
import network.xyo.ui.XYBaseFragment
import java.nio.charset.Charset

class CharacteristicFragment(private val characteristic: BluetoothGattCharacteristic) : XYBaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_characteristic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update(view)

        view.characteristic_button_update.setOnClickListener { update(view) }
    }

    private fun update (view: View) {
        view.characteristics_uuid_title.text = characteristic.uuid.toString()
        view.characteristics_type.text = getCharacteristicType()
        view.characteristics_permission.text = getCharacteristicPermissions()
        view.characteristics_value_hex.text = getCharacteristicValueHex()
        view.characteristics_value_utf8.text = getCharacteristicValueUtf8()
    }

    private fun getCharacteristicValueHex() : String {
        return "Hex Value: ${bytesToString(characteristic.value ?: byteArrayOf())}"
    }

    private fun getCharacteristicValueUtf8() : String {
        val string = "UTF8 Value: "
        val value = characteristic.value
        if (value != null) {
            return string + value.toString(Charset.defaultCharset())
        }
        return string
    }

    private fun getCharacteristicType() : String {
        var string = "Properties: "

        for (property in XYBluetoothCharacteristic.Companion.Properties.values()) {
            if (characteristic.properties and property.value == property.value) {
                string += "$property "
            }
        }

        return string
    }

    private fun getCharacteristicPermissions () : String {
        var string = "Permissions: "

        for (property in XYBluetoothCharacteristic.Companion.Permissions.values()) {
            if (characteristic.properties and property.value == property.value) {
                string += "$property "
            }
        }

        return string
    }

    companion object {
        fun newInstance(characteristic: BluetoothGattCharacteristic): CharacteristicFragment {
            return CharacteristicFragment(characteristic)
        }

        fun bytesToString(bytes: ByteArray): String {
            val sb = StringBuilder()
            val it = bytes.iterator()
            sb.append("0x")
            while (it.hasNext()) {
                sb.append(String.format("%02X ", it.next()))
            }

            return sb.toString()
        }
    }
}