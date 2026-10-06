package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.KhataSettings
import com.example.data.local.KhataTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter

object BackupRestoreManager {

  data class BackupData(
    val settings: KhataSettings,
    val transactions: List<KhataTransaction>
  )

  fun exportBackupToJson(
    context: Context,
    settings: KhataSettings,
    transactions: List<KhataTransaction>
  ): Result<File> {
    return try {
      val root = JSONObject()
      root.put("version", 1)
      root.put("appName", "My Khata")
      root.put("exportedAt", System.currentTimeMillis())

      val settingsObj = JSONObject().apply {
        put("userName", settings.userName)
        put("openingBalance", settings.openingBalance)
        put("isOpeningBalanceSet", settings.isOpeningBalanceSet)
        put("currencySymbol", settings.currencySymbol)
        put("currencyCode", settings.currencyCode)
        put("darkModePreference", settings.darkModePreference)
        put("customCategories", settings.customCategories)
      }
      root.put("settings", settingsObj)

      val txArray = JSONArray()
      transactions.forEach { tx ->
        val txObj = JSONObject().apply {
          put("id", tx.id)
          put("type", tx.type)
          put("amount", tx.amount)
          put("person", tx.person)
          put("reason", tx.reason)
          put("note", tx.note)
          put("category", tx.category)
          put("date", tx.date)
          put("time", tx.time)
          put("timestamp", tx.timestamp)
          put("createdAt", tx.createdAt)
          put("updatedAt", tx.updatedAt)
        }
        txArray.put(txObj)
      }
      root.put("transactions", txArray)

      val cacheDir = File(context.cacheDir, "backups")
      if (!cacheDir.exists()) cacheDir.mkdirs()
      val file = File(cacheDir, "MyKhata_Backup_${System.currentTimeMillis()}.json")
      val writer = FileWriter(file)
      writer.write(root.toString(2))
      writer.flush()
      writer.close()

      // Share intent
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "My Khata Full Backup")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(shareIntent, "Save or Share Backup")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)

      Result.success(file)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun parseBackupJson(jsonString: String): Result<BackupData> {
    return try {
      val root = JSONObject(jsonString)
      val settingsObj = root.optJSONObject("settings") ?: JSONObject()
      val settings = KhataSettings(
        id = 1,
        userName = settingsObj.optString("userName", ""),
        openingBalance = settingsObj.optDouble("openingBalance", 0.0),
        isOpeningBalanceSet = settingsObj.optBoolean("isOpeningBalanceSet", true),
        currencySymbol = settingsObj.optString("currencySymbol", "₹"),
        currencyCode = settingsObj.optString("currencyCode", "INR"),
        darkModePreference = settingsObj.optString("darkModePreference", "SYSTEM"),
        customCategories = settingsObj.optString("customCategories", "")
      )

      val txList = mutableListOf<KhataTransaction>()
      val txArray = root.optJSONArray("transactions") ?: JSONArray()
      for (i in 0 until txArray.length()) {
        val o = txArray.getJSONObject(i)
        txList.add(
          KhataTransaction(
            id = 0, // Reset for autogenerate on restore
            type = o.optString("type", "IN"),
            amount = o.optDouble("amount", 0.0),
            person = o.optString("person", ""),
            reason = o.optString("reason", ""),
            note = o.optString("note", ""),
            category = o.optString("category", ""),
            date = o.optString("date", ""),
            time = o.optString("time", ""),
            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
          )
        )
      }
      Result.success(BackupData(settings, txList))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
