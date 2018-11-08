package network.xyo.ble.sample.adapters

import android.bluetooth.BluetoothGattService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import network.xyo.ble.sample.R

class XYServiceListAdapter(services : Array<BluetoothGattService>) : RecyclerView.Adapter<XYServiceListAdapter.ViewHolder>() {
    private val listeners = HashMap<String, XYServiceListAdapterListener>()
    private val list: ArrayList<BluetoothGattService> = ArrayList(services.asList())

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.uuid.text = list[position].uuid.toString()
        holder.button.setOnClickListener {
            for ((_, listener) in listeners) {
                listener.onClick(list[position])
            }
        }
    }

    fun addListener(key : String, listener : XYServiceListAdapterListener) {
        listeners[key] = listener
    }

    fun removeListener(key: String, listener : XYServiceListAdapterListener) {
        listeners.remove(key)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.service_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getList(): ArrayList<BluetoothGattService> {
        return list
    }

    fun addItem(item: BluetoothGattService) {
        list.add(item)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val uuid = itemView.findViewById<TextView>(R.id.service_uuid)
        val button = itemView.findViewById<Button>(R.id.view_service_button)
    }

    companion object {
        interface XYServiceListAdapterListener {
            fun onClick(service : BluetoothGattService)
        }
    }
}