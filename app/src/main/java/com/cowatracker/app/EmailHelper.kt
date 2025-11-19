package com.cowatracker.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class EmailHelper {

    companion object {
        fun sendReportEmail(context: Context, pdfFile: File, subject: String) {
            try {
                // Get URI using FileProvider
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                // Create email intent
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, "Please find attached my CowaTracker delivery report.")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Start chooser
                context.startActivity(
                    Intent.createChooser(emailIntent, "Send Report via Email")
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun shareReport(context: Context, pdfFile: File) {
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(shareIntent, "Share Report")
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}