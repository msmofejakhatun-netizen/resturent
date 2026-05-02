package com.kalpana.captainbilling.model

data class MenuItem(
    val id: Int,
    val category: String,
    val name: String,
    val price: Double
)

data class BillLine(
    val itemId: Int,
    val itemName: String,
    val qty: Int,
    val price: Double,
    val total: Double
)

data class BillTotals(
    val subtotal: Double = 0.0,
    val cgst: Double = 0.0,
    val sgst: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0
)

data class BillSnapshot(
    val restaurantName: String,
    val address: String,
    val dateTime: String,
    val cashier: String,
    val tableNo: String,
    val items: List<BillLine>,
    val totals: BillTotals
)
