package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChips(
  categories: List<String>,
  selectedCategory: String,
  onCategorySelected: (String) -> Unit,
  onAddNewCategoryClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(scrollState)
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // "None" / Optional chip
    FilterChip(
      selected = selectedCategory.isBlank(),
      onClick = { onCategorySelected("") },
      label = { Text("None") },
      colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
      )
    )

    categories.forEach { category ->
      val isSelected = selectedCategory.equals(category, ignoreCase = true)
      FilterChip(
        selected = isSelected,
        onClick = {
          if (isSelected) onCategorySelected("") else onCategorySelected(category)
        },
        label = { Text(category) },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }

    // Add Custom Category Chip
    FilterChip(
      selected = false,
      onClick = onAddNewCategoryClick,
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Category",
          modifier = Modifier.size(16.dp)
        )
      },
      label = { Text("Add New") }
    )
  }
}
