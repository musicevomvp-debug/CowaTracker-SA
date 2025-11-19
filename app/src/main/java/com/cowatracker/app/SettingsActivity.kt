package com.cowatracker.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnDeleteAllData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btnDeleteAllData = findViewById(R.id.btnDeleteAllData)

        btnDeleteAllData.setOnClickListener {
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}