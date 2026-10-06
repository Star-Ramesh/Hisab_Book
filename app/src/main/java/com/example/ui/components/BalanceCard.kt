package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SummaryStats
import com.example.ui.theme.EmeraldContainerDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryDark
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyInGreenLight
import com.example.ui.theme.MoneyOutRose
import com.example.ui.theme.MoneyOutRoseLight
import com.example.util.CurrencyUtils

@Composable
fun BalanceCard(
  summary: SummaryStats,
  currencySymbol: String,
  onAddMoneyIn: () -> Unit,
  onAddMoneyOut: () -> Unit,
  onViewPassbook: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("balance_card"),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column {
      // Top Emerald Header for Total Balance
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                EmeraldPrimary,
                EmeraldPrimaryDark
              )
            )
          )
          .padding(20.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL BALANCE",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White.copy(alpha = 0.85f),
              letterSpacing = 1.2.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Opening: " + CurrencyUtils.formatAmount(summary.openingBalance, currencySymbol),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.75f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = CurrencyUtils.formatAmount(summary.currentBalance, currencySymbol),
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 34.sp
            ),
            color = Color.White,
            modifier = Modifier.testTag("current_balance_text")
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Quick In / Out Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // + Money In Button
            ElevatedButton(
              onClick = onAddMoneyIn,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("quick_money_in_button"),
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.White,
                contentColor = MoneyInGreen
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Money In",
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "MONEY IN",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            // − Money Out Button
            ElevatedButton(
              onClick = onAddMoneyOut,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("quick_money_out_button"),
              colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.White,
                contentColor = MoneyOutRose
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Money Out",
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "MONEY OUT",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      // Middle Summary Section: Today & This Month
      Column(modifier = Modifier.padding(18.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Today Summary Column
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Today",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRowMini(
              label = "Money In",
              amount = summary.todayIn,
              currencySymbol = currencySymbol,
              color = MoneyInGreen,
              isIncome = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRowMini(
              label = "Money Out",
              amount = summary.todayOut,
              currencySymbol = currencySymbol,
              color = MoneyOutRose,
              isIncome = false
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // This Month Summary Column
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "This Month",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SummaryRowMini(
              label = "Money In",
              amount = summary.monthIn,
              currencySymbol = currencySymbol,
              color = MoneyInGreen,
              isIncome = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRowMini(
              label = "Money Out",
              amount = summary.monthOut,
              currencySymbol = currencySymbol,
              color = MoneyOutRose,
              isIncome = false
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(10.dp))

        // View Passbook Button
        OutlinedButton(
          onClick = onViewPassbook,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("view_passbook_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "View Digital Passbook",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}

@Composable
private fun SummaryRowMini(
  label: String,
  amount: Double,
  currencySymbol: String,
  color: Color,
  isIncome: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = CurrencyUtils.formatAmount(amount, currencySymbol),
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
      color = color
    )
  }
}
