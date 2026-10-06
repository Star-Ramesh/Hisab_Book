package com.example.data.repository

import com.example.data.local.KhataDao
import com.example.data.local.KhataSettings
import com.example.data.local.KhataTransaction
import com.example.model.KhataEntryWithBalance
import com.example.model.SummaryStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KhataRepository(private val khataDao: KhataDao) {

  val settingsFlow: Flow<KhataSettings> = khataDao.getSettings().map {
    it ?: KhataSettings()
  }

  val allTransactionsAsc: Flow<List<KhataTransaction>> = khataDao.getAllTransactionsAsc()

  val allTransactionsDesc: Flow<List<KhataTransaction>> = khataDao.getAllTransactionsDesc()

  /**
   * Flow of all transactions descending with their accurate chronological running balance
   */
  val ledgerEntriesDesc: Flow<List<KhataEntryWithBalance>> = combine(
    allTransactionsAsc,
    settingsFlow
  ) { transactions, settings ->
    var running = settings.openingBalance
    val entriesWithBalanceAsc = transactions.map { tx ->
      if (tx.type == "IN") {
        running += tx.amount
      } else {
        running -= tx.amount
      }
      KhataEntryWithBalance(
        transaction = tx,
        runningBalance = running
      )
    }
    entriesWithBalanceAsc.reversed()
  }

  /**
   * Flow of overall summary statistics
   */
  val summaryStats: Flow<SummaryStats> = combine(
    allTransactionsAsc,
    settingsFlow
  ) { transactions, settings ->
    val opening = settings.openingBalance
    var totalIn = 0.0
    var totalOut = 0.0
    var todayIn = 0.0
    var todayOut = 0.0
    var monthIn = 0.0
    var monthOut = 0.0
    var maxIn = 0.0
    var maxOut = 0.0

    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    transactions.forEach { tx ->
      if (tx.type == "IN") {
        totalIn += tx.amount
        if (tx.amount > maxIn) maxIn = tx.amount
        if (tx.date == todayDateStr) todayIn += tx.amount
        if (tx.date.startsWith(currentMonthPrefix)) monthIn += tx.amount
      } else {
        totalOut += tx.amount
        if (tx.amount > maxOut) maxOut = tx.amount
        if (tx.date == todayDateStr) todayOut += tx.amount
        if (tx.date.startsWith(currentMonthPrefix)) monthOut += tx.amount
      }
    }

    val currentBalance = opening + totalIn - totalOut

    SummaryStats(
      openingBalance = opening,
      currentBalance = currentBalance,
      totalMoneyIn = totalIn,
      totalMoneyOut = totalOut,
      netChange = totalIn - totalOut,
      todayIn = todayIn,
      todayOut = todayOut,
      monthIn = monthIn,
      monthOut = monthOut,
      totalTransactions = transactions.size,
      largestIncome = maxIn,
      largestExpense = maxOut
    )
  }

  fun getTransactionById(id: Long): Flow<KhataTransaction?> = khataDao.getTransactionById(id)

  suspend fun getTransactionByIdSync(id: Long): KhataTransaction? = khataDao.getTransactionByIdSync(id)

  suspend fun insertTransaction(transaction: KhataTransaction): Long =
    khataDao.insertTransaction(transaction)

  suspend fun updateTransaction(transaction: KhataTransaction) =
    khataDao.updateTransaction(transaction)

  suspend fun deleteTransaction(id: Long) =
    khataDao.deleteTransactionById(id)

  suspend fun updateSettings(settings: KhataSettings) =
    khataDao.insertOrUpdateSettings(settings)

  suspend fun updateOpeningBalance(newOpeningBalance: Double) {
    val current = khataDao.getSettingsSync() ?: KhataSettings()
    val updated = current.copy(
      openingBalance = newOpeningBalance,
      isOpeningBalanceSet = true
    )
    khataDao.insertOrUpdateSettings(updated)
  }

  suspend fun clearAllData() {
    khataDao.clearAllTransactions()
    val current = khataDao.getSettingsSync() ?: KhataSettings()
    khataDao.insertOrUpdateSettings(
      current.copy(
        openingBalance = 0.0,
        isOpeningBalanceSet = false
      )
    )
  }

  suspend fun restoreData(transactions: List<KhataTransaction>, settings: KhataSettings) {
    khataDao.clearAllTransactions()
    khataDao.insertAllTransactions(transactions)
    khataDao.insertOrUpdateSettings(settings)
  }
}
