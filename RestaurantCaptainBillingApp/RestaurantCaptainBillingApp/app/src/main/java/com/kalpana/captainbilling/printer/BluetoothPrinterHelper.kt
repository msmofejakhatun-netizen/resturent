package com.kalpana.captainbilling.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.kalpana.captainbilling.model.BillSnapshot
import java.util.Locale

class BluetoothPrinterHelper(private val context: Context) {

    fun hasBluetoothPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun pairedPrinterSummary(): String {
        if (!hasBluetoothPermission()) return "Printer: Bluetooth permission required"
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return "Printer: Bluetooth not supported"
        if (!adapter.isEnabled) return "Printer: Bluetooth is off"

        val printers = adapter.bondedDevices
            .filter { device ->
                val name = device.name.orEmpty().lowercase(Locale.US)
                name.contains("epson") || name.contains("printer") || name.contains("pos") || name.contains("thermal")
            }
            .map { it.name ?: it.address }

        return if (printers.isEmpty()) {
            "Printer: no paired thermal printer found"
        } else {
            "Printer: ${printers.joinToString()}"
        }
    }

    @SuppressLint("MissingPermission")
    fun printBill(snapshot: BillSnapshot): Result<Unit> {
        return runCatching {
            if (!hasBluetoothPermission()) error("Bluetooth permission is required")
            if (snapshot.items.isEmpty()) error("No bill items to print")

            val connection = preferredPrinterConnection()
                ?: error("No paired Bluetooth printer found")
            try {
                val printer = EscPosPrinter(connection, 203, 48f, 32)
                printer.printFormattedText(buildReceipt(snapshot))
            } finally {
                connection.disconnect()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun preferredPrinterConnection(): BluetoothConnection? {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
        val printerDevice = adapter.bondedDevices.firstOrNull { device ->
            val name = device.name.orEmpty().lowercase(Locale.US)
            name.contains("epson") || name.contains("printer") || name.contains("pos") || name.contains("thermal")
        }
        return printerDevice?.let { BluetoothConnection(it) }
            ?: BluetoothPrintersConnections.selectFirstPaired()
    }

    fun buildReceipt(snapshot: BillSnapshot): String {
        val sb = StringBuilder()
        sb.append("[C]<b>${snapshot.restaurantName.uppercase(Locale.US)}</b>\n")
        sb.append("[C]${snapshot.address}\n")
        sb.append("[L]--------------------------------\n")
        sb.append("[L]Date: ${snapshot.dateTime}\n")
        sb.append("[L]Cashier: ${snapshot.cashier}\n")
        sb.append("[L]Dine In: ${snapshot.tableNo}\n")
        sb.append("[L]--------------------------------\n")
        sb.append("[L]<b>Item Name</b>[C]<b>Qty</b>[R]<b>Amount</b>\n")
        sb.append("[L]--------------------------------\n")
        snapshot.items.forEach { item ->
            sb.append("[L]${fitName(item.itemName)}[C]${item.qty}[R]${money(item.total)}\n")
        }
        sb.append("[L]--------------------------------\n")
        sb.append("[L]Sub Total[R]${money(snapshot.totals.subtotal)}\n")
        sb.append("[L]CGST 2.5%[R]${money(snapshot.totals.cgst)}\n")
        sb.append("[L]SGST 2.5%[R]${money(snapshot.totals.sgst)}\n")
        sb.append("[L]Round Off[R]${money(snapshot.totals.roundOff)}\n")
        sb.append("[L]--------------------------------\n")
        sb.append("[L]<b>GRAND TOTAL</b>[R]<b>${money(snapshot.totals.grandTotal)}</b>\n")
        sb.append("[L]--------------------------------\n")
        sb.append("[C]Thank You Visit Again!\n\n")
        return sb.toString()
    }

    private fun fitName(name: String): String {
        return if (name.length <= 16) name else name.take(15) + "."
    }

    private fun money(value: Double): String = String.format(Locale.US, "%.2f", value)
}
