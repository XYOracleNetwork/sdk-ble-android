package network.xyo.ble.sample.fragments

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import network.xyo.ble.generic.gatt.server.XYBluetoothCharacteristicPermissions
import network.xyo.ble.generic.gatt.server.XYBluetoothCharacteristicProperties
import network.xyo.ble.sample.databinding.FragmentCharacteristicBinding
import java.nio.charset.Charset

class CharacteristicFragment(private var characteristic: BluetoothGattCharacteristic) : XYAppBaseFragment<FragmentCharacteristicBinding>() {
    override fun inflate(inflater: LayoutInflater, container: ViewGroup?): FragmentCharacteristicBinding {
        return FragmentCharacteristicBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()

        binding.characteristicButtonUpdate.setOnClickListener { update() }
    }

    override fun update () {
        super.update()
        binding.characteristicsUuidTitle.text = characteristic.uuid.toString()
        binding.characteristicsType.text = getCharacteristicType()
        binding.characteristicsPermission.text = getCharacteristicPermissions()
        binding.characteristicsValueHex.text = getCharacteristicValueHex()
        binding.characteristicsValueUtf8.text = getCharacteristicValueUtf8()
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

        for (property in XYBluetoothCharacteristicProperties.values()) {
            if (characteristic.properties.and(property.value) == property.value) {
                string += "$property "
            }
        }

        return string
    }

    private fun getCharacteristicPermissions () : String {
        var string = "Permissions: "

        for (property in XYBluetoothCharacteristicPermissions.values()) {
            if (characteristic.properties.and(property.value) == property.value) {
                string += "$property "
            }
        }

        return string
    }

    companion object {
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
