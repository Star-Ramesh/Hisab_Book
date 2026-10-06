package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.model.KhataEntryWithBalance
import com.example.model.ReportData
import com.example.model.ReportPeriodType
import com.example.ui.components.CategoryBreakdownView
import com.example.ui.components.DailyActivityBarChart
import com.example.ui.components.IncomeVsExpenseBar
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyOutRose
import com.example.util.CsvExporter
import com.example.util.CurrencyUtils
import com.example.util.PdfPassbookExporter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReportsScreen(
  reportData: ReportData,
  settings: KhataSettings,
  allLedgerEntriesDesc: List<KhataEntryWithBalance>,
  selectedPeriod: ReportPeriodType,
  onPeriodSelected: (ReportPeriodType) -> Unit,
  customStartDate: String,
  customEndDate: String,
  onCustomDateChange: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val periodTabs = listOf(
    ReportPeriodType.DAILY,
    ReportPeriodType.WEEKLY,
    ReportPeriodType.MONTHLY,
    ReportPeriodType.CUSTOM
  )

  // Date Pickers for Custom Range
  val cal = Calendar.getInstance()
  val startPicker = DatePickerDialog(
    context,
    { _, y, m, d ->
      val c = Calendar.getInstance().apply { set(y, m, d) }
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      onCustomDateChange(sdf.format(c.time), customEndDate)
    },
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH),
    cal.get(Calendar.DAY_OF_MONTH)
  )

  val endPicker = DatePickerDialog(
    context,
    { _, y, m, d ->
      val c = Calendar.getInstance().apply { set(y, m, d) }
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      onCustomDateChange(customStartDate, sdf.format(c.time))
    },
    cal.get(Calendar.YEAR),
    cal.get(Calendar.MONTH),
    cal.get(Calendar.DAY_OF_MONTH)
  )

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .testTag("reports_screen"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      // Header & Title
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Financial Reports",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = reportData.periodTitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // PDF statement download icon
          IconButton(
            onClick = {
              val filteredAsc = allLedgerEntriesDesc
                .filter { it.transaction.date >= customStartDate && (customEndDate.isEmpty() || it.transaction.date <= customEndDate) }
                .reversed()
              val res = PdfPassbookExporter.exportAndSharePdf(
                context = context,
                reportTitle = "${selectedPeriod.label} Statement",
                periodLabel = reportData.periodTitle,
                entriesAsc = if (filteredAsc.isNotEmpty()) filteredAsc else allLedgerEntriesDesc.reversed(),
                openingBalance = reportData.openingBalance,
                currencySymbol = settings.currencySymbol
              )
              if (res.isFailure) {
                Toast.makeText(context, "Export error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("reports_export_pdf_button")
          ) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = "Export PDF",
              tint = EmeraldPrimary
            )
          }
        }
      }

      // Period Selection Tabs
      item {
        TabRow(
          selectedTabIndex = periodTabs.indexOf(selectedPeriod),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
          periodTabs.forEach { period ->
            Tab(
              selected = selectedPeriod == period,
              onClick = { onPeriodSelected(period) },
              text = {
                Text(
                  text = period.label,
                  fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            )
          }
        }
      }

      // Custom Date Range Pickers (if Custom Selected)
      if (selectedPeriod == ReportPeriodType.CUSTOM) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { startPicker.show() },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(text = "From: " + customStartDate.ifEmpty { "Start" }, fontSize = 12.sp)
            }
            OutlinedButton(
              onClick = { endPicker.show() },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(text = "To: " + customEndDate.ifEmpty { "End" }, fontSize = 12.sp)
            }
          }
        }
      }

      // Report Summary Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "Summary (${reportData.periodTitle})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            ReportStatRow(
              label = "Opening Balance",
              amount = reportData.openingBalance,
              symbol = settings.currencySymbol
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            ReportStatRow(
              label = "Money Received (In)",
              amount = reportData.totalMoneyIn,
              symbol = settings.currencySymbol,
              color = MoneyInGreen,
              prefix = "+"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            ReportStatRow(
              label = "Money Spent (Out)",
              amount = reportData.totalMoneyOut,
              symbol = settings.currencySymbol,
              color = MoneyOutRose,
              prefix = "-"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            val netPrefix = if (reportData.netChange >= 0) "+" else ""
            val netColor = if (reportData.netChange >= 0) MoneyInGreen else MoneyOutRose
            ReportStatRow(
              label = "Net Change",
              amount = reportData.netChange,
              symbol = settings.currencySymbol,
              color = netColor,
              prefix = netPrefix
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            ReportStatRow(
              label = "Closing Balance",
              amount = reportData.closingBalance,
              symbol = settings.currencySymbol,
              isHighlight = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "${reportData.transactionCount} transactions in this period",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Income vs Expense Comparison Chart
      item {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          IncomeVsExpenseBar(
            moneyIn = reportData.totalMoneyIn,
            moneyOut = reportData.totalMoneyOut,
            currencySymbol = settings.currencySymbol
          )
        }
      }

      // Daily Activity Chart (if multiple days available)
      if (reportData.dayBreakdown.isNotEmpty()) {
        item {
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            DailyActivityBarChart(
              activities = reportData.dayBreakdown,
              currencySymbol = settings.currencySymbol
            )
          }
        }
      }

      // Largest Income & Expense Cards
      if (reportData.largestIncome > 0 || reportData.largestExpense > 0) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Largest Income
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Largest Income",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = CurrencyUtils.formatAmount(reportData.largestIncome, settings.currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MoneyInGreen
                )
              }
            }

            // Largest Expense
            Card(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Largest Expense",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = CurrencyUtils.formatAmount(reportData.largestExpense, settings.currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MoneyOutRose
                )
              }
            }
          }
        }
      }

      // Category Breakdown Progress Bars (if any used)
      if (reportData.categoryBreakdown.isNotEmpty()) {
        item {
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            CategoryBreakdownView(
              categories = reportData.categoryBreakdown,
              currencySymbol = settings.currencySymbol
            )
          }
        }
      }

      // Export Buttons Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Export Period Statements",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val res = CsvExporter.exportAndShareCsv(
                    context = context,
                    entriesAsc = allLedgerEntriesDesc.reversed(),
                    openingBalance = settings.openingBalance,
                    currencySymbol = settings.currencySymbol
                  )
                  if (res.isFailure) {
                    Toast.makeText(context, "CSV export failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV")
              }

              ElevatedButton(
                onClick = {
                  val res = PdfPassbookExporter.exportAndSharePdf(
                    context = context,
                    reportTitle = "Passbook Statement",
                    periodLabel = reportData.periodTitle,
                    entriesAsc = allLedgerEntriesDesc.reversed(),
                    openingBalance = reportData.openingBalance,
                    currencySymbol = settings.currencySymbol
                  )
                  if (res.isFailure) {
                    Toast.makeText(context, "PDF export failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.elevatedButtonColors(
                  containerColor = EmeraldPrimary,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReportStatRow(
  label: String,
  amount: Double,
  symbol: String,
  color: Color = MaterialTheme.colorScheme.onSurface,
  prefix: String = "",
  isHighlight: Boolean = false
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = if (isHighlight) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      else MaterialTheme.typography.bodyMedium,
      color = if (isHighlight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
      text = "$prefix" + CurrencyUtils.formatAmount(amount, symbol),
      style = if (isHighlight) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
      else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      color = color
    )
  }
}
