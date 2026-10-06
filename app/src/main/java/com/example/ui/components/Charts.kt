package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryBreakdown
import com.example.model.DayActivity
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyOutRose
import com.example.util.CurrencyUtils

/**
 * Income vs Expense Comparison Bar
 */
@Composable
fun IncomeVsExpenseBar(
  moneyIn: Double,
  moneyOut: Double,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  val total = moneyIn + moneyOut
  val inRatio = if (total > 0) (moneyIn / total).toFloat() else 0.5f
  val outRatio = if (total > 0) (moneyOut / total).toFloat() else 0.5f

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Income vs Expense",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Ratio Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(14.dp)
          .clip(RoundedCornerShape(7.dp))
      ) {
        if (total > 0) {
          Box(
            modifier = Modifier
              .weight(inRatio.coerceAtLeast(0.01f))
              .fillMaxHeight()
              .background(MoneyInGreen)
          )
          Box(
            modifier = Modifier
              .weight(outRatio.coerceAtLeast(0.01f))
              .fillMaxHeight()
              .background(MoneyOutRose)
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surfaceVariant)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Legend & Amounts
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(MoneyInGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Received: " + CurrencyUtils.formatAmount(moneyIn, currencySymbol),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MoneyInGreen
          )
          if (total > 0) {
            Text(
              text = " (${(inRatio * 100).toInt()}%)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(MoneyOutRose)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Spent: " + CurrencyUtils.formatAmount(moneyOut, currencySymbol),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MoneyOutRose
          )
          if (total > 0) {
            Text(
              text = " (${(outRatio * 100).toInt()}%)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

/**
 * Daily Activity Bar Chart
 */
@Composable
fun DailyActivityBarChart(
  activities: List<DayActivity>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  if (activities.isEmpty()) return

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Daily Activity",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MoneyInGreen)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "In", style = MaterialTheme.typography.labelSmall)
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MoneyOutRose)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Out", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      val maxVal = activities.maxOfOrNull { maxOf(it.moneyIn, it.moneyOut) }?.coerceAtLeast(100.0) ?: 100.0
      val scrollState = rememberScrollState()

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState)
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
      ) {
        activities.forEach { act ->
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
          ) {
            // Dual Bars (In and Out)
            Row(
              modifier = Modifier.height(110.dp),
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Money In Bar
              val inFraction = (act.moneyIn / maxVal).toFloat().coerceIn(0.04f, 1f)
              Box(
                modifier = Modifier
                  .width(16.dp)
                  .fillMaxHeight(if (act.moneyIn > 0) inFraction else 0.02f)
                  .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .background(if (act.moneyIn > 0) MoneyInGreen else Color.LightGray.copy(alpha = 0.4f))
              )

              // Money Out Bar
              val outFraction = (act.moneyOut / maxVal).toFloat().coerceIn(0.04f, 1f)
              Box(
                modifier = Modifier
                  .width(16.dp)
                  .fillMaxHeight(if (act.moneyOut > 0) outFraction else 0.02f)
                  .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                  .background(if (act.moneyOut > 0) MoneyOutRose else Color.LightGray.copy(alpha = 0.4f))
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = act.dayLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

/**
 * Category Breakdown List & Progress Bars
 */
@Composable
fun CategoryBreakdownView(
  categories: List<CategoryBreakdown>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  if (categories.isEmpty()) return

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Expense Categories",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(12.dp))

      categories.forEach { cat ->
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = cat.category,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = CurrencyUtils.formatAmount(cat.amount, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MoneyOutRose
              )
              Text(
                text = " (${cat.percentage.toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          val progress = (cat.percentage / 100f).coerceIn(0f, 1f)
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = MoneyOutRose,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
        }
      }
    }
  }
}
