package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.KhataEntryWithBalance
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfPassbookExporter {

  fun exportAndSharePdf(
    context: Context,
    reportTitle: String,
    periodLabel: String,
    entriesAsc: List<KhataEntryWithBalance>,
    openingBalance: Double,
    currencySymbol: String
  ): Result<Unit> {
    return try {
      val pdfDoc = PdfDocument()
      val pageWidth = 595 // Standard A4 width (points)
      val pageHeight = 842 // Standard A4 height (points)
      val margin = 36f

      val paintText = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(30, 41, 59)
        textSize = 9f
      }

      val paintBold = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(15, 23, 42)
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val paintTitle = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(4, 120, 87) // Deep Emerald
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val paintSub = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(100, 116, 139)
        textSize = 10f
      }

      val paintHeaderBg = Paint().apply {
        color = Color.rgb(241, 245, 249)
        style = Paint.Style.FILL
      }

      val paintLine = Paint().apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 0.8f
      }

      val paintGreen = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(5, 150, 105)
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      val paintRed = Paint().apply {
        isAntiAlias = true
        color = Color.rgb(225, 29, 72)
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }

      // Calculations for summary
      val totalIn = entriesAsc.filter { it.transaction.type == "IN" }.sumOf { it.transaction.amount }
      val totalOut = entriesAsc.filter { it.transaction.type == "OUT" }.sumOf { it.transaction.amount }
      val closingBalance = openingBalance + totalIn - totalOut

      var pageNumber = 1
      val rowsPerPage = 26
      val totalPages = maxOf(1, ((entriesAsc.size + rowsPerPage - 1) / rowsPerPage))

      for (p in 0 until totalPages) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        var y = margin + 20f

        // Draw Header on each page
        if (pageNumber == 1) {
          canvas.drawText("MY KHATA", margin, y, paintTitle)
          y += 14f
          canvas.drawText("Official Digital Passbook & Ledger Statement", margin, y, paintSub)
          y += 12f
          canvas.drawText("Period: $periodLabel", margin, y, paintBold)
          y += 18f

          // Summary Card Box
          val summaryBoxTop = y
          val summaryBoxBottom = y + 44f
          val boxPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
          }
          canvas.drawRect(margin, summaryBoxTop, pageWidth - margin, summaryBoxBottom, boxPaint)
          canvas.drawRect(margin, summaryBoxTop, pageWidth - margin, summaryBoxBottom, Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 1f
          })

          val colW = (pageWidth - margin * 2) / 4f
          val labelY = summaryBoxTop + 16f
          val valY = summaryBoxTop + 32f

          // Col 1: Opening
          canvas.drawText("Opening Balance", margin + 8f, labelY, paintSub)
          canvas.drawText(CurrencyUtils.formatAmount(openingBalance, currencySymbol), margin + 8f, valY, paintBold)

          // Col 2: Total In
          canvas.drawText("Total Money In", margin + colW + 8f, labelY, paintSub)
          canvas.drawText(CurrencyUtils.formatAmount(totalIn, currencySymbol), margin + colW + 8f, valY, paintGreen)

          // Col 3: Total Out
          canvas.drawText("Total Money Out", margin + colW * 2 + 8f, labelY, paintSub)
          canvas.drawText(CurrencyUtils.formatAmount(totalOut, currencySymbol), margin + colW * 2 + 8f, valY, paintRed)

          // Col 4: Closing
          canvas.drawText("Closing Balance", margin + colW * 3 + 8f, labelY, paintSub)
          canvas.drawText(CurrencyUtils.formatAmount(closingBalance, currencySymbol), margin + colW * 3 + 8f, valY, paintBold)

          y = summaryBoxBottom + 20f
        } else {
          canvas.drawText("MY KHATA — Digital Passbook ($periodLabel)", margin, y, paintBold)
          y += 20f
        }

        // Table Header
        val colDate = margin + 4f
        val colTime = margin + 55f
        val colDesc = margin + 105f
        val colIn = margin + 300f
        val colOut = margin + 375f
        val colBal = margin + 450f

        val tableHeaderH = 20f
        canvas.drawRect(margin, y, pageWidth - margin, y + tableHeaderH, paintHeaderBg)
        canvas.drawLine(margin, y + tableHeaderH, pageWidth - margin, y + tableHeaderH, paintLine)

        val headerTextY = y + 13f
        canvas.drawText("Date", colDate, headerTextY, paintBold)
        canvas.drawText("Time", colTime, headerTextY, paintBold)
        canvas.drawText("Particulars / Details", colDesc, headerTextY, paintBold)
        canvas.drawText("Money In", colIn, headerTextY, paintBold)
        canvas.drawText("Money Out", colOut, headerTextY, paintBold)
        canvas.drawText("Balance", colBal, headerTextY, paintBold)

        y += tableHeaderH + 4f

        val startIndex = p * rowsPerPage
        val endIndex = minOf(entriesAsc.size, startIndex + rowsPerPage)

        for (i in startIndex until endIndex) {
          val item = entriesAsc[i]
          val tx = item.transaction
          val rowY = y + 14f

          canvas.drawText(tx.date, colDate, rowY, paintText)
          canvas.drawText(tx.time, colTime, rowY, paintText)

          val partyDesc = when {
            tx.person.isNotBlank() && tx.reason.isNotBlank() -> "${tx.person} - ${tx.reason}"
            tx.person.isNotBlank() -> tx.person
            tx.reason.isNotBlank() -> tx.reason
            else -> if (tx.type == "IN") "Money In" else "Money Out"
          }
          val truncatedDesc = if (partyDesc.length > 38) partyDesc.substring(0, 35) + "..." else partyDesc
          canvas.drawText(truncatedDesc, colDesc, rowY, paintText)

          if (tx.type == "IN") {
            canvas.drawText("+ " + CurrencyUtils.formatAmount(tx.amount, currencySymbol), colIn, rowY, paintGreen)
            canvas.drawText("-", colOut, rowY, paintSub)
          } else {
            canvas.drawText("-", colIn, rowY, paintSub)
            canvas.drawText("- " + CurrencyUtils.formatAmount(tx.amount, currencySymbol), colOut, rowY, paintRed)
          }

          canvas.drawText(CurrencyUtils.formatAmount(item.runningBalance, currencySymbol), colBal, rowY, paintBold)

          y += 20f
          canvas.drawLine(margin, y, pageWidth - margin, y, paintLine)
        }

        // Footer on bottom of page
        val footerDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on $footerDate • My Khata Personal Ledger", margin, pageHeight - margin + 10f, paintSub)
        canvas.drawText("Page $pageNumber of $totalPages", pageWidth - margin - 60f, pageHeight - margin + 10f, paintSub)

        pdfDoc.finishPage(page)
        pageNumber++
      }

      val fileName = "MyKhata_Passbook_${System.currentTimeMillis()}.pdf"
      val cacheDir = File(context.cacheDir, "exports")
      if (!cacheDir.exists()) cacheDir.mkdirs()
      val pdfFile = File(cacheDir, fileName)

      val fos = FileOutputStream(pdfFile)
      pdfDoc.writeTo(fos)
      fos.flush()
      fos.close()
      pdfDoc.close()

      // Share via FileProvider
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "My Khata Passbook Statement - $periodLabel")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Share Khata PDF Passbook")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
