package com.example.data.local.entity

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject

data class AttachmentItem(
    val uriString: String,
    val name: String,
    val size: String,
    val mimeType: String = "",
    val isImage: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("uriString", uriString)
        put("name", name)
        put("size", size)
        put("mimeType", mimeType)
        put("isImage", isImage)
    }

    companion object {
        fun fromJson(json: JSONObject): AttachmentItem {
            return AttachmentItem(
                uriString = json.optString("uriString"),
                name = json.optString("name", "Attached File"),
                size = json.optString("size", ""),
                mimeType = json.optString("mimeType", ""),
                isImage = json.optBoolean("isImage", false)
            )
        }

        fun listToJson(list: List<AttachmentItem>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String?): List<AttachmentItem> {
            if (jsonStr.isNullOrBlank()) return emptyList()
            val list = mutableListOf<AttachmentItem>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (e: Exception) {
                // ignore
            }
            return list
        }

        fun fromUri(context: Context, uri: Uri): AttachmentItem {
            var name = "Attached File"
            var sizeBytes: Long = 0
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val isImage = mimeType.startsWith("image/")

            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val displayName = cursor.getString(nameIndex)
                            if (!displayName.isNullOrBlank()) {
                                name = displayName
                            }
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex != -1) {
                            sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                name = uri.lastPathSegment ?: "Attached File"
            }

            val formattedSize = when {
                sizeBytes <= 0 -> ""
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
                else -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes.toDouble() / (1024 * 1024))
            }

            return AttachmentItem(
                uriString = uri.toString(),
                name = name,
                size = formattedSize,
                mimeType = mimeType,
                isImage = isImage
            )
        }
    }
}
