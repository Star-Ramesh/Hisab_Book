package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataDao {
  @Query("SELECT * FROM transactions ORDER BY timestamp ASC, id ASC")
  fun getAllTransactionsAsc(): Flow<List<KhataTransaction>>

  @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
  fun getAllTransactionsDesc(): Flow<List<KhataTransaction>>

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  fun getTransactionById(id: Long): Flow<KhataTransaction?>

  @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
  suspend fun getTransactionByIdSync(id: Long): KhataTransaction?

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<KhataSettings?>

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsSync(): KhataSettings?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: KhataTransaction): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllTransactions(transactions: List<KhataTransaction>)

  @Update
  suspend fun updateTransaction(transaction: KhataTransaction)

  @Delete
  suspend fun deleteTransaction(transaction: KhataTransaction)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransactionById(id: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSettings(settings: KhataSettings)

  @Query("DELETE FROM transactions")
  suspend fun clearAllTransactions()
}
