package com.example.data

import kotlinx.coroutines.flow.Flow

class SpamRepository(private val dao: AppDao) {
    val allConfigs: Flow<List<SpamConfig>> = dao.getAllConfigs()
    val recentLogs: Flow<List<SendLog>> = dao.getRecentLogs()

    suspend fun insertConfig(config: SpamConfig) {
        dao.insertConfig(config)
    }

    suspend fun deleteConfigById(id: Int) {
        dao.deleteConfigById(id)
    }

    suspend fun insertLog(log: SendLog) {
        dao.insertLog(log)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }
}
