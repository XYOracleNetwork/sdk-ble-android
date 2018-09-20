package network.xyo.ble.sample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import network.xyo.ble.gatt.server.XYBluetoothService
import network.xyo.ble.sample.R

class XYServiceListAdapter : RecyclerView.Adapter<XYServiceListAdapter.ViewHolder>() {
    private var list: ArrayList<XYBluetoothService> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.uuid.text = list[position].uuid.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.service_item, parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun getList(): ArrayList<XYBluetoothService> {
        return list
    }

    fun addItem(item: XYBluetoothService) {
        list.add(item)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val uuid = itemView.findViewById<TextView>(R.id.uuid_service)
    }
}