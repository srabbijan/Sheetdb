package com.srabbijan.sheetdb.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.srabbijan.sheetdb.data.local.dao.DataItemDao
import com.srabbijan.sheetdb.data.local.entity.DataItem
import com.srabbijan.sheetdb.service.GoogleSheetsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import androidx.core.content.edit

class DataRepository(
    private val dao: DataItemDao,
    private val sheetsService: GoogleSheetsService,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getAllItems(): Flow<List<DataItem>> = dao.getAllItems()

    suspend fun insertItem(item: DataItem) {
        dao.insertItem(item)
        if (isOnline()) {
            syncToSheet()
        }
    }

    suspend fun updateItem(item: DataItem) {
        dao.updateItem(item.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
        if (isOnline()) {
            syncToSheet()
        }
    }

    suspend fun deleteItem(item: DataItem) {
        dao.deleteItem(item)
        if (isOnline()) {
            syncToSheet()
        }
    }

    suspend fun syncToSheet(): Boolean {
        if (!isOnline()) return false

        val spreadsheetId = getOrCreateSpreadsheet() ?: return false
        val allItems = dao.getAllItems().first()

        return if (sheetsService.syncDataToSheet(spreadsheetId, allItems)) {
            // Mark all items as synced
            allItems.forEach { dao.markAsSynced(it.id) }
            true
        } else {
            false
        }
    }

    private suspend fun getOrCreateSpreadsheet(): String? {
        var spreadsheetId = prefs.getString("spreadsheet_id", null)

        if (spreadsheetId == null) {
            spreadsheetId = sheetsService.createSpreadsheet("sPOS DATA BASE")
            spreadsheetId?.let {
                prefs.edit { putString("spreadsheet_id", it) }
            }
        }

        return spreadsheetId
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}