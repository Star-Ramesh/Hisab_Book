package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.KhataBottomBar
import com.example.ui.components.KhataNavTab
import com.example.ui.components.OpeningBalanceDialog
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.AddEditTransactionScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PassbookScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyKhataTheme
import com.example.ui.viewmodel.KhataViewModel

sealed class Screen {
  object MainTabs : Screen()
  object Search : Screen()
  data class AddTransaction(val initialType: String) : Screen()
  data class TransactionDetail(val id: Long) : Screen()
  data class EditTransaction(val id: Long) : Screen()
}

class MainActivity : ComponentActivity() {
  private val viewModel: KhataViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val settings by viewModel.settings.collectAsStateWithLifecycle()
      val isDark = when (settings.darkModePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
      }

      MyKhataTheme(darkTheme = isDark) {
        KhataApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun KhataApp(viewModel: KhataViewModel) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val summaryStats by viewModel.summaryStats.collectAsStateWithLifecycle()
  val allEntries by viewModel.allLedgerEntries.collectAsStateWithLifecycle()
  val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()
  val reportData by viewModel.reportData.collectAsStateWithLifecycle()
  val selectedReportPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()
  val customReportStartDate by viewModel.customReportStartDate.collectAsStateWithLifecycle()
  val customReportEndDate by viewModel.customReportEndDate.collectAsStateWithLifecycle()
  val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
  val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()

  var currentTab by remember { mutableStateOf(KhataNavTab.HOME) }
  var currentScreen by remember { mutableStateOf<Screen>(Screen.MainTabs) }

  // PIN Lock verification screen
  if (settings.isPinLockEnabled && !isAppUnlocked) {
    PinLockScreen(
      onUnlock = { pin ->
        val success = viewModel.unlockWithPin(pin)
        if (!success) {
          Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
        }
        success
      }
    )
    return
  }

  // Initial Onboarding Opening Balance Dialog (Asks for User Name [Required] and Starting Balance)
  if (!settings.isOpeningBalanceSet) {
    OpeningBalanceDialog(
      currentOpeningBalance = settings.openingBalance,
      currentUserName = settings.userName,
      currencySymbol = settings.currencySymbol,
      isEditingFromSettings = false,
      onDismiss = { /* Non-dismissible first run */ },
      onConfirmInitial = { name, initialBalance ->
        viewModel.setInitialUserAndBalance(name, initialBalance)
        Toast.makeText(context, "Welcome, $name! Khata ready.", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // BackHandler for sub-screens
  BackHandler(enabled = currentScreen !is Screen.MainTabs) {
    currentScreen = Screen.MainTabs
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (currentScreen is Screen.MainTabs) {
        KhataBottomBar(
          currentTab = currentTab,
          onTabSelected = { currentTab = it }
        )
      }
    },
    floatingActionButton = {
      if (currentScreen is Screen.MainTabs && (currentTab == KhataNavTab.HOME || currentTab == KhataNavTab.PASSBOOK)) {
        FloatingActionButton(
          onClick = { currentScreen = Screen.AddTransaction("IN") },
          containerColor = EmeraldPrimary,
          contentColor = androidx.compose.ui.graphics.Color.White,
          modifier = Modifier.testTag("main_fab_add")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val screen = currentScreen) {
        is Screen.MainTabs -> {
          when (currentTab) {
            KhataNavTab.HOME -> {
              HomeScreen(
                summaryStats = summaryStats,
                settings = settings,
                recentEntries = allEntries,
                onAddMoneyIn = { currentScreen = Screen.AddTransaction("IN") },
                onAddMoneyOut = { currentScreen = Screen.AddTransaction("OUT") },
                onViewPassbook = { currentTab = KhataNavTab.PASSBOOK },
                onSearchClick = { currentScreen = Screen.Search },
                onTransactionClick = { txId -> currentScreen = Screen.TransactionDetail(txId) },
                onUpdateUserName = { newName -> viewModel.updateUserName(newName) }
              )
            }
            KhataNavTab.PASSBOOK -> {
              PassbookScreen(
                entries = filteredEntries,
                settings = settings,
                filterState = filterState,
                onTypeFilterSelected = { viewModel.updateTypeFilter(it) },
                onDateFilterSelected = { viewModel.updateDateFilter(it) },
                onSearchClick = { currentScreen = Screen.Search },
                onTransactionClick = { txId -> currentScreen = Screen.TransactionDetail(txId) }
              )
            }
            KhataNavTab.REPORTS -> {
              ReportsScreen(
                reportData = reportData,
                settings = settings,
                allLedgerEntriesDesc = allEntries,
                selectedPeriod = selectedReportPeriod,
                onPeriodSelected = { viewModel.selectedReportPeriod.value = it },
                customStartDate = customReportStartDate,
                customEndDate = customReportEndDate,
                onCustomDateChange = { s, e ->
                  viewModel.customReportStartDate.value = s
                  viewModel.customReportEndDate.value = e
                }
              )
            }
            KhataNavTab.SETTINGS -> {
              SettingsScreen(
                settings = settings,
                allLedgerEntriesDesc = allEntries,
                onUpdateUserName = { newName -> viewModel.updateUserName(newName) },
                onUpdateOpeningBalance = { viewModel.setOpeningBalance(it) },
                onUpdateCurrency = { symbol, code -> viewModel.updateCurrency(symbol, code) },
                onUpdateDarkMode = { viewModel.setDarkModePreference(it) },
                onUpdatePinLock = { enabled, pin -> viewModel.setPinLock(enabled, pin) },
                onAddNewCategory = { viewModel.addCustomCategory(it) },
                onRestoreBackup = { json, callback -> viewModel.restoreFromJson(json, callback) },
                onClearAllData = { viewModel.clearAllData() }
              )
            }
          }
        }

        is Screen.Search -> {
          SearchScreen(
            searchResults = filteredEntries,
            filterState = filterState,
            settings = settings,
            availableCategories = allCategories,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onTypeFilterChange = { viewModel.updateTypeFilter(it) },
            onDateFilterChange = { viewModel.updateDateFilter(it) },
            onCategoryFilterChange = { viewModel.updateCategoryFilter(it) },
            onTransactionClick = { txId -> currentScreen = Screen.TransactionDetail(txId) },
            onNavigateBack = { currentScreen = Screen.MainTabs }
          )
        }

        is Screen.AddTransaction -> {
          AddEditTransactionScreen(
            initialType = screen.initialType,
            existingTransaction = null,
            settings = settings,
            availableCategories = allCategories,
            onSaveTransaction = { type, amt, person, reason, note, category, date, time ->
              viewModel.addTransaction(type, amt, person, reason, note, category, date, time)
              currentScreen = Screen.MainTabs
              Toast.makeText(context, if (type == "IN") "Money In saved!" else "Expense recorded!", Toast.LENGTH_SHORT).show()
            },
            onAddNewCategory = { viewModel.addCustomCategory(it) },
            onNavigateBack = { currentScreen = Screen.MainTabs }
          )
        }

        is Screen.TransactionDetail -> {
          val selectedEntry = allEntries.firstOrNull { it.transaction.id == screen.id }
          TransactionDetailScreen(
            entry = selectedEntry,
            settings = settings,
            onEditClick = { txId -> currentScreen = Screen.EditTransaction(txId) },
            onDeleteConfirmed = { txId ->
              viewModel.deleteTransaction(txId)
              currentScreen = Screen.MainTabs
              Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
            },
            onNavigateBack = { currentScreen = Screen.MainTabs }
          )
        }

        is Screen.EditTransaction -> {
          val selectedEntry = allEntries.firstOrNull { it.transaction.id == screen.id }
          AddEditTransactionScreen(
            initialType = selectedEntry?.transaction?.type ?: "IN",
            existingTransaction = selectedEntry?.transaction,
            settings = settings,
            availableCategories = allCategories,
            onSaveTransaction = { type, amt, person, reason, note, category, date, time ->
              viewModel.updateTransaction(screen.id, type, amt, person, reason, note, category, date, time)
              currentScreen = Screen.TransactionDetail(screen.id)
              Toast.makeText(context, "Transaction updated!", Toast.LENGTH_SHORT).show()
            },
            onAddNewCategory = { viewModel.addCustomCategory(it) },
            onNavigateBack = { currentScreen = Screen.TransactionDetail(screen.id) }
          )
        }
      }
    }
  }
}
