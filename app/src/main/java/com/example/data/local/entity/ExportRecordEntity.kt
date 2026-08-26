package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "export_records")
data class ExportRecordEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val format: String, // "JSON", "CSV", "PDF"
    val fileSize: String,
    val status: String, // "Ready", "Processing", "Expired"
    val scope: String, // "ALL", "DATE_RANGE", "SETTINGS"
    val dateRangeText: String? = null,
    val filePayload: String? = null
)
