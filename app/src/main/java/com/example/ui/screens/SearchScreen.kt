package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.util.CurrencyUtils

@Composable
fun SearchScreen(
  searchResults: List<KhataEntryWithBalance>,
  filterState: FilterState,
  settings: KhataSettings,
  availableCategories: List<String>,
  onQueryChange: (String) -> Unit,
  onTypeFilterChange: (TypeFilter) -> Unit,
  onDateFilterChange: (DateFilterType) -> Unit,
  onCategoryFilterChange: (String) -> Unit,
  onTransactionClick: (Long) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Aggregate stats from search results
  val resultsIn = remember(searchResults) {
    searchResults.filter { it.transaction.type == "IN" }.sumOf { it.transaction.amount }
  }
  val resultsOut = remember(searchResults) {
    searchResults.filter { it.transaction.type == "OUT" }.sumOf { it.transaction.amount }
  }
  val resultsNet = resultsIn - resultsOut

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .testTag("search_screen")
    ) {
      // Top Search Bar Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("search_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }

        OutlinedTextField(
          value = filterState.searchQuery,
          onValueChange = onQueryChange,
          placeholder = { Text("Search person, reason, note, amount...") },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = EmeraldPrimary
            )
          },
          trailingIcon = {
            if (filterState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onQueryChange("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search"
                )
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("search_input_field"),
          shape = RoundedCornerShape(16.dp)
        )
      }

      // Filter Chips Row: Type & Categories
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          // Type filters
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.ALL,
              onClick = { onTypeFilterChange(TypeFilter.ALL) },
              label = { Text("All") }
            )
          }
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.IN,
              onClick = { onTypeFilterChange(TypeFilter.IN) },
              label = { Text("Money In") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MoneyInGreen.copy(alpha = 0.15f),
                selectedLabelColor = MoneyInGreen
              )
            )
          }
          item {
            FilterChip(
              selected = filterState.typeFilter == TypeFilter.OUT,
              onClick = { onTypeFilterChange(TypeFilter.OUT) },
              label = { Text("Money Out") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MoneyOutRose.copy(alpha = 0.15f),
                selectedLabelColor = MoneyOutRose
              )
            )
          }

          // Date filters
          item {
            FilterChip(
              selected = filterState.dateFilter == DateFilterType.THIS_MONTH,
              onClick = {
                onDateFilterChange(
                  if (filterState.dateFilter == DateFilterType.THIS_MONTH) DateFilterType.ALL else DateFilterType.THIS_MONTH
                )
              },
              label = { Text("This Month") }
            )
          }
          item {
            FilterChip(
              selected = filterState.dateFilter == DateFilterType.LAST_MONTH,
              onClick = {
                onDateFilterChange(
                  if (filterState.dateFilter == DateFilterType.LAST_MONTH) DateFilterType.ALL else DateFilterType.LAST_MONTH
                )
              },
              label = { Text("Last Month") }
            )
          }

          // Category chips
          availableCategories.take(6).forEach { cat ->
            item {
              FilterChip(
                selected = filterState.categoryFilter == cat,
                onClick = {
                  onCategoryFilterChange(
                    if (filterState.categoryFilter == cat) "All" else cat
                  )
                },
                label = { Text(cat) }
              )
            }
          }
        }
      }

      // Results Summary Banner
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${searchResults.size} results found",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            val netPrefix = if (resultsNet >= 0) "+" else ""
            Text(
              text = "Net: $netPrefix" + CurrencyUtils.formatAmount(resultsNet, settings.currencySymbol),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (resultsNet >= 0) MoneyInGreen else MoneyOutRose
            )
          }

          if (searchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "In: " + CurrencyUtils.formatAmount(resultsIn, settings.currencySymbol),
                style = MaterialTheme.typography.bodySmall,
                color = MoneyInGreen,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Out: " + CurrencyUtils.formatAmount(resultsOut, settings.currencySymbol),
                style = MaterialTheme.typography.bodySmall,
                color = MoneyOutRose,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Search Results List
      if (searchResults.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (filterState.searchQuery.isBlank()) "Type words to search" else "No matching transactions",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (filterState.searchQuery.isBlank())
                "Search by name, reason, note, amount, or date."
              else
                "Try checking for typos or removing filters.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(
            items = searchResults,
            key = { it.transaction.id }
          ) { item ->
            TransactionListItem(
              entry = item,
              currencySymbol = settings.currencySymbol,
              onClick = { onTransactionClick(item.transaction.id) }
            )
          }
        }
      }
    }
  }
}
