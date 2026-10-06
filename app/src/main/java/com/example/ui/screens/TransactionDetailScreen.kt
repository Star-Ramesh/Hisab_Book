package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.model.KhataEntryWithBalance
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInGreenLight
import com.example.ui.theme.MoneyOutRose
import com.example.ui.theme.MoneyOutRoseLight
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreen(
  entry: KhataEntryWithBalance?,
  settings: KhataSettings,
  onEditClick: (Long) -> Unit,
  onDeleteConfirmed: (Long) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (entry == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("Transaction not found")
    }
    return
  }

  val tx = entry.transaction
  val isMoneyIn = tx.type == "IN"
  val accentColor = if (isMoneyIn) MoneyInGreen else MoneyOutRose
  val badgeBg = if (isMoneyIn) MoneyInGreenLight else MoneyOutRoseLight

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("transaction_detail_screen")
    ) {
      // Top Navigation Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }

        Text(
          text = "Transaction Details",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Row {
          IconButton(
            onClick = { onEditClick(tx.id) },
            modifier = Modifier.testTag("detail_edit_button")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Transaction"
            )
          }

          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("detail_delete_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete Transaction",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Hero Amount & Type Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Icon badge
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(badgeBg),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isMoneyIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Type Tag
          Text(
            text = if (isMoneyIn) "MONEY IN (RECEIVED)" else "MONEY OUT (SPENT)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Amount
          val amountPrefix = if (isMoneyIn) "+ " else "- "
          Text(
            text = amountPrefix + CurrencyUtils.formatAmount(tx.amount, settings.currencySymbol),
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 32.sp
            ),
            color = accentColor
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Running Balance Badge
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Text(
              text = "Passbook Balance: " + CurrencyUtils.formatAmount(entry.runningBalance, settings.currencySymbol),
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Detailed Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          DetailRow(
            label = "Transaction ID",
            value = "#TXN-${tx.id}"
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

          DetailRow(
            label = if (isMoneyIn) "Received From / Source" else "Spent On / Destination",
            value = tx.person.ifBlank { "Not specified" }
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

          DetailRow(
            label = "Reason",
            value = tx.reason.ifBlank { "Not specified" }
          )

          if (tx.category.isNotBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            DetailRow(
              label = "Category",
              value = tx.category
            )
          }

          if (tx.note.isNotBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            DetailRow(
              label = "Notes / Details",
              value = tx.note
            )
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

          DetailRow(
            label = "Date & Time",
            value = "${DateUtils.formatDisplayDate(tx.date)} at ${DateUtils.formatDisplayTime(tx.time)}"
          )

          HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

          val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
          DetailRow(
            label = "Recorded On",
            value = sdf.format(Date(tx.createdAt))
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = { onEditClick(tx.id) },
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("detail_edit_action_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Edit")
        }

        Button(
          onClick = { showDeleteDialog = true },
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("detail_delete_action_button"),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Delete")
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete this transaction?") },
      text = {
        Text("This transaction will be permanently removed. All subsequent passbook balances will update automatically.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDialog = false
            onDeleteConfirmed(tx.id)
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String
) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
