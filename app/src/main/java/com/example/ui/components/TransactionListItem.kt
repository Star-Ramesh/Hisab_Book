package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KhataEntryWithBalance
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInGreenLight
import com.example.ui.theme.MoneyOutRose
import com.example.ui.theme.MoneyOutRoseLight
import com.example.util.CurrencyUtils
import com.example.util.DateUtils

@Composable
fun TransactionListItem(
  entry: KhataEntryWithBalance,
  currencySymbol: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val tx = entry.transaction
  val isMoneyIn = tx.type == "IN"
  val accentColor = if (isMoneyIn) MoneyInGreen else MoneyOutRose
  val badgeBg = if (isMoneyIn) MoneyInGreenLight else MoneyOutRoseLight

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("transaction_item_${tx.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Circular Type Icon Indicator
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(badgeBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isMoneyIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
          contentDescription = if (isMoneyIn) "Money In" else "Money Out",
          tint = accentColor,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Center Details: Time, Person, Reason, Category
      Column(
        modifier = Modifier.weight(1f)
      ) {
        // Time & Category chip
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = DateUtils.formatDisplayTime(tx.time),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          if (tx.category.isNotBlank()) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = tx.category,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Person / Source / Destination
        val personLabel = when {
          tx.person.isNotBlank() -> if (isMoneyIn) "From ${tx.person}" else tx.person
          isMoneyIn -> "Money In"
          else -> "Money Out"
        }

        Text(
          text = personLabel,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Reason or note
        if (tx.reason.isNotBlank()) {
          Text(
            text = tx.reason,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Right: Amount and Running Balance
      Column(
        horizontalAlignment = Alignment.End
      ) {
        val amountPrefix = if (isMoneyIn) "+ " else "- "
        Text(
          text = amountPrefix + CurrencyUtils.formatAmount(tx.amount, currencySymbol),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = accentColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Balance: " + CurrencyUtils.formatAmount(entry.runningBalance, currencySymbol),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
