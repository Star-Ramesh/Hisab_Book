package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KhataDatabase
import com.example.data.local.KhataSettings
import com.example.data.local.KhataTransaction
import com.example.data.repository.KhataRepository
import com.example.model.CategoryBreakdown
import com.example.model.DateFilterType
import com.example.model.DayActivity
import com.example.model.FilterState
import com.example.model.KhataEntryWithBalance
import com.example.model.ReportData
import com.example.model.ReportPeriodType
import com.example.model.SummaryStats
import com.example.model.TypeFilter
import com.example.util.BackupRestoreManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class KhataViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: KhataRepository

  init {
    val db = KhataDatabase.getDatabase(application)
    repository = KhataRepository(db.khataDao())
  }

  val settings: StateFlow<KhataSettings> = repository.settingsFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, KhataSettings())

  val summaryStats: StateFlow<SummaryStats> = repository.summaryStats
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

  val allLedgerEntries: StateFlow<List<KhataEntryWithBalance>> = repository.ledgerEntriesDesc
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val filterState = MutableStateFlow(FilterState())

  val isAppUnlocked = MutableStateFlow(false)

  // Report Period Selection
  val selectedReportPeriod = MutableStateFlow(ReportPeriodType.MONTHLY)
  val customReportStartDate = MutableStateFlow(DateUtils.getStartOfMonthIso())
  val customReportEndDate = MutableStateFlow(DateUtils.getCurrentDateIso())

  // Categories list (defaults + custom)
  val defaultInCategories = listOf("Salary", "Business", "Freelance", "Gift", "Refund", "Borrowed", "Other")
  val defaultOutCategories = listOf("Food", "Travel", "Shopping", "Bills", "Medicine", "Education", "Entertainment", "Rent", "Recharge", "Other")

  val allCategories: StateFlow<List<String>> = settings.combine(filterState) { set, _ ->
    val customList = if (set.customCategories.isNotBlank()) {
      set.customCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else emptyList()
    val combined = (defaultInCategories + defaultOutCategories + customList).distinct()
    combined
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultInCategories + defaultOutCategories)

  /**
   * Filtered & Searched Ledger Entries
   */
  val filteredEntries: StateFlow<List<KhataEntryWithBalance>> = combine(
    allLedgerEntries,
    filterState
  ) { entries, filter ->
    val query = filter.searchQuery.trim().lowercase(Locale.getDefault())

    entries.filter { item ->
      val tx = item.transaction

      // 1. Text Search matching across all relevant fields
      val matchesSearch = if (query.isEmpty()) {
        true
      } else {
        tx.person.lowercase(Locale.getDefault()).contains(query) ||
            tx.reason.lowercase(Locale.getDefault()).contains(query) ||
            tx.note.lowercase(Locale.getDefault()).contains(query) ||
            tx.category.lowercase(Locale.getDefault()).contains(query) ||
            tx.id.toString().contains(query) ||
            tx.amount.toString().contains(query) ||
            tx.date.contains(query) ||
            tx.time.contains(query)
      }

      // 2. Type Filter
      val matchesType = when (filter.typeFilter) {
        TypeFilter.ALL -> true
        TypeFilter.IN -> tx.type == "IN"
        TypeFilter.OUT -> tx.type == "OUT"
      }

      // 3. Category Filter
      val matchesCategory = if (filter.categoryFilter == "All") {
        true
      } else {
        tx.category.equals(filter.categoryFilter, ignoreCase = true)
      }

      // 4. Date Filter
      val matchesDate = when (filter.dateFilter) {
        DateFilterType.ALL -> true
        DateFilterType.TODAY -> tx.date == DateUtils.getCurrentDateIso()
        DateFilterType.YESTERDAY -> tx.date == DateUtils.getYesterdayDateIso()
        DateFilterType.THIS_WEEK -> tx.date >= DateUtils.getStartOfWeekIso()
        DateFilterType.THIS_MONTH -> tx.date >= DateUtils.getStartOfMonthIso()
        DateFilterType.LAST_MONTH -> {
          tx.date >= DateUtils.getStartOfLastMonthIso() && tx.date <= DateUtils.getEndOfLastMonthIso()
        }
        DateFilterType.CUSTOM -> {
          val start = filter.customStartDate
          val end = filter.customEndDate
          (start.isEmpty() || tx.date >= start) && (end.isEmpty() || tx.date <= end)
        }
      }

      matchesSearch && matchesType && matchesCategory && matchesDate
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  /**
   * Report Data Computation
   */
  val reportData: StateFlow<ReportData> = combine(
    repository.allTransactionsAsc,
    settings,
    selectedReportPeriod,
    customReportStartDate,
    customReportEndDate
  ) { allAsc, currentSettings, period, customStart, customEnd ->
    computeReportData(allAsc, currentSettings, period, customStart, customEnd)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createEmptyReport())

  init {
    viewModelScope.launch {
      settings.collect { set ->
        if (!set.isPinLockEnabled) {
          isAppUnlocked.value = true
        }
      }
    }
  }

  fun unlockWithPin(pin: String): Boolean {
    if (settings.value.pinCode == pin) {
      isAppUnlocked.value = true
      return true
    }
    return false
  }

  fun updateSearchQuery(query: String) {
    filterState.value = filterState.value.copy(searchQuery = query)
  }

  fun updateTypeFilter(typeFilter: TypeFilter) {
    filterState.value = filterState.value.copy(typeFilter = typeFilter)
  }

  fun updateDateFilter(dateFilter: DateFilterType) {
    filterState.value = filterState.value.copy(dateFilter = dateFilter)
  }

  fun updateCategoryFilter(category: String) {
    filterState.value = filterState.value.copy(categoryFilter = category)
  }

  fun updateCustomDateFilter(start: String, end: String) {
    filterState.value = filterState.value.copy(
      dateFilter = DateFilterType.CUSTOM,
      customStartDate = start,
      customEndDate = end
    )
  }

  fun resetFilters() {
    filterState.value = FilterState()
  }

  fun addTransaction(
    type: String,
    amount: Double,
    person: String,
    reason: String,
    note: String,
    category: String,
    date: String,
    time: String
  ) {
    viewModelScope.launch {
      val epochMillis = DateUtils.computeEpochMillis(date, time)
      val tx = KhataTransaction(
        type = type,
        amount = amount,
        person = person.trim(),
        reason = reason.trim(),
        note = note.trim(),
        category = category.trim(),
        date = date,
        time = time,
        timestamp = epochMillis,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      repository.insertTransaction(tx)
    }
  }

  fun updateTransaction(
    id: Long,
    type: String,
    amount: Double,
    person: String,
    reason: String,
    note: String,
    category: String,
    date: String,
    time: String
  ) {
    viewModelScope.launch {
      val existing = repository.getTransactionByIdSync(id) ?: return@launch
      val epochMillis = DateUtils.computeEpochMillis(date, time)
      val updated = existing.copy(
        type = type,
        amount = amount,
        person = person.trim(),
        reason = reason.trim(),
        note = note.trim(),
        category = category.trim(),
        date = date,
        time = time,
        timestamp = epochMillis,
        updatedAt = System.currentTimeMillis()
      )
      repository.updateTransaction(updated)
    }
  }

  fun deleteTransaction(id: Long) {
    viewModelScope.launch {
      repository.deleteTransaction(id)
    }
  }

  fun updateUserName(name: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.updateSettings(
        current.copy(userName = name.trim())
      )
    }
  }

  fun setInitialUserAndBalance(userName: String, initialBalance: Double) {
    viewModelScope.launch {
      val current = settings.value
      repository.updateSettings(
        current.copy(
          userName = userName.trim(),
          openingBalance = initialBalance,
          isOpeningBalanceSet = true
        )
      )
    }
  }

  fun setOpeningBalance(amount: Double) {
    viewModelScope.launch {
      repository.updateOpeningBalance(amount)
    }
  }

  fun updateCurrency(symbol: String, code: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.updateSettings(
        current.copy(
          currencySymbol = symbol,
          currencyCode = code
        )
      )
    }
  }

  fun setDarkModePreference(mode: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.updateSettings(
        current.copy(darkModePreference = mode)
      )
    }
  }

  fun setPinLock(enabled: Boolean, pin: String) {
    viewModelScope.launch {
      val current = settings.value
      repository.updateSettings(
        current.copy(
          isPinLockEnabled = enabled,
          pinCode = pin
        )
      )
      if (!enabled) {
        isAppUnlocked.value = true
      }
    }
  }

  fun addCustomCategory(newCategory: String) {
    val clean = newCategory.trim()
    if (clean.isBlank()) return
    viewModelScope.launch {
      val current = settings.value
      val existing = current.customCategories.split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toMutableList()
      if (!existing.contains(clean)) {
        existing.add(clean)
        val joined = existing.joinToString(",")
        repository.updateSettings(current.copy(customCategories = joined))
      }
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      repository.clearAllData()
    }
  }

  fun restoreFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val parsed = BackupRestoreManager.parseBackupJson(jsonString)
      parsed.onSuccess { data ->
        repository.restoreData(data.transactions, data.settings)
        onResult(true, "Successfully restored ${data.transactions.size} transactions.")
      }.onFailure { err ->
        onResult(false, "Restore failed: ${err.localizedMessage ?: "Invalid JSON backup"}")
      }
    }
  }

  private fun createEmptyReport(): ReportData {
    return ReportData(
      periodType = ReportPeriodType.MONTHLY,
      periodTitle = "",
      openingBalance = 0.0,
      totalMoneyIn = 0.0,
      totalMoneyOut = 0.0,
      netChange = 0.0,
      closingBalance = 0.0,
      transactionCount = 0,
      largestIncome = 0.0,
      largestExpense = 0.0,
      dayBreakdown = emptyList(),
      categoryBreakdown = emptyList()
    )
  }

  private fun computeReportData(
    allAsc: List<KhataTransaction>,
    settings: KhataSettings,
    period: ReportPeriodType,
    customStart: String,
    customEnd: String
  ): ReportData {
    val todayIso = DateUtils.getCurrentDateIso()
    val (startDate, endDate, title) = when (period) {
      ReportPeriodType.DAILY -> {
        val displayDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        Triple(todayIso, todayIso, displayDate)
      }
      ReportPeriodType.WEEKLY -> {
        val startOfWeek = DateUtils.getStartOfWeekIso()
        Triple(startOfWeek, todayIso, "This Week")
      }
      ReportPeriodType.MONTHLY -> {
        val startOfMonth = DateUtils.getStartOfMonthIso()
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        Triple(startOfMonth, todayIso, monthName)
      }
      ReportPeriodType.CUSTOM -> {
        val s = if (customStart.isNotBlank()) customStart else "All time"
        val e = if (customEnd.isNotBlank()) customEnd else todayIso
        Triple(customStart, customEnd, "$s to $e")
      }
    }

    // Transactions before startDate determine opening balance for the period
    var opening = settings.openingBalance
    val periodTransactions = mutableListOf<KhataTransaction>()

    allAsc.forEach { tx ->
      if (startDate.isNotBlank() && tx.date < startDate) {
        if (tx.type == "IN") opening += tx.amount else opening -= tx.amount
      } else if ((startDate.isBlank() || tx.date >= startDate) && (endDate.isBlank() || tx.date <= endDate)) {
        periodTransactions.add(tx)
      }
    }

    var totalIn = 0.0
    var totalOut = 0.0
    var maxIn = 0.0
    var maxOut = 0.0
    val dayMap = mutableMapOf<String, Pair<Double, Double>>() // date -> (in, out)
    val categoryMap = mutableMapOf<String, Double>()

    periodTransactions.forEach { tx ->
      val (dIn, dOut) = dayMap.getOrDefault(tx.date, Pair(0.0, 0.0))
      if (tx.type == "IN") {
        totalIn += tx.amount
        if (tx.amount > maxIn) maxIn = tx.amount
        dayMap[tx.date] = Pair(dIn + tx.amount, dOut)
      } else {
        totalOut += tx.amount
        if (tx.amount > maxOut) maxOut = tx.amount
        dayMap[tx.date] = Pair(dIn, dOut + tx.amount)

        // Expense category breakdown
        val cat = if (tx.category.isNotBlank()) tx.category else "Uncategorized"
        categoryMap[cat] = (categoryMap[cat] ?: 0.0) + tx.amount
      }
    }

    val closing = opening + totalIn - totalOut

    // Prepare Day Activity List
    val dayActivityList = dayMap.entries.sortedBy { it.key }.map { (date, inOut) ->
      val dayLabel = DateUtils.getDayOfWeekShort(date).ifEmpty { DateUtils.formatShortDate(date) }
      DayActivity(
        dayLabel = dayLabel,
        date = date,
        moneyIn = inOut.first,
        moneyOut = inOut.second,
        net = inOut.first - inOut.second
      )
    }

    // Prepare Category Breakdown
    val catList = if (totalOut > 0) {
      categoryMap.entries.map { (cat, amt) ->
        val pct = ((amt / totalOut) * 100).toFloat()
        CategoryBreakdown(
          category = cat,
          amount = amt,
          percentage = pct,
          count = periodTransactions.count { it.category == cat && it.type == "OUT" }
        )
      }.sortedByDescending { it.amount }
    } else emptyList()

    return ReportData(
      periodType = period,
      periodTitle = title,
      openingBalance = opening,
      totalMoneyIn = totalIn,
      totalMoneyOut = totalOut,
      netChange = totalIn - totalOut,
      closingBalance = closing,
      transactionCount = periodTransactions.size,
      largestIncome = maxIn,
      largestExpense = maxOut,
      dayBreakdown = dayActivityList,
      categoryBreakdown = catList
    )
  }
}
