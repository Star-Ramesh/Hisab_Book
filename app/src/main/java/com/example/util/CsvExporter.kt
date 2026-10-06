package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.KhataEntryWithBalance
import java.io.File
import java.io.FileWriter

object CsvExporter {

  fun exportAndShareCsv(
    context: Context,
    entriesAsc: List<KhataEntryWithBalance>,
    openingBalance: Double,
    currencySymbol: String
  ): Result<Unit> {
    return try {
      val fileName = "MyKhata_Report_${System.currentTimeMillis()}.csv"
      val cacheDir = File(context.cacheDir, "exports")
      if (!cacheDir.exists()) cacheDir.mkdirs()

      val file = File(cacheDir, fileName)
      val writer = FileWriter(file)

      // CSV Header
      writer.append("ID,Date,Time,Type,Amount ($currencySymbol),Person / Party,Reason,Category,Note,Running Balance ($currencySymbol)\n")

      // Add Opening Balance Row
      writer.append("0,-,-,Opening Balance,$openingBalance,-,-,-,-,$openingBalance\n")

      // Add transaction entries
      entriesAsc.forEach { item ->
        val tx = item.transaction
        val typeLabel = if (tx.type == "IN") "Money In" else "Money Out"
        val escape = { str: String ->
          val clean = str.replace("\"", "\"\"")
          "\"$clean\""
        }

        writer.append("${tx.id},")
        writer.append("${tx.date},")
        writer.append("${tx.time},")
        writer.append("${escape(typeLabel)},")
        writer.append("${tx.amount},")
        writer.append("${escape(tx.person)},")
        writer.append("${escape(tx.reason)},")
        writer.append("${escape(tx.category)},")
        writer.append("${escape(tx.note)},")
        writer.append("${item.runningBalance}\n")
      }

      writer.flush()
      writer.close()

      // Share intent via FileProvider
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "My Khata Transaction Export")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Share Khata CSV")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
