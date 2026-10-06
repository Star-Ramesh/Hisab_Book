package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
  private val formatterWithDecimals = DecimalFormat("#,##,##0.00", DecimalFormatSymbols(Locale.US))
  private val formatterWhole = DecimalFormat("#,##,##0", DecimalFormatSymbols(Locale.US))

  fun formatAmount(amount: Double, symbol: String = "₹"): String {
    val formatted = if (amount % 1.0 == 0.0) {
      formatterWhole.format(amount)
    } else {
      formatterWithDecimals.format(amount)
    }
    return "$symbol$formatted"
  }

  fun formatAmountWithoutSymbol(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
      formatterWhole.format(amount)
    } else {
      formatterWithDecimals.format(amount)
    }
  }

  val availableCurrencies = listOf(
    Pair("₹", "INR - Indian Rupee"),
    Pair("$", "USD - US Dollar"),
    Pair("€", "EUR - Euro"),
    Pair("£", "GBP - British Pound"),
    Pair("¥", "JPY - Japanese Yen"),
    Pair("৳", "BDT - Bangladeshi Taka"),
    Pair("₨", "PKR - Pakistani Rupee"),
    Pair("AED", "AED - UAE Dirham"),
    Pair("C$", "CAD - Canadian Dollar"),
    Pair("A$", "AUD - Australian Dollar"),
    Pair("S$", "SGD - Singapore Dollar")
  )
}
