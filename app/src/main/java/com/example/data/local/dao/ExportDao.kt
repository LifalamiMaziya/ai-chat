package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ExportRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExportDao {
    @Query("SELECT * FROM export_records ORDER BY timestamp DESC")
    fun getAllExports(): Flow<List<ExportRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: ExportRecordEntity)

    @Query("UPDATE export_records SET status = :status, fileSize = :fileSize WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, fileSize: String)

    @Query("DELETE FROM export_records WHERE id = :id")
    suspend fun deleteExport(id: String)
}
