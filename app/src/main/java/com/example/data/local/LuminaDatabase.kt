package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ExportDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.ExportRecordEntity

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        ExportRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LuminaDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun exportDao(): ExportDao

    companion object {
        @Volatile
        private var INSTANCE: LuminaDatabase? = null

        fun getInstance(context: Context): LuminaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LuminaDatabase::class.java,
                    "lumina_nexus.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
