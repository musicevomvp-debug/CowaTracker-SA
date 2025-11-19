package com.cowatracker.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.cowatracker.app.data.AppDatabase
import com.cowatracker.app.data.Order

class NotificationListener : NotificationListenerService() {

    private val TAG = "CowaTracker_NotifListener"
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val TARGET_PACKAGES = listOf(
        "za.co.loop.logistics",
        "za.co.cowabunga.loop",
        "za.co.cowabunga",
        "com.cowabunga"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName

        if (TARGET_PACKAGES.any { packageName.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "Cowa-Bunga notification detected from: $packageName")
            processNotification(sbn)
        }
    }

    private fun processNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

            val fullText = listOfNotNull(text, bigText)
                .filter { it.isNotBlank() }
                .joinToString(" | ")

            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $fullText")

            val orderNumber = extractOrderNumber(title, fullText)
            val addresses = extractAddresses(fullText)

            val order = Order(
                notificationTime = sbn.postTime,
                orderNumber = orderNumber,
                notificationTitle = title,
                notificationText = fullText,
                pickupAddress = addresses.getOrNull(0),
                deliveryAddress = addresses.getOrNull(1),
                status = "pending",
                isManualEntry = false
            )

            serviceScope.launch {
                try {
                    val database = AppDatabase.getDatabase(applicationContext)
                    val orderId = database.orderDao().insertOrder(order)
                    Log.d(TAG, "Order saved with ID: $orderId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving order: ${e.message}", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification: ${e.message}", e)
        }
    }

    private fun extractOrderNumber(title: String?, text: String?): String? {
        val combinedText = "$title $text"

        val patterns = listOf(
            """#(\d+)""",
            """Order[:\s]+#?(\d+)""",
            """Order ID[:\s]+#?(\d+)""",
            """(\d{4,})"""
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(combinedText)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        return null
    }

    private fun extractAddresses(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()

        val addresses = mutableListOf<String>()

        val addressPatterns = listOf(
            """Pickup[:\s]+([^|]+)""",
            """Pick[- ]?up[:\s]+([^|]+)""",
            """Deliver[:\s]+([^|]+)""",
            """Delivery[:\s]+([^|]+)""",
            """To[:\s]+([^|]+)""",
            """From[:\s]+([^|]+)"""
        )

        for (pattern in addressPatterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(text)
            if (match != null) {
                val address = match.groupValues[1].trim()
                if (address.isNotBlank() && address.length > 5) {
                    addresses.add(address)
                }
            }
        }

        return addresses.distinct()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener Disconnected")
    }
}