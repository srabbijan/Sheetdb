package com.srabbijan.sheetdb.service

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import android.util.Log
import com.srabbijan.sheetdb.data.local.entity.DataItem
import com.srabbijan.sheetdb.data.local.entity.SaleEntity
import com.srabbijan.sheetdb.data.local.entity.SaleItemsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsService(private val context: Context) {
    private val signInHelper = GoogleSignInHelper(context)

    suspend fun createSpreadsheet(title: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val account = signInHelper.getCurrentUser() ?: return@withContext null

                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf("https://www.googleapis.com/auth/spreadsheets",
                        "https://www.googleapis.com/auth/drive.file")
                ).apply {
                    selectedAccount = account.account
                }

                val transport = NetHttpTransport()
                val jsonFactory = GsonFactory.getDefaultInstance()

                val service = Sheets.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Your App Name")
                    .build()

                // Create spreadsheet with multiple sheets
                val spreadsheet = Spreadsheet().apply {
                    properties = SpreadsheetProperties().apply {
                        this.title = title
                    }
                    sheets = listOf(
                        // Sales sheet
                        Sheet().apply {
                            properties = SheetProperties().apply {
//                                title = "Sales"
                                setTitle("Sales")
                                sheetId = 0
                            }
                        },
                        // Sale Items sheet
                        Sheet().apply {
                            properties = SheetProperties().apply {
//                                title = "Sale Items"
                                setTitle("Sale Items")
                                sheetId = 1
                            }
                        },
                        // DataItem sheet (your original data)
                        Sheet().apply {
                            properties = SheetProperties().apply {
//                                title =
                                setTitle("Data Items")
                                sheetId = 2
                            }
                        }
                    )
                }

                val request = service.spreadsheets().create(spreadsheet)
                val response = request.execute()

                // Add headers to all sheets
                addHeadersToAllSheets(service, response.spreadsheetId)

                response.spreadsheetId
            } catch (e: Exception) {
                Log.e("GoogleSheetsService", "Error creating spreadsheet", e)
                null
            }
        }
    }

    private suspend fun addHeadersToAllSheets(service: Sheets, spreadsheetId: String) {
        try {
            // Sales headers
            val salesHeaders = listOf(listOf(
                "ID", "Shop ID", "Customer ID", "Total Amount", "Discount",
                "Paid Amount", "Due Amount", "Total Product", "Total Profit",
                "Note", "Date", "Created At", "Updated At", "Deleted At", "Is Synced"
            ))

            // Sale Items headers
            val saleItemsHeaders = listOf(listOf(
                "ID", "Shop ID", "Sale ID", "Product ID", "Product Name",
                "Purchase Price", "Sale Price", "Qty", "Total", "Profit",
                "Created At", "Updated At", "Deleted At", "Is Synced"
            ))

            // Data Items headers (your original)
            val dataItemsHeaders = listOf(listOf(
                "ID", "Title", "Description", "Created At", "Updated At"
            ))

            // Update Sales sheet headers
            val salesValueRange = ValueRange().setValues(salesHeaders)
            service.spreadsheets().values()
                .update(spreadsheetId, "Sales!A1:O1", salesValueRange)
                .setValueInputOption("RAW")
                .execute()

            // Update Sale Items sheet headers
            val saleItemsValueRange = ValueRange().setValues(saleItemsHeaders)
            service.spreadsheets().values()
                .update(spreadsheetId, "Sale Items!A1:N1", saleItemsValueRange)
                .setValueInputOption("RAW")
                .execute()

            // Update Data Items sheet headers
            val dataItemsValueRange = ValueRange().setValues(dataItemsHeaders)
            service.spreadsheets().values()
                .update(spreadsheetId, "Data Items!A1:E1", dataItemsValueRange)
                .setValueInputOption("RAW")
                .execute()

        } catch (e: Exception) {
            Log.e("GoogleSheetsService", "Error adding headers", e)
        }
    }

    suspend fun syncSalesToSheet(spreadsheetId: String, sales: List<SaleEntity>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val service = getAuthenticatedSheetsService() ?: return@withContext false

                // Clear existing sales data (except headers)
                service.spreadsheets().values()
                    .clear(spreadsheetId, "Sales!A2:O", ClearValuesRequest())
                    .execute()

                // Add new sales data
                val values = sales.map { sale ->
                    listOf(
                        sale.id,
                        sale.shopId,
                        sale.customerId ?: "",
                        sale.totalAmount.toString(),
                        sale.discount.toString(),
                        sale.paidAmount.toString(),
                        sale.dueAmount.toString(),
                        sale.totalProduct.toString(),
                        sale.totalProfit.toString(),
                        sale.note ?: "",
                        sale.date,
                        sale.createdAt,
                        sale.updatedAt,
                        sale.deletedAt ?: "",
                        sale.isSynced.toString()
                    )
                }

                if (values.isNotEmpty()) {
                    val valueRange = ValueRange().setValues(values)
                    service.spreadsheets().values()
                        .update(spreadsheetId, "Sales!A2", valueRange)
                        .setValueInputOption("RAW")
                        .execute()
                }

                true
            } catch (e: Exception) {
                Log.e("GoogleSheetsService", "Error syncing sales data", e)
                false
            }
        }
    }

    suspend fun syncSaleItemsToSheet(spreadsheetId: String, saleItems: List<SaleItemsEntity>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val service = getAuthenticatedSheetsService() ?: return@withContext false

                // Clear existing sale items data (except headers)
                service.spreadsheets().values()
                    .clear(spreadsheetId, "Sale Items!A2:N", ClearValuesRequest())
                    .execute()

                // Add new sale items data
                val values = saleItems.map { item ->
                    listOf(
                        item.id,
                        item.shopId,
                        item.saleId,
                        item.productId,
                        item.productName,
                        item.purchasePrice.toString(),
                        item.salePrice.toString(),
                        item.qty.toString(),
                        item.total.toString(),
                        item.profit.toString(),
                        item.createdAt,
                        item.updatedAt,
                        item.deletedAt ?: "",
                        item.isSynced.toString()
                    )
                }

                if (values.isNotEmpty()) {
                    val valueRange = ValueRange().setValues(values)
                    service.spreadsheets().values()
                        .update(spreadsheetId, "Sale Items!A2", valueRange)
                        .setValueInputOption("RAW")
                        .execute()
                }

                true
            } catch (e: Exception) {
                Log.e("GoogleSheetsService", "Error syncing sale items data", e)
                false
            }
        }
    }

    // Your original method (renamed for clarity)
    suspend fun syncDataToSheet(spreadsheetId: String, items: List<DataItem>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val service = getAuthenticatedSheetsService() ?: return@withContext false

                // Clear existing data (except headers)
                service.spreadsheets().values()
                    .clear(spreadsheetId, "Data Items!A2:E", ClearValuesRequest())
                    .execute()

                // Add new data
                val values = items.map { item ->
                    listOf(
                        item.id,
                        item.title,
                        item.description,
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date(item.createdAt)),
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date(item.updatedAt))
                    )
                }

                if (values.isNotEmpty()) {
                    val valueRange = ValueRange().setValues(values)
                    service.spreadsheets().values()
                        .update(spreadsheetId, "Data Items!A2", valueRange)
                        .setValueInputOption("RAW")
                        .execute()
                }

                true
            } catch (e: Exception) {
                Log.e("GoogleSheetsService", "Error syncing data", e)
                false
            }
        }
    }

    // Sync all data at once
    suspend fun syncAllDataToSheet(
        spreadsheetId: String,
        sales: List<SaleEntity>,
        saleItems: List<SaleItemsEntity>,
        dataItems: List<DataItem>
    ): Boolean {
        return try {
            val salesResult = syncSalesToSheet(spreadsheetId, sales)
            val saleItemsResult = syncSaleItemsToSheet(spreadsheetId, saleItems)
            val dataItemsResult = syncDataToSheet(spreadsheetId, dataItems)

            salesResult && saleItemsResult && dataItemsResult
        } catch (e: Exception) {
            Log.e("GoogleSheetsService", "Error syncing all data", e)
            false
        }
    }

    private suspend fun getAuthenticatedSheetsService(): Sheets? {
        return try {
            val account = signInHelper.getCurrentUser() ?: return null

            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf("https://www.googleapis.com/auth/spreadsheets")
            ).apply {
                selectedAccount = account.account
            }

            val transport = NetHttpTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()

            Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("Your App Name")
                .build()
        } catch (e: Exception) {
            Log.e("GoogleSheetsService", "Error getting authenticated service", e)
            null
        }
    }

    // Helper method to add a new sheet to existing spreadsheet
    suspend fun addNewSheet(spreadsheetId: String, sheetTitle: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val service = getAuthenticatedSheetsService() ?: return@withContext false

                val addSheetRequest = AddSheetRequest().apply {
                    properties = SheetProperties().apply {
                        title = sheetTitle
                    }
                }

                val batchUpdateRequest = BatchUpdateSpreadsheetRequest().apply {
                    requests = listOf(
                        Request().apply {
                            addSheet = addSheetRequest
                        }
                    )
                }

                service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
                true
            } catch (e: Exception) {
                Log.e("GoogleSheetsService", "Error adding new sheet", e)
                false
            }
        }
    }
}