package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.data.local.KhataTransaction
import com.example.model.KhataEntryWithBalance
import com.example.ui.components.DeveloperInfoCard
import com.example.ui.components.OpeningBalanceDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.util.BackupRestoreManager
import com.example.util.CsvExporter
import com.example.util.CurrencyUtils
import com.example.util.PdfPassbookExporter

@Composable
fun SettingsScreen(
  settings: KhataSettings,
  allLedgerEntriesDesc: List<KhataEntryWithBalance>,
  onUpdateUserName: (String) -> Unit = {},
  onUpdateOpeningBalance: (Double) -> Unit,
  onUpdateCurrency: (symbol: String, code: String) -> Unit,
  onUpdateDarkMode: (String) -> Unit,
  onUpdatePinLock: (enabled: Boolean, pin: String) -> Unit,
  onAddNewCategory: (String) -> Unit,
  onRestoreBackup: (String, (Boolean, String) -> Unit) -> Unit,
  onClearAllData: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  var showEditNameDialog by remember { mutableStateOf(false) }
  var userNameInput by remember { mutableStateOf(settings.userName) }
  var showOpeningBalanceDialog by remember { mutableStateOf(false) }
  var showCurrencyDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showPinDialog by remember { mutableStateOf(false) }
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var showRestoreDialog by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  // PIN Setup State
  var pinCodeInput by remember { mutableStateOf("") }
  var newCategoryText by remember { mutableStateOf("") }
  var restoreJsonInput by remember { mutableStateOf("") }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .testTag("settings_screen"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      item {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
          Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
          )
          Text(
            text = "Preferences, security & data management",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Financial & Ledger Preferences
      item {
        SettingsSectionHeader(title = "Financial Preferences")
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column {
            SettingsItemRow(
              icon = Icons.Default.Person,
              title = "Your Name",
              subtitle = if (settings.userName.isNotBlank()) settings.userName else "Not set (Tap to set)",
              onClick = {
                userNameInput = settings.userName
                showEditNameDialog = true
              },
              testTag = "settings_user_name_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.AccountBalanceWallet,
              title = "Opening / Starting Balance",
              subtitle = CurrencyUtils.formatAmount(settings.openingBalance, settings.currencySymbol),
              onClick = { showOpeningBalanceDialog = true },
              testTag = "settings_opening_balance_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.AttachMoney,
              title = "Currency",
              subtitle = "${settings.currencySymbol} (${settings.currencyCode})",
              onClick = { showCurrencyDialog = true },
              testTag = "settings_currency_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.Category,
              title = "Custom Categories",
              subtitle = "Add custom spending / earning categories",
              onClick = { showAddCategoryDialog = true },
              testTag = "settings_custom_categories_item"
            )
          }
        }
      }

      // App Security & Theme
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "App & Security")
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column {
            // PIN Lock Switch Row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Default.Lock,
                  contentDescription = null,
                  tint = EmeraldPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    text = "App PIN Lock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = if (settings.isPinLockEnabled) "PIN protection is active" else "Protect passbook with 4-digit PIN",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = settings.isPinLockEnabled,
                onCheckedChange = { isChecked ->
                  if (isChecked) {
                    pinCodeInput = ""
                    showPinDialog = true
                  } else {
                    onUpdatePinLock(false, "")
                    Toast.makeText(context, "PIN Lock disabled", Toast.LENGTH_SHORT).show()
                  }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary),
                modifier = Modifier.testTag("pin_lock_switch")
              )
            }

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.Brightness4,
              title = "Theme",
              subtitle = when (settings.darkModePreference) {
                "DARK" -> "Dark Mode"
                "LIGHT" -> "Light Mode"
                else -> "System Default"
              },
              onClick = { showThemeDialog = true },
              testTag = "settings_theme_item"
            )
          }
        }
      }

      // Backup & Exports
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "Data & Backups")
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column {
            SettingsItemRow(
              icon = Icons.Default.PictureAsPdf,
              title = "Export Passbook as PDF",
              subtitle = "Printable statement with running balances",
              onClick = {
                val res = PdfPassbookExporter.exportAndSharePdf(
                  context = context,
                  reportTitle = "Digital Passbook Statement",
                  periodLabel = "Complete Ledger",
                  entriesAsc = allLedgerEntriesDesc.reversed(),
                  openingBalance = settings.openingBalance,
                  currencySymbol = settings.currencySymbol
                )
                if (res.isFailure) {
                  Toast.makeText(context, "Export error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
              },
              testTag = "settings_export_pdf_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.TableChart,
              title = "Export CSV Spreadsheet",
              subtitle = "Raw transaction data for Excel/Google Sheets",
              onClick = {
                val res = CsvExporter.exportAndShareCsv(
                  context = context,
                  entriesAsc = allLedgerEntriesDesc.reversed(),
                  openingBalance = settings.openingBalance,
                  currencySymbol = settings.currencySymbol
                )
                if (res.isFailure) {
                  Toast.makeText(context, "Export error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
              },
              testTag = "settings_export_csv_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.Backup,
              title = "Backup to JSON File",
              subtitle = "Save complete encrypted ledger file",
              onClick = {
                val res = BackupRestoreManager.exportBackupToJson(
                  context = context,
                  settings = settings,
                  transactions = allLedgerEntriesDesc.map { it.transaction }.reversed()
                )
                if (res.isFailure) {
                  Toast.makeText(context, "Backup failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
              },
              testTag = "settings_backup_json_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.Restore,
              title = "Restore from JSON Backup",
              subtitle = "Import previously exported ledger backup",
              onClick = {
                restoreJsonInput = ""
                showRestoreDialog = true
              },
              testTag = "settings_restore_json_item"
            )
          }
        }
      }

      // Developer & Contact Section
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "Developer & Contact")
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          DeveloperInfoCard()
        }
      }

      // Danger Zone & About
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "General")
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column {
            SettingsItemRow(
              icon = Icons.Default.Info,
              title = "About My Khata",
              subtitle = "Version 1.0 • Offline Personal Passbook",
              onClick = { showAboutDialog = true },
              testTag = "settings_about_item"
            )

            HorizontalDivider()

            SettingsItemRow(
              icon = Icons.Default.DeleteForever,
              title = "Reset All Khata Data",
              subtitle = "Delete all transactions and reset balance",
              onClick = { showClearConfirmDialog = true },
              iconTint = MaterialTheme.colorScheme.error,
              testTag = "settings_reset_item"
            )
          }
        }
      }
    }
  }

  // Opening Balance Dialog
  if (showOpeningBalanceDialog) {
    OpeningBalanceDialog(
      currentOpeningBalance = settings.openingBalance,
      currencySymbol = settings.currencySymbol,
      isEditingFromSettings = true,
      onDismiss = { showOpeningBalanceDialog = false },
      onConfirm = { newBal ->
        onUpdateOpeningBalance(newBal)
        showOpeningBalanceDialog = false
        Toast.makeText(context, "Opening balance updated", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // Currency Selection Dialog
  if (showCurrencyDialog) {
    AlertDialog(
      onDismissRequest = { showCurrencyDialog = false },
      title = { Text("Select Currency") },
      text = {
        Column {
          CurrencyUtils.availableCurrencies.forEach { (symbol, name) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val code = name.take(3)
                  onUpdateCurrency(symbol, code)
                  showCurrencyDialog = false
                  Toast.makeText(context, "Currency set to $symbol", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.currencySymbol == symbol,
                onClick = null
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "$symbol - $name",
                style = MaterialTheme.typography.bodyMedium
              )
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        OutlinedButton(onClick = { showCurrencyDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Theme Dialog
  if (showThemeDialog) {
    AlertDialog(
      onDismissRequest = { showThemeDialog = false },
      title = { Text("Select Theme") },
      text = {
        Column {
          listOf(
            Pair("SYSTEM", "System Default"),
            Pair("LIGHT", "Light Mode"),
            Pair("DARK", "Dark Mode")
          ).forEach { (key, label) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onUpdateDarkMode(key)
                  showThemeDialog = false
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = settings.darkModePreference == key,
                onClick = null
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        OutlinedButton(onClick = { showThemeDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // PIN Setup Dialog
  if (showPinDialog) {
    AlertDialog(
      onDismissRequest = { showPinDialog = false },
      title = { Text("Set 4-Digit Security PIN") },
      text = {
        Column {
          Text("Enter a 4-digit PIN to lock your personal khata records:")
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = pinCodeInput,
            onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinCodeInput = it },
            label = { Text("4-Digit PIN") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pin_setup_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (pinCodeInput.length == 4) {
              onUpdatePinLock(true, pinCodeInput)
              showPinDialog = false
              Toast.makeText(context, "PIN Lock enabled", Toast.LENGTH_SHORT).show()
            } else {
              Toast.makeText(context, "Please enter exactly 4 digits", Toast.LENGTH_SHORT).show()
            }
          }
        ) {
          Text("Set PIN")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showPinDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add Custom Category Dialog
  if (showAddCategoryDialog) {
    AlertDialog(
      onDismissRequest = { showAddCategoryDialog = false },
      title = { Text("Add Custom Category") },
      text = {
        OutlinedTextField(
          value = newCategoryText,
          onValueChange = { newCategoryText = it },
          label = { Text("Category Name") },
          placeholder = { Text("e.g. Gym, Pet Care, Investments") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newCategoryText.isNotBlank()) {
              onAddNewCategory(newCategoryText.trim())
              newCategoryText = ""
              showAddCategoryDialog = false
              Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
            }
          }
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddCategoryDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Restore from JSON Dialog
  if (showRestoreDialog) {
    AlertDialog(
      onDismissRequest = { showRestoreDialog = false },
      title = { Text("Restore Ledger from JSON") },
      text = {
        Column {
          Text("Paste the JSON backup text below to restore all transactions and settings:")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = restoreJsonInput,
            onValueChange = { restoreJsonInput = it },
            placeholder = { Text("Paste JSON content here...") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (restoreJsonInput.isNotBlank()) {
              onRestoreBackup(restoreJsonInput) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                if (success) showRestoreDialog = false
              }
            }
          }
        ) {
          Text("Restore Now")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showRestoreDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Clear Confirmation Dialog
  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = { Text("Reset All Data?") },
      text = {
        Text("This will permanently delete all recorded transactions and reset your opening balance to 0. This action cannot be undone.")
      },
      confirmButton = {
        Button(
          onClick = {
            onClearAllData()
            showClearConfirmDialog = false
            Toast.makeText(context, "All data has been reset", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Reset Everything")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Name Dialog
  if (showEditNameDialog) {
    AlertDialog(
      onDismissRequest = { showEditNameDialog = false },
      title = { Text("Edit Your Name") },
      text = {
        Column {
          Text("Enter the name you want displayed on your home dashboard and passbook:")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = userNameInput,
            onValueChange = { userNameInput = it },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (userNameInput.isNotBlank()) {
              onUpdateUserName(userNameInput.trim())
              showEditNameDialog = false
              Toast.makeText(context, "Name updated to ${userNameInput.trim()}", Toast.LENGTH_SHORT).show()
            }
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showEditNameDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // About Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = { Text("About My Khata") },
      text = {
        Column {
          Text(
            text = "My Khata is a simple, private personal money tracker and digital passbook application.",
            style = MaterialTheme.typography.bodyMedium
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "• 100% Offline & Private\n• One Unified Ledger Balance\n• Dynamic Running Balance Calculation\n• Powerful Text Search\n• PDF & CSV Passbook Export",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))
          DeveloperInfoCard()
        }
      },
      confirmButton = {
        Button(onClick = { showAboutDialog = false }) {
          Text("Got It")
        }
      }
    )
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
  )
}

@Composable
private fun SettingsItemRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  iconTint: androidx.compose.ui.graphics.Color = EmeraldPrimary,
  testTag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(14.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.outline
    )
  }
}
