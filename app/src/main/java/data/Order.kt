package com.cowatracker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val notificationTime: Long = System.currentTimeMillis(),
    val orderNumber: String? = null,
    val notificationTitle: String? = null,
    val notificationText: String? = null,

    val customerAddress: String? = null,
    val pickupAddress: String? = null,
    val deliveryAddress: String? = null,

    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,

    val distanceKm: Double? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,

    val earnings: Double? = null,
    val notes: String? = null,
    val photoPath: String? = null,

    val status: String = "pending",
    val isManualEntry: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun getDurationMinutes(): Long? {
        return if (startTime != null && endTime != null) {
            (endTime - startTime) / 60000
        } else null
    }

    fun getFormattedDistance(): String {
        return distanceKm?.let { String.format("%.2f km", it) } ?: "N/A"
    }

    fun getFormattedEarnings(): String {
        return earnings?.let { String.format("R %.2f", it) } ?: "N/A"
    }
}