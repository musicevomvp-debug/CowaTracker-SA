package com.cowatracker.app

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun generateReport(
        orders: List<Order>,
        startDate: Date,
        endDate: Date,
        periodName: String
    ): File? {
        try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint()

            var yPos = 50f

            // Title
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("CowaTracker Report", 50f, yPos, paint)
            yPos += 40f

            // Period
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Period: $periodName", 50f, yPos, paint)
            yPos += 25f
            canvas.drawText("${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}", 50f, yPos, paint)
            yPos += 40f

            // Summary
            val totalOrders = orders.size
            val totalDistance = orders.sumOf { it.distanceKm ?: 0.0 }
            val totalEarnings = orders.sumOf { it.earnings ?: 0.0 }

            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("SUMMARY", 50f, yPos, paint)
            yPos += 30f

            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Total Orders: $totalOrders", 50f, yPos, paint)
            yPos += 25f
            canvas.drawText("Total Distance: ${String.format("%.2f", totalDistance)} km", 50f, yPos, paint)
            yPos += 25f
            canvas.drawText("Total Earnings: R ${String.format("%.2f", totalEarnings)}", 50f, yPos, paint)
            yPos += 40f

            // Orders list
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("ORDER DETAILS", 50f, yPos, paint)
            yPos += 30f

            paint.textSize = 10f
            paint.isFakeBoldText = false

            // Header
            canvas.drawText("Date", 50f, yPos, paint)
            canvas.drawText("Time", 120f, yPos, paint)
            canvas.drawText("Order #", 180f, yPos, paint)
            canvas.drawText("Distance", 280f, yPos, paint)
            canvas.drawText("Earnings", 360f, yPos, paint)
            canvas.drawText("Status", 450f, yPos, paint)
            yPos += 20f

            // Draw line
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 15f

            // Orders
            for (order in orders) {
                if (yPos > 780) {
                    // New page needed
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(pageInfo)
                    yPos = 50f
                }

                val date = Date(order.notificationTime)
                canvas.drawText(dateFormat.format(date), 50f, yPos, paint)
                canvas.drawText(timeFormat.format(date), 120f, yPos, paint)
                canvas.drawText(order.orderNumber ?: "N/A", 180f, yPos, paint)
                canvas.drawText("${String.format("%.1f", order.distanceKm ?: 0.0)} km", 280f, yPos, paint)
                canvas.drawText("R ${String.format("%.2f", order.earnings ?: 0.0)}", 360f, yPos, paint)
                canvas.drawText(order.status, 450f, yPos, paint)
                yPos += 20f
            }

            pdfDocument.finishPage(page)

            // Save to file
            val fileName = "CowaTracker_${periodName}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            return file

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}