package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpeningBalanceDialog(
  currentOpeningBalance: Double,
  currentUserName: String = "",
  currencySymbol: String,
  isEditingFromSettings: Boolean = false,
  onDismiss: () -> Unit = {},
  onConfirm: (Double) -> Unit = {},
  onConfirmInitial: (userName: String, openingBalance: Double) -> Unit = { _, _ -> }
) {
  var nameText by remember { mutableStateOf(currentUserName) }
  var nameError by remember { mutableStateOf(false) }

  var amountText by remember {
    mutableStateOf(
      if (currentOpeningBalance > 0) CurrencyUtils.formatAmountWithoutSymbol(currentOpeningBalance) else ""
    )
  }
  var amountError by remember { mutableStateOf(false) }

  val presetValues = listOf(5000.0, 10000.0, 25000.0, 50000.0, 100000.0)

  AlertDialog(
    onDismissRequest = {
      if (isEditingFromSettings) onDismiss()
    },
    icon = {
      Icon(
        imageVector = if (isEditingFromSettings) Icons.Default.Warning else Icons.Default.AccountBalanceWallet,
        contentDescription = null,
        tint = if (isEditingFromSettings) MaterialTheme.colorScheme.error else EmeraldPrimary
      )
    },
    title = {
      Text(
        text = if (isEditingFromSettings) "Edit Opening Balance" else "Welcome to My Khata",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (isEditingFromSettings)
            "Changing your starting balance will recalculate all past running balances and reports."
          else
            "Please enter your name and starting cash/bank money to set up your personal passbook.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Name input (Required when initial setup)
        if (!isEditingFromSettings) {
          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = nameText,
            onValueChange = {
              nameText = it
              nameError = false
            },
            label = { Text("Your Name * (Required)") },
            placeholder = { Text("e.g. Ramesh Kumar") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary)
            },
            singleLine = true,
            isError = nameError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("initial_user_name_input"),
            shape = RoundedCornerShape(12.dp)
          )

          if (nameError) {
            Text(
              text = "Name is required to personalize your khata",
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = amountText,
          onValueChange = { input ->
            val clean = input.filter { it.isDigit() || it == '.' }
            amountText = clean
            amountError = false
          },
          label = { Text(if (!isEditingFromSettings) "Starting Balance (How much money you have)" else "Starting Balance") },
          prefix = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          isError = amountError,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("opening_balance_input"),
          shape = RoundedCornerShape(12.dp)
        )

        if (amountError) {
          Text(
            text = "Please enter a valid amount (can be 0)",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Preset Chips
        Text(
          text = "Quick Presets:",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          presetValues.forEach { preset ->
            FilterChip(
              selected = amountText == CurrencyUtils.formatAmountWithoutSymbol(preset),
              onClick = {
                amountText = preset.toString()
                amountError = false
              },
              label = {
                Text(CurrencyUtils.formatAmount(preset, currencySymbol), fontSize = 11.sp)
              }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!isEditingFromSettings && nameText.trim().isBlank()) {
            nameError = true
            return@Button
          }

          val value = if (amountText.isBlank()) 0.0 else amountText.toDoubleOrNull()
          if (value != null && value >= 0) {
            if (isEditingFromSettings) {
              onConfirm(value)
            } else {
              onConfirmInitial(nameText.trim(), value)
            }
          } else {
            amountError = true
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        modifier = Modifier.testTag("save_opening_balance_button")
      ) {
        Text(if (isEditingFromSettings) "Update Starting Balance" else "Save & Start Khata")
      }
    },
    dismissButton = {
      if (isEditingFromSettings) {
        OutlinedButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}
