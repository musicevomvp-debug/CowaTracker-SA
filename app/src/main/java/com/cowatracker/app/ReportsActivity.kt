package com.cowatracker.app

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReportsActivity : AppCompatActivity() {

    private lateinit var rgDateRange: RadioGroup
    private lateinit var tvOrderCount: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTotalEarnings: TextView
    private lateinit var btnGeneratePDF: Button
    private lateinit var btnEmailReport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        initializeViews()
        setupButtons()
    }

    private fun initializeViews() {
        rgDateRange = findViewById(R.id.rgDateRange)
        tvOrderCount = findViewById(R.id.tvOrderCount)
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings)
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF)
        btnEmailReport = findViewById(R.id.btnEmailReport)
    }

    private fun setupButtons() {
        btnGeneratePDF.setOnClickListener {
            Toast.makeText(this, "PDF generation coming soon", Toast.LENGTH_SHORT).show()
        }

        btnEmailReport.setOnClickListener {
            Toast.makeText(this, "Email feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}