package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.model.DateFilterType
import com.example.model.FilterState
import com.example.model.KhataEntryWithBalance
import com.example.model.TypeFilter
import com.example.ui.components.TransactionListItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyOutRose
import com.example.util.CsvExporter
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.PdfPassbookExporter

@Composable
fun PassbookScreen(
  entries: List<KhataEntryWithBalance>,
  settings: KhataSettings,
  filterState: FilterState,
  onTypeFilterSelected: (TypeFilter) -> Unit,
  onDateFilterSelected: (DateFilterType) -> Unit,
  onSearchClick: () -> Unit,
  onTransactionClick: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Group entries by date for bank passbook style display
  val groupedEntries = remember(entries) {
    entries.groupBy { it.transaction.date }
  }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .testTag("passbook_screen")
    ) {
      // Top Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Passbook",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${entries.size} recorded entries",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // Search Icon Button
          IconButton(
            onClick = onSearchClick,
            modifier = Modifier
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("passbook_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Export PDF Statement Button
          IconButton(
            onClick = {
              val result = PdfPassbookExporter.exportAndSharePdf(
                context = context,
                reportTitle = "Passbook Statement",
                periodLabel = filterState.dateFilter.label,
                entriesAsc = entries.reversed(),
                openingBalance = settings.openingBalance,
                currencySymbol = settings.currencySymbol
              )
              if (result.isFailure) {
                Toast.makeText(context, "Could not export PDF: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("passbook_export_pdf_button")
          ) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = "Export PDF Passbook",
              tint = EmeraldPrimary
            )
          }
        }
      }

      // Quick Filters Bar
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
      ) {
        // Type Filters (All, Money In, Money Out)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.ALL,
              onClick = { onTypeFilterSelected(TypeFilter.ALL) },
              label = { Text("All Types") }
            )
          }
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.IN,
              onClick = { onTypeFilterSelected(TypeFilter.IN) },
              label = { Text("Money In") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MoneyInGreen
                )
              }
            )
          }
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.OUT,
              onClick = { onTypeFilterSelected(TypeFilter.OUT) },
              label = { Text("Money Out") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MoneyOutRose
                )
              }
            )
          }
          item {
            FilterChip(
              selected = filterState.dateFilter == DateFilterType.TODAY,
              onClick = {
                onDateFilterSelected(
                  if (filterState.dateFilter == DateFilterType.TODAY) DateFilterType.ALL else DateFilterType.TODAY
                )
              },
              label = { Text("Today") }
            )
          }
          item {
            FilterChip(
              selected = filterState.dateFilter == DateFilterType.THIS_WEEK,
              onClick = {
                onDateFilterSelected(
                  if (filterState.dateFilter == DateFilterType.THIS_WEEK) DateFilterType.ALL else DateFilterType.THIS_WEEK
                )
              },
              label = { Text("This Week") }
            )
          }
          item {
            FilterChip(
              selected = filterState.dateFilter == DateFilterType.THIS_MONTH,
              onClick = {
                onDateFilterSelected(
                  if (filterState.dateFilter == DateFilterType.THIS_MONTH) DateFilterType.ALL else DateFilterType.THIS_MONTH
                )
              },
              label = { Text("This Month") }
            )
          }
        }
      }

      // Grouped Transactions List
      if (entries.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No Transactions Found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "No records match the current filters.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 100.dp)
        ) {
          groupedEntries.forEach { (dateStr, dateEntries) ->
            // Date Header
            item(key = "header_$dateStr") {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = DateUtils.formatDateHeaderUppercase(dateStr),
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                )
              }
            }

            // Items for this Date
            items(
              items = dateEntries,
              key = { it.transaction.id }
            ) { item ->
              Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                TransactionListItem(
                  entry = item,
                  currencySymbol = settings.currencySymbol,
                  onClick = { onTransactionClick(item.transaction.id) }
                )
              }
            }
          }

          // Starting Balance Anchor at base of passbook
          item(key = "opening_balance_anchor") {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Starting Opening Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Initial ledger starting point",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = CurrencyUtils.formatAmount(settings.openingBalance, settings.currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              }
            }
          }
        }
      }
    }
  }
}
