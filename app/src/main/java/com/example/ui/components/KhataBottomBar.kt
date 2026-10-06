package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldPrimary

enum class KhataNavTab(val route: String, val label: String, val icon: ImageVector) {
  HOME("home", "Home", Icons.Default.Home),
  PASSBOOK("passbook", "Passbook", Icons.Default.MenuBook),
  REPORTS("reports", "Reports", Icons.Default.Assessment),
  SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun KhataBottomBar(
  currentTab: KhataNavTab,
  onTabSelected: (KhataNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier.testTag("bottom_nav_bar"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp
  ) {
    KhataNavTab.values().forEach { tab ->
      val isSelected = currentTab == tab
      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          Icon(
            imageVector = tab.icon,
            contentDescription = tab.label
          )
        },
        label = {
          Text(
            text = tab.label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = EmeraldPrimary,
          selectedTextColor = EmeraldPrimary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_tab_${tab.route}")
      )
    }
  }
}
