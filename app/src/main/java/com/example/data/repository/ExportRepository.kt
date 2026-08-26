package com.example.data.repository

import com.example.data.local.dao.ExportDao
import com.example.data.local.entity.ExportRecordEntity
import com.example.data.network.ApiClient
import com.example.data.network.model.ExportRequestDto
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.UUID

class ExportRepository(
    private val exportDao: ExportDao,
    private val apiClient: ApiClient
) {
    val allExports: Flow<List<ExportRecordEntity>> = exportDao.getAllExports()

    suspend fun generateExport(
        title: String,
        format: String,
        scope: String,
        dateRangeText: String?
    ): ExportRecordEntity {
        val exportId = "exp_" + UUID.randomUUID().toString().take(8)
        val initialRecord = ExportRecordEntity(
            id = exportId,
            title = title,
            timestamp = System.currentTimeMillis(),
            format = format,
            fileSize = "Pending...",
            status = "Processing",
            scope = scope,
            dateRangeText = dateRangeText
        )
        exportDao.insertExport(initialRecord)

        // Request export generation from backend server
        val request = ExportRequestDto(
            title = title,
            format = format,
            scope = scope,
            dateRangeText = dateRangeText
        )

        try {
            val response = apiClient.getApiService().generateExport(request)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val updatedRecord = initialRecord.copy(
                    id = dto.id,
                    status = dto.status,
                    fileSize = dto.fileSize,
                    filePayload = dto.filePayload ?: dto.downloadUrl
                )
                exportDao.insertExport(updatedRecord)
                return updatedRecord
            } else {
                val errorMsg = "Server export failed (${response.code()}): ${response.message()}"
                exportDao.updateStatus(exportId, "Failed", "0 KB")
                throw IOException(errorMsg)
            }
        } catch (e: Exception) {
            exportDao.updateStatus(exportId, "Failed", "0 KB")
            throw e
        }
    }

    suspend fun deleteExport(id: String) {
        exportDao.deleteExport(id)
    }
}
