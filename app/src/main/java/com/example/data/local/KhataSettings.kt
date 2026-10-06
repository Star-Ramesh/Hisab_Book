package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class KhataSettings(
  @PrimaryKey
  val id: Int = 1,
  val userName: String = "",
  val openingBalance: Double = 0.0,
  val isOpeningBalanceSet: Boolean = false,
  val currencySymbol: String = "₹",
  val currencyCode: String = "INR",
  val darkModePreference: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
  val isPinLockEnabled: Boolean = false,
  val pinCode: String = "",
  val customCategories: String = "" // comma-separated custom categories
)
