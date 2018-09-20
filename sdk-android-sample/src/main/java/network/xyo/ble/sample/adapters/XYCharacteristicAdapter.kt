package network.xyo.ble.sample.adapters

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import network.xyo.ble.sample.R

class XYCharacteristicAdapter(services : Array<BluetoothGattCharacteristic>) : RecyclerView.Adapter<XYCharacteristicAdapter.ViewHolder>() {
    private val listeners = HashMap<String, XYCharacteristicAdapterListener>()
    private val list: ArrayList<BluetoothGattCharacteristic> = ArrayList(services.asList())

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.uuid.text = list[position].uuid.toString()
        holder.button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                for ((_, listener) in listeners) {
                    listener.onClick(list[position])
                }
            }
        })
    }

    fun addListener(key : String, listener : XYCharacteristicAdapterListener) {
        listeners[key] = listener
    }

    fun removeListener(key: String, listener : XYCharacteristicAdapterListener) {
        listeners.remove(key)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.characteristic_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getList(): ArrayList<BluetoothGattCharacteristic> {
        return list
    }

    fun addItem(item: BluetoothGattCharacteristic) {
        list.add(item)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val uuid = itemView.findViewById<TextView>(R.id.characteristic_uuid)
        val button = itemView.findViewById<Button>(R.id.view_characteristic_button)
    }

    companion object {
        interface XYCharacteristicAdapterListener {
            fun onClick(service : BluetoothGattCharacteristic)
        }
    }
}