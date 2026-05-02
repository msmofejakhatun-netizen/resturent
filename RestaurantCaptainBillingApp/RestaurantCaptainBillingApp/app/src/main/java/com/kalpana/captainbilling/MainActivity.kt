package com.kalpana.captainbilling

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.kalpana.captainbilling.adapter.MenuItemAdapter
import com.kalpana.captainbilling.adapter.SelectedItemsAdapter
import com.kalpana.captainbilling.model.BillSnapshot
import com.kalpana.captainbilling.printer.BluetoothPrinterHelper
import com.kalpana.captainbilling.viewmodel.BillingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val viewModel: BillingViewModel by viewModels()
    private lateinit var menuAdapter: MenuItemAdapter
    private lateinit var selectedAdapter: SelectedItemsAdapter
    private lateinit var printerHelper: BluetoothPrinterHelper

    private val gson = Gson()
    private val prefs by lazy { getSharedPreferences("captain_billing", Context.MODE_PRIVATE) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        updatePrinterStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printerHelper = BluetoothPrinterHelper(this)
        setupInputs()
        setupRecyclerViews()
        setupObservers()
        setupActions()
        requestBluetoothPermissionIfNeeded()
        updatePrinterStatus()
    }

    private fun setupInputs() {
        findViewById<TextInputEditText>(R.id.etCaptainName).setText(
            prefs.getString("captain_name", "Captain 01")
        )
    }

    private fun setupRecyclerViews() {
        menuAdapter = MenuItemAdapter(
            items = viewModel.menuItems,
            quantityProvider = { viewModel.quantityFor(it) },
            onIncrease = { viewModel.increase(it) },
            onDecrease = { viewModel.decrease(it) }
        )
        selectedAdapter = SelectedItemsAdapter()

        findViewById<RecyclerView>(R.id.rvMenuItems).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = menuAdapter
        }
        findViewById<RecyclerView>(R.id.rvSelectedItems).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = selectedAdapter
        }
    }

    private fun setupObservers() {
        viewModel.selectedItems.observe(this) { lines ->
            selectedAdapter.submitList(lines)
            menuAdapter.refreshQuantities()
        }
        viewModel.totals.observe(this) { totals ->
            findViewById<TextView>(R.id.tvSubtotal).text = totalRow("Subtotal", totals.subtotal)
            findViewById<TextView>(R.id.tvCgst).text = totalRow("CGST (2.5%)", totals.cgst)
            findViewById<TextView>(R.id.tvSgst).text = totalRow("SGST (2.5%)", totals.sgst)
            findViewById<TextView>(R.id.tvRoundOff).text = totalRow("Round Off", totals.roundOff)
            findViewById<TextView>(R.id.tvGrandTotal).text = String.format(
                Locale.US,
                "GRAND TOTAL  %.2f",
                totals.grandTotal
            )
        }
    }

    private fun setupActions() {
        findViewById<MaterialButton>(R.id.btnPrint).setOnClickListener {
            val snapshot = currentSnapshot()
            if (snapshot.items.isEmpty()) {
                toast("Add at least one item")
                return@setOnClickListener
            }
            saveLastBill(snapshot)
            printSnapshot(snapshot, "Bill printed")
        }

        findViewById<MaterialButton>(R.id.btnClear).setOnClickListener {
            viewModel.clear()
            findViewById<TextInputEditText>(R.id.etTableNo).setText("")
        }

        findViewById<MaterialButton>(R.id.btnReprint).setOnClickListener {
            val lastBill = loadLastBill()
            if (lastBill == null) {
                toast("No last bill saved")
            } else {
                printSnapshot(lastBill, "Last bill printed")
            }
        }
    }

    private fun printSnapshot(snapshot: BillSnapshot, successMessage: String) {
        lifecycleScope.launch {
            setPrintingEnabled(false)
            val result = withContext(Dispatchers.IO) {
                printerHelper.printBill(snapshot)
            }
            setPrintingEnabled(true)
            result.fold(
                onSuccess = { toast(successMessage) },
                onFailure = { toast(it.message ?: "Unable to print bill") }
            )
        }
    }

    private fun setPrintingEnabled(enabled: Boolean) {
        findViewById<MaterialButton>(R.id.btnPrint).isEnabled = enabled
        findViewById<MaterialButton>(R.id.btnReprint).isEnabled = enabled
    }

    private fun currentSnapshot(): BillSnapshot {
        val restaurant = findViewById<TextView>(R.id.tvRestaurantName).text.toString()
        val address = findViewById<TextView>(R.id.tvAddress).text.toString()
        val captain = findViewById<TextInputEditText>(R.id.etCaptainName).text?.toString().orEmpty()
        val table = findViewById<TextInputEditText>(R.id.etTableNo).text?.toString().orEmpty()
        prefs.edit().putString("captain_name", captain.ifBlank { "Captain 01" }).apply()
        return viewModel.createSnapshot(restaurant, address, captain, table)
    }

    private fun saveLastBill(snapshot: BillSnapshot) {
        prefs.edit().putString("last_bill", gson.toJson(snapshot)).apply()
    }

    private fun loadLastBill(): BillSnapshot? {
        return prefs.getString("last_bill", null)?.let {
            runCatching { gson.fromJson(it, BillSnapshot::class.java) }.getOrNull()
        }
    }

    private fun requestBluetoothPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !printerHelper.hasBluetoothPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }
    }

    private fun updatePrinterStatus() {
        findViewById<TextView>(R.id.tvPrinterStatus).text = printerHelper.pairedPrinterSummary()
    }

    private fun totalRow(label: String, amount: Double): String {
        return String.format(Locale.US, "%-16s %8.2f", label, amount)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
