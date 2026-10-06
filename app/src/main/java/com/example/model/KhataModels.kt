package com.example.model

import com.example.data.local.KhataTransaction

data class KhataEntryWithBalance(
  val transaction: KhataTransaction,
  val runningBalance: Double
)

enum class TransactionType(val label: String, val code: String) {
  IN("Money In", "IN"),
  OUT("Money Out", "OUT")
}

enum class DateFilterType(val label: String) {
  ALL("All"),
  TODAY("Today"),
  YESTERDAY("Yesterday"),
  THIS_WEEK("This Week"),
  THIS_MONTH("This Month"),
  LAST_MONTH("Last Month"),
  CUSTOM("Custom Range")
}

enum class TypeFilter(val label: String) {
  ALL("All"),
  IN("Money In"),
  OUT("Money Out")
}

data class FilterState(
  val searchQuery: String = "",
  val typeFilter: TypeFilter = TypeFilter.ALL,
  val dateFilter: DateFilterType = DateFilterType.ALL,
  val categoryFilter: String = "All",
  val customStartDate: String = "", // "yyyy-MM-dd"
  val customEndDate: String = "" // "yyyy-MM-dd"
)

data class SummaryStats(
  val openingBalance: Double = 0.0,
  val currentBalance: Double = 0.0,
  val totalMoneyIn: Double = 0.0,
  val totalMoneyOut: Double = 0.0,
  val netChange: Double = 0.0,
  val todayIn: Double = 0.0,
  val todayOut: Double = 0.0,
  val monthIn: Double = 0.0,
  val monthOut: Double = 0.0,
  val totalTransactions: Int = 0,
  val largestIncome: Double = 0.0,
  val largestExpense: Double = 0.0
)

enum class ReportPeriodType(val label: String) {
  DAILY("Daily"),
  WEEKLY("Weekly"),
  MONTHLY("Monthly"),
  CUSTOM("Custom Range")
}

data class DayActivity(
  val dayLabel: String,
  val date: String,
  val moneyIn: Double,
  val moneyOut: Double,
  val net: Double
)

data class CategoryBreakdown(
  val category: String,
  val amount: Double,
  val percentage: Float,
  val count: Int
)

data class ReportData(
  val periodType: ReportPeriodType,
  val periodTitle: String,
  val openingBalance: Double,
  val totalMoneyIn: Double,
  val totalMoneyOut: Double,
  val netChange: Double,
  val closingBalance: Double,
  val transactionCount: Int,
  val largestIncome: Double,
  val largestExpense: Double,
  val dayBreakdown: List<DayActivity>,
  val categoryBreakdown: List<CategoryBreakdown>
)
