package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- SpamConfig operations ---
    @Query("SELECT * FROM spam_config ORDER BY id DESC")
    fun getAllConfigs(): Flow<List<SpamConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SpamConfig)

    @Query("DELETE FROM spam_config WHERE id = :id")
    suspend fun deleteConfigById(id: Int)

    // --- SendLog operations ---
    @Query("SELECT * FROM send_log ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<SendLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SendLog)

    @Query("DELETE FROM send_log")
    suspend fun clearLogs()
}
