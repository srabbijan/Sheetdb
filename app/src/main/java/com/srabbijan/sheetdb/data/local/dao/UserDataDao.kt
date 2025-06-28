package com.srabbijan.sheetdb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.srabbijan.sheetdb.data.local.entity.DataItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DataItemDao {
    @Query("SELECT * FROM data_items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<DataItem>>

    @Query("SELECT * FROM data_items WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<DataItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: DataItem)

    @Update
    suspend fun updateItem(item: DataItem)

    @Delete
    suspend fun deleteItem(item: DataItem)

    @Query("UPDATE data_items SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM data_items")
    suspend fun clearAll()
}