package com.srabbijan.sheetdb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "data_items")
data class DataItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
@Entity
data class SaleItemsEntity(
    @PrimaryKey
    val id: String,
    val shopId: String,
    val saleId: String,
    val productId: String,
    val productName: String,
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val qty: Double = 0.0,
    val total: Double = 0.0,
    val profit: Double = 0.0,
    val createdAt: String = "getCurrentDateTime()",
    val updatedAt: String = "getCurrentDateTime()",
    val deletedAt: String? = null,
    val isSynced: Boolean = false
)
@Entity
data class SaleEntity(
    @PrimaryKey
    val id: String,
    val shopId: String,
    val customerId: String? = null,
    val totalAmount: Double = 0.0, //with discount
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val totalProduct:Int = 0,
    val totalProfit:Double = 0.0,
    val note:String? = null,
    val date: String =" getCurrentDateTime()",
    val createdAt: String = "getCurrentDateTime()",
    val updatedAt: String = "getCurrentDateTime()",
    val deletedAt: String? = null,
    val isSynced: Boolean = false
)