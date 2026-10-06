package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class KhataTransaction(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val type: String, // "IN" or "OUT"
  val amount: Double,
  val person: String = "", // Source/Received from for IN, Destination/Spent on for OUT
  val reason: String = "",
  val note: String = "",
  val category: String = "",
  val date: String, // "yyyy-MM-dd"
  val time: String, // "HH:mm"
  val timestamp: Long, // Epoch millis for chronological sorting
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
