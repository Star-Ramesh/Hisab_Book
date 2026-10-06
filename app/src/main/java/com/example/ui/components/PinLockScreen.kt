package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.ui.theme.EmeraldPrimary

@Composable
fun PinLockScreen(
  onUnlock: (String) -> Boolean,
  modifier: Modifier = Modifier
) {
  var enteredPin by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  fun handleKeyPress(digit: String) {
    if (enteredPin.length < 4) {
      val newPin = enteredPin + digit
      enteredPin = newPin
      isError = false
      if (newPin.length == 4) {
        val success = onUnlock(newPin)
        if (!success) {
          isError = true
          enteredPin = ""
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      isError = false
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("pin_lock_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(EmeraldPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "My Khata Locked",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isError) "Incorrect PIN. Try again." else "Enter your 4-digit security PIN",
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(32.dp))

      // 4 PIN Dots Indicator
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 4) {
          val isFilled = i < enteredPin.length
          Box(
            modifier = Modifier
              .size(16.dp)
              .clip(CircleShape)
              .background(
                if (isFilled) (if (isError) MaterialTheme.colorScheme.error else EmeraldPrimary)
                else Color.Transparent
              )
              .border(
                width = 2.dp,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                shape = CircleShape
              )
          )
        }
      }

      Spacer(modifier = Modifier.height(44.dp))

      // Numeric Keypad 1-9
      val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
      )

      keypadRows.forEach { row ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(28.dp),
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          row.forEach { key ->
            if (key.isEmpty()) {
              Spacer(modifier = Modifier.size(68.dp))
            } else if (key == "DEL") {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .clickable { handleBackspace() }
                  .testTag("pin_key_del"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Backspace,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.size(24.dp)
                )
              }
            } else {
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .clickable { handleKeyPress(key) }
                  .testTag("pin_key_$key"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = key,
                  style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }
  }
}
