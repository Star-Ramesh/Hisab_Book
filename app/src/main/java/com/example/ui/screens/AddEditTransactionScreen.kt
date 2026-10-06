package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.data.local.KhataTransaction
import com.example.ui.components.CategoryChips
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MoneyInGreen
import com.example.ui.theme.MoneyOutRose
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddEditTransactionScreen(
  initialType: String = "IN",
  existingTransaction: KhataTransaction? = null,
  settings: KhataSettings,
  availableCategories: List<String>,
  onSaveTransaction: (
    type: String,
    amount: Double,
    person: String,
    reason: String,
    note: String,
    category: String,
    date: String,
    time: String
  ) -> Unit,
  onAddNewCategory: (String) -> Unit,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val isEditing = existingTransaction != null

  var transactionType by remember {
    mutableStateOf(existingTransaction?.type ?: initialType)
  }
  var amountText by remember {
    mutableStateOf(existingTransaction?.let { CurrencyUtils.formatAmountWithoutSymbol(it.amount) } ?: "")
  }
  var personText by remember {
    mutableStateOf(existingTransaction?.person ?: "")
  }
  var reasonText by remember {
    mutableStateOf(existingTransaction?.reason ?: "")
  }
  var noteText by remember {
    mutableStateOf(existingTransaction?.note ?: "")
  }
  var selectedCategory by remember {
    mutableStateOf(existingTransaction?.category ?: "")
  }
  var selectedDate by remember {
    mutableStateOf(existingTransaction?.date ?: DateUtils.getCurrentDateIso())
  }
  var selectedTime by remember {
    mutableStateOf(existingTransaction?.time ?: DateUtils.getCurrentTime24())
  }
  var amountError by remember { mutableStateOf(false) }

  // Custom Category Dialog State
  var showAddCategoryDialog by remember { mutableStateOf(false) }
  var newCategoryInput by remember { mutableStateOf("") }

  val isMoneyIn = transactionType == "IN"
  val primaryColor = if (isMoneyIn) MoneyInGreen else MoneyOutRose

  // Date Picker
  val calendar = Calendar.getInstance()
  val datePickerDialog = DatePickerDialog(
    context,
    { _, year, month, dayOfMonth ->
      val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      selectedDate = sdf.format(cal.time)
    },
    calendar.get(Calendar.YEAR),
    calendar.get(Calendar.MONTH),
    calendar.get(Calendar.DAY_OF_MONTH)
  )

  // Time Picker
  val timePickerDialog = TimePickerDialog(
    context,
    { _, hourOfDay, minute ->
      val formattedHour = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
      val formattedMinute = if (minute < 10) "0$minute" else "$minute"
      selectedTime = "$formattedHour:$formattedMinute"
    },
    calendar.get(Calendar.HOUR_OF_DAY),
    calendar.get(Calendar.MINUTE),
    false
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .imePadding(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("add_edit_transaction_screen")
    ) {
      // Top Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("add_edit_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = if (isEditing) "Edit Transaction" else if (isMoneyIn) "Record Money In" else "Record Money Out",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Type Selector Tabs (Money In vs Money Out)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Money In Tab
        Box(
          modifier = Modifier
            .weight(1f)
            .background(
              if (isMoneyIn) MoneyInGreen else Color.Transparent,
              RoundedCornerShape(12.dp)
            )
            .clickable { transactionType = "IN" }
            .padding(vertical = 12.dp)
            .testTag("type_tab_in"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "+ MONEY IN",
            fontWeight = FontWeight.Bold,
            color = if (isMoneyIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Money Out Tab
        Box(
          modifier = Modifier
            .weight(1f)
            .background(
              if (!isMoneyIn) MoneyOutRose else Color.Transparent,
              RoundedCornerShape(12.dp)
            )
            .clickable { transactionType = "OUT" }
            .padding(vertical = 12.dp)
            .testTag("type_tab_out"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "− MONEY OUT",
            fontWeight = FontWeight.Bold,
            color = if (!isMoneyIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Amount Input (Required)
      OutlinedTextField(
        value = amountText,
        onValueChange = { input ->
          val clean = input.filter { it.isDigit() || it == '.' }
          amountText = clean
          amountError = false
        },
        label = { Text("Amount *") },
        prefix = {
          Text(
            text = settings.currencySymbol + " ",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primaryColor
          )
        },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = amountError,
        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("amount_input_field"),
        shape = RoundedCornerShape(14.dp)
      )

      if (amountError) {
        Text(
          text = "Please enter a valid amount greater than 0",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 8.dp, top = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Person / Source / Destination (Free Text)
      val personLabel = if (isMoneyIn) "From / Received From" else "To / Spent On"
      val personPlaceholder = if (isMoneyIn) "e.g. Rahul, Office, Mom, Client" else "e.g. Rahul, Restaurant, Amazon, Grocery"

      OutlinedTextField(
        value = personText,
        onValueChange = { personText = it },
        label = { Text(personLabel) },
        placeholder = { Text(personPlaceholder) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("person_input_field"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Reason (Free Text)
      val reasonLabel = if (isMoneyIn) "Why did you receive it?" else "Why did you spend it?"
      val reasonPlaceholder = if (isMoneyIn) "e.g. Returned yesterday's money, Salary" else "e.g. Bought groceries, Dinner, Travel"

      OutlinedTextField(
        value = reasonText,
        onValueChange = { reasonText = it },
        label = { Text(reasonLabel) },
        placeholder = { Text(reasonPlaceholder) },
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reason_input_field"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Date & Time Pickers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Date Button
        OutlinedButton(
          onClick = { datePickerDialog.show() },
          modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .testTag("date_picker_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = DateUtils.formatDisplayDate(selectedDate),
            fontSize = 13.sp,
            maxLines = 1
          )
        }

        // Time Button
        OutlinedButton(
          onClick = { timePickerDialog.show() },
          modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .testTag("time_picker_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = DateUtils.formatDisplayTime(selectedTime),
            fontSize = 13.sp,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Chips (Optional)
      Text(
        text = "Category (Optional)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      CategoryChips(
        categories = availableCategories,
        selectedCategory = selectedCategory,
        onCategorySelected = { selectedCategory = it },
        onAddNewCategoryClick = { showAddCategoryDialog = true }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Note (Optional Free Text)
      OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        label = { Text("Note / Additional Details (Optional)") },
        placeholder = { Text("e.g. Paid through UPI, Will receive rest later") },
        minLines = 2,
        maxLines = 4,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_input_field"),
        shape = RoundedCornerShape(14.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Submit Button
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull()
          if (amt == null || amt <= 0) {
            amountError = true
            Toast.makeText(context, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
          } else {
            onSaveTransaction(
              transactionType,
              amt,
              personText,
              reasonText,
              noteText,
              selectedCategory,
              selectedDate,
              selectedTime
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_transaction_button"),
        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
        shape = RoundedCornerShape(16.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isEditing) "Update Transaction" else if (isMoneyIn) "Save Money In" else "Save Expense",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  // Add Custom Category Dialog
  if (showAddCategoryDialog) {
    AlertDialog(
      onDismissRequest = {
        showAddCategoryDialog = false
        newCategoryInput = ""
      },
      title = { Text("Add Custom Category") },
      text = {
        OutlinedTextField(
          value = newCategoryInput,
          onValueChange = { newCategoryInput = it },
          label = { Text("Category Name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("new_category_input")
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newCategoryInput.isNotBlank()) {
              onAddNewCategory(newCategoryInput.trim())
              selectedCategory = newCategoryInput.trim()
              showAddCategoryDialog = false
              newCategoryInput = ""
            }
          }
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            showAddCategoryDialog = false
            newCategoryInput = ""
          }
        ) {
          Text("Cancel")
        }
      }
    )
  }
}
