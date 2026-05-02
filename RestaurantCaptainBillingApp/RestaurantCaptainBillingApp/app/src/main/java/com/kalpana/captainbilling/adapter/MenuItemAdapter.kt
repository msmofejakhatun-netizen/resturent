package com.kalpana.captainbilling.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kalpana.captainbilling.R
import com.kalpana.captainbilling.model.MenuItem
import java.util.Locale

class MenuItemAdapter(
    private val items: List<MenuItem>,
    private val quantityProvider: (Int) -> Int,
    private val onIncrease: (MenuItem) -> Unit,
    private val onDecrease: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun refreshQuantities() {
        notifyItemRangeChanged(0, items.size)
    }

    inner class MenuItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val category: TextView = view.findViewById(R.id.tvCategory)
        private val name: TextView = view.findViewById(R.id.tvItemName)
        private val price: TextView = view.findViewById(R.id.tvPrice)
        private val quantity: TextView = view.findViewById(R.id.tvQty)
        private val plus: TextView = view.findViewById(R.id.btnPlus)
        private val minus: TextView = view.findViewById(R.id.btnMinus)

        fun bind(item: MenuItem) {
            category.text = item.category
            name.text = item.name
            price.text = String.format(Locale.US, "Rs. %.2f", item.price)
            quantity.text = quantityProvider(item.id).toString()
            plus.setOnClickListener { onIncrease(item) }
            minus.setOnClickListener { onDecrease(item) }
        }
    }
}
