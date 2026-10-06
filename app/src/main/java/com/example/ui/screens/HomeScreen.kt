package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.KhataSettings
import com.example.model.KhataEntryWithBalance
import com.example.model.SummaryStats
import com.example.ui.components.BalanceCard
import com.example.ui.components.TransactionListItem
import com.example.ui.theme.EmeraldContainerLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MontserratFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily

@Composable
fun HomeScreen(
  summaryStats: SummaryStats,
  settings: KhataSettings,
  recentEntries: List<KhataEntryWithBalance>,
  onAddMoneyIn: () -> Unit,
  onAddMoneyOut: () -> Unit,
  onViewPassbook: () -> Unit,
  onSearchClick: () -> Unit,
  onTransactionClick: (Long) -> Unit,
  onUpdateUserName: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showEditNameDialog by remember { mutableStateOf(false) }
  var nameInput by remember { mutableStateOf(settings.userName) }

  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .testTag("home_screen"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      // Top Bar: User Name Greeting & Search
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .weight(1f)
              .clickable {
                nameInput = settings.userName
                showEditNameDialog = true
              }
              .testTag("user_name_profile_header")
          ) {
            // Profile Initial Avatar
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary),
              contentAlignment = Alignment.Center
            ) {
              if (settings.userName.isNotBlank()) {
                val parts = settings.userName.trim().split(" ")
                val initials = if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                else settings.userName.take(2).uppercase()

                Text(
                  text = initials,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  fontFamily = MontserratFontFamily
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Profile",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (settings.userName.isNotBlank()) "Hello, ${settings.userName} 👋" else "Welcome 👋",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = MontserratFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  ),
                  color = MaterialTheme.colorScheme.onBackground,
                  maxLines = 1,
                  modifier = Modifier.testTag("home_user_name_text")
                )
              }
              Text(
                text = if (settings.userName.isNotBlank()) "My Khata • Personal Passbook" else "Tap to set your name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onSearchClick,
            modifier = Modifier
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .testTag("home_search_button")
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Transactions",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // Balance Card
      item {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
          BalanceCard(
            summary = summaryStats,
            currencySymbol = settings.currencySymbol,
            onAddMoneyIn = onAddMoneyIn,
            onAddMoneyOut = onAddMoneyOut,
            onViewPassbook = onViewPassbook
          )
        }
      }

      // Recent Transactions Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 24.dp, bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )

          if (recentEntries.isNotEmpty()) {
            TextButton(
              onClick = onViewPassbook,
              modifier = Modifier.testTag("see_all_transactions_button")
            ) {
              Text(
                text = "See All",
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = EmeraldPrimary
              )
            }
          }
        }
      }

      // Recent Transactions List
      if (recentEntries.isEmpty()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Transactions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap + Money In to record money you received, or − Money Out for an expense.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(
          items = recentEntries.take(7),
          key = { it.transaction.id }
        ) { entry ->
          Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            TransactionListItem(
              entry = entry,
              currencySymbol = settings.currencySymbol,
              onClick = { onTransactionClick(entry.transaction.id) }
            )
          }
        }
      }
    }
  }

  // Edit Name Dialog
  if (showEditNameDialog) {
    AlertDialog(
      onDismissRequest = { showEditNameDialog = false },
      title = { Text("Edit Your Name") },
      text = {
        Column {
          Text("This name is displayed on your home dashboard and passbook:")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_user_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (nameInput.isNotBlank()) {
              onUpdateUserName(nameInput.trim())
              showEditNameDialog = false
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
}
