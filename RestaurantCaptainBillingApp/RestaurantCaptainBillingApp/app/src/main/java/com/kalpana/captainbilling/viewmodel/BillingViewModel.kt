package com.kalpana.captainbilling.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kalpana.captainbilling.model.BillLine
import com.kalpana.captainbilling.model.BillSnapshot
import com.kalpana.captainbilling.model.BillTotals
import com.kalpana.captainbilling.model.MenuItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

class BillingViewModel : ViewModel() {
    val menuItems: List<MenuItem> = listOf(
        MenuItem(1, "Roti", "Roti", 49.0),
        MenuItem(2, "Roti", "Butter Roti", 59.0),
        MenuItem(3, "Roti", "Tandoori Roti", 45.0),
        MenuItem(4, "Naan", "Plain Naan", 69.0),
        MenuItem(5, "Naan", "Butter Naan", 79.0),
        MenuItem(6, "Naan", "Garlic Naan", 89.0),
        MenuItem(7, "Sabji", "Paneer Butter Masala", 239.0),
        MenuItem(8, "Sabji", "Mix Veg", 189.0),
        MenuItem(9, "Sabji", "Dal Tadka", 159.0),
        MenuItem(10, "Sabji", "Veg Kolhapuri", 209.0),
        MenuItem(11, "Drinks", "Mineral Water", 25.0),
        MenuItem(12, "Drinks", "Fresh Lime Soda", 79.0),
        MenuItem(13, "Drinks", "Cold Drink", 49.0)
    )

    private val quantities = mutableMapOf<Int, Int>()
    private val _selectedItems = MutableLiveData<List<BillLine>>(emptyList())
    private val _totals = MutableLiveData(BillTotals())

    val selectedItems: LiveData<List<BillLine>> = _selectedItems
    val totals: LiveData<BillTotals> = _totals

    fun quantityFor(itemId: Int): Int = quantities[itemId] ?: 0

    fun increase(item: MenuItem) {
        quantities[item.id] = quantityFor(item.id) + 1
        recalculate()
    }

    fun decrease(item: MenuItem) {
        val next = quantityFor(item.id) - 1
        if (next > 0) {
            quantities[item.id] = next
        } else {
            quantities.remove(item.id)
        }
        recalculate()
    }

    fun clear() {
        quantities.clear()
        recalculate()
    }

    fun createSnapshot(restaurantName: String, address: String, cashier: String, tableNo: String): BillSnapshot {
        val dateTime = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
        return BillSnapshot(
            restaurantName = restaurantName,
            address = address,
            dateTime = dateTime,
            cashier = cashier.ifBlank { "Captain 01" },
            tableNo = tableNo.ifBlank { "-" },
            items = _selectedItems.value.orEmpty(),
            totals = _totals.value ?: BillTotals()
        )
    }

    private fun recalculate() {
        val lines = menuItems.mapNotNull { item ->
            val qty = quantities[item.id] ?: 0
            if (qty == 0) null else BillLine(
                itemId = item.id,
                itemName = item.name,
                qty = qty,
                price = item.price,
                total = item.price * qty
            )
        }
        val subtotal = lines.sumOf { it.total }
        val cgst = subtotal * 0.025
        val sgst = subtotal * 0.025
        val beforeRound = subtotal + cgst + sgst
        val grandTotal = round(beforeRound)
        val roundOff = grandTotal - beforeRound

        _selectedItems.value = lines
        _totals.value = BillTotals(
            subtotal = subtotal,
            cgst = cgst,
            sgst = sgst,
            roundOff = roundOff,
            grandTotal = grandTotal
        )
    }
}
