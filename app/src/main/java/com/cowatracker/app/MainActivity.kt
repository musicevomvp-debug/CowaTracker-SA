package com.cowatracker.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.cowatracker.app.data.Order
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var orderAdapter: OrderAdapter
    private var isTracking = false

    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var btnToggleTracking: Button
    private lateinit var fabAddOrder: FloatingActionButton
    private lateinit var tvNotificationStatus: TextView
    private lateinit var tvLocationStatus: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalEarnings: TextView
    private lateinit var btnReports: Button
    private lateinit var btnSettings: Button

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]
        setupRecyclerView()
        setupButtons()
        checkPermissions()
        observeOrders()
    }

    private fun initializeViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders)
        btnToggleTracking = findViewById(R.id.btnToggleTracking)
        fabAddOrder = findViewById(R.id.fabAddOrder)
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        tvOrderCount = findViewById(R.id.tvOrderCount)
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings)
        btnReports = findViewById(R.id.btnReports)
        btnSettings = findViewById(R.id.btnSettings)
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.id)
            startActivity(intent)
        }

        recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = orderAdapter
        }
    }

    private fun setupButtons() {
        btnToggleTracking.setOnClickListener {
            if (isTracking) stopTracking() else startTracking()
        }

        fabAddOrder.setOnClickListener {
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("MANUAL_ENTRY", true)
            startActivity(intent)
        }

        btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun observeOrders() {
        orderViewModel.getTodayOrders().observe(this) { orders ->
            orderAdapter.submitList(orders)
            tvOrderCount.text = "Today: ${orders.size} orders"

            var totalDist = 0.0
            var totalEarn = 0.0
            orders.forEach { order ->
                totalDist += order.distanceKm ?: 0.0
                totalEarn += order.earnings ?: 0.0
            }

            tvTotalDistance.text = String.format("%.2f km", totalDist)
            tvTotalEarnings.text = String.format("R %.2f", totalEarn)
        }
    }

    private fun startTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_START_TRACKING

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        isTracking = true
        updateTrackingUI()
    }

    private fun stopTracking() {
        val intent = Intent(this, LocationService::class.java)
        intent.action = LocationService.ACTION_STOP_TRACKING
        startService(intent)

        isTracking = false
        updateTrackingUI()
    }

    private fun updateTrackingUI() {
        if (isTracking) {
            btnToggleTracking.text = "Stop Tracking"
            btnToggleTracking.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        } else {
            btnToggleTracking.text = "Start Tracking"
            btnToggleTracking.setBackgroundColor(getColor(android.R.color.holo_green_dark))
        }
    }

    private fun checkPermissions() {
        if (isNotificationServiceEnabled()) {
            tvNotificationStatus.text = "✓ Notifications: ON"
            tvNotificationStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvNotificationStatus.text = "✗ Notifications: OFF (Tap to Enable)"
            tvNotificationStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            tvNotificationStatus.setOnClickListener {
                showNotificationAccessDialog()
            }
        }

        if (hasLocationPermission()) {
            tvLocationStatus.text = "✓ Location: ON"
            tvLocationStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvLocationStatus.text = "✗ Location: OFF (Tap to Enable)"
            tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            tvLocationStatus.setOnClickListener {
                requestLocationPermission()
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    private fun showNotificationAccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Enable Notification Access")
            .setMessage("CowaTracker needs notification access to capture Cowa-Bunga orders automatically.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            checkPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
}

class OrderAdapter(
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var orders = emptyList<Order>()

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvEarnings: TextView = view.findViewById(R.id.tvEarnings)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        holder.tvOrderNumber.text = order.orderNumber ?: "#${order.id}"
        holder.tvTime.text = dateFormat.format(Date(order.notificationTime))
        holder.tvAddress.text = order.deliveryAddress ?: order.notificationText ?: "No address"
        holder.tvDistance.text = order.getFormattedDistance()
        holder.tvEarnings.text = order.getFormattedEarnings()
        holder.tvStatus.text = order.status.uppercase()

        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount() = orders.size

    fun submitList(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}