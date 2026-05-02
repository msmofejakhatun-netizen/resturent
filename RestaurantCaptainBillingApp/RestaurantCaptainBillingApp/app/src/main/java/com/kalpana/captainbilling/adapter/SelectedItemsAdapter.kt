package com.kalpana.captainbilling.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kalpana.captainbilling.R
import com.kalpana.captainbilling.model.BillLine
import java.util.Locale

class SelectedItemsAdapter : RecyclerView.Adapter<SelectedItemsAdapter.SelectedItemViewHolder>() {
    private val items = mutableListOf<BillLine>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected, parent, false)
        return SelectedItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(lines: List<BillLine>) {
        items.clear()
        items.addAll(lines)
        notifyDataSetChanged()
    }

    class SelectedItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvSelectedName)
        private val qty: TextView = view.findViewById(R.id.tvSelectedQty)
        private val price: TextView = view.findViewById(R.id.tvSelectedPrice)
        private val total: TextView = view.findViewById(R.id.tvSelectedTotal)

        fun bind(line: BillLine) {
            name.text = line.itemName
            qty.text = line.qty.toString()
            price.text = String.format(Locale.US, "%.2f", line.price)
            total.text = String.format(Locale.US, "%.2f", line.total)
        }
    }
}
