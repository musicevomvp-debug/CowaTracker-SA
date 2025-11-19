package com.cowatracker.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cowatracker.app.data.AppDatabase
import com.cowatracker.app.data.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var orderViewModel: OrderViewModel
    private var orderId: Long = -1
    private var isManualEntry: Boolean = false
    private var currentOrder: Order? = null

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvTime: TextView
    private lateinit var etCustomerAddress: EditText
    private lateinit var etPickupAddress: EditText
    private lateinit var etDeliveryAddress: EditText
    private lateinit var etDistance: EditText
    private lateinit var etEarnings: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        orderViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)

        orderId = intent.getLongExtra("ORDER_ID", -1)
        isManualEntry = intent.getBooleanExtra("MANUAL_ENTRY", false)

        initializeViews()

        if (orderId != -1L) {
            loadOrder()
        } else if (isManualEntry) {
            setupForManualEntry()
        }

        setupButtons()
    }

    private fun initializeViews() {
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvTime = findViewById(R.id.tvTime)
        etCustomerAddress = findViewById(R.id.etCustomerAddress)
        etPickupAddress = findViewById(R.id.etPickupAddress)
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress)
        etDistance = findViewById(R.id.etDistance)
        etEarnings = findViewById(R.id.etEarnings)
        etNotes = findViewById(R.id.etNotes)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun loadOrder() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            currentOrder = database.orderDao().getOrderById(orderId)

            currentOrder?.let { order ->
                runOnUiThread {
                    populateFields(order)
                }
            }
        }
    }

    private fun populateFields(order: Order) {
        tvOrderNumber.text = "Order ${order.orderNumber ?: "#${order.id}"}"

        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        tvTime.text = dateFormat.format(Date(order.notificationTime))

        etCustomerAddress.setText(order.customerAddress ?: "")
        etPickupAddress.setText(order.pickupAddress ?: "")
        etDeliveryAddress.setText(order.deliveryAddress ?: "")
        etDistance.setText(order.distanceKm?.toString() ?: "")
        etEarnings.setText(order.earnings?.toString() ?: "")
        etNotes.setText(order.notes ?: "")
    }

    private fun setupForManualEntry() {
        tvOrderNumber.text = "New Manual Order"
        tvTime.text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())
        btnDelete.isEnabled = false
    }

    private fun setupButtons() {
        btnSave.setOnClickListener {
            saveOrder()
        }

        btnDelete.setOnClickListener {
            deleteOrder()
        }
    }

    private fun saveOrder() {
        val customerAddress = etCustomerAddress.text.toString()
        val pickupAddress = etPickupAddress.text.toString()
        val deliveryAddress = etDeliveryAddress.text.toString()
        val distanceText = etDistance.text.toString()
        val earningsText = etEarnings.text.toString()
        val notes = etNotes.text.toString()

        val distance = distanceText.toDoubleOrNull()
        val earnings = earningsText.toDoubleOrNull()

        val currentGPSDistance = LocationService.getCurrentDistance()
        val finalDistance = distance ?: currentGPSDistance

        if (currentOrder != null) {
            val updated = currentOrder!!.copy(
                customerAddress = customerAddress.ifBlank { null },
                pickupAddress = pickupAddress.ifBlank { null },
                deliveryAddress = deliveryAddress.ifBlank { null },
                distanceKm = finalDistance,
                earnings = earnings,
                notes = notes.ifBlank { null },
                lastModified = System.currentTimeMillis()
            )
            orderViewModel.updateOrder(updated)
        } else {
            val newOrder = Order(
                notificationTime = System.currentTimeMillis(),
                customerAddress = customerAddress.ifBlank { null },
                pickupAddress = pickupAddress.ifBlank { null },
                deliveryAddress = deliveryAddress.ifBlank { null },
                distanceKm = finalDistance,
                earnings = earnings,
                notes = notes.ifBlank { null },
                status = "completed",
                isManualEntry = true
            )
            orderViewModel.insertOrder(newOrder)
        }

        Toast.makeText(this, "Order saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteOrder() {
        currentOrder?.let { order ->
            orderViewModel.deleteOrder(order)
            Toast.makeText(this, "Order deleted", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
