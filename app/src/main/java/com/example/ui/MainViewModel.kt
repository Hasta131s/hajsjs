package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SendLog
import com.example.data.SpamConfig
import com.example.data.SpamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpamRepository

    val configs: StateFlow<List<SpamConfig>>
    val logs: StateFlow<List<SendLog>>

    // Input state indicators holding template form parameters
    val codeConfigTitle = MutableStateFlow("")
    val codeConfigMessage = MutableStateFlow("")
    val codeIntervalMs = MutableStateFlow("1000")
    val codeRepeatCount = MutableStateFlow("10")
    val codeMode = MutableStateFlow("TEXT_INJECTION") // "TEXT_INJECTION" or "AUTO_CLICK"

    val codeUseTargetA = MutableStateFlow(false)
    val codeTargetAX = MutableStateFlow("150")
    val codeTargetAY = MutableStateFlow("400")

    val codeUseTargetB = MutableStateFlow(false)
    val codeTargetBX = MutableStateFlow("800")
    val codeTargetBY = MutableStateFlow("400")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SpamRepository(database.dao())

        configs = repository.allConfigs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        logs = repository.recentLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default spam messages if none exist in the local database
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.allConfigs.first()
            if (list.isEmpty()) {
                seedDefaultTemplates()
            }
        }
    }

    private suspend fun seedDefaultTemplates() {
        val templates = listOf(
            SpamConfig(
                title = "Klan Savaşı Daveti (Clash)",
                message = "Sa beyler klan savaşı başladı, aktif olanlar klan kalesine ordu istesin seri saldırın!",
                intervalMs = 1500,
                repeatCount = 10,
                mode = "TEXT_INJECTION"
            ),
            SpamConfig(
                title = "Brawl Stars Kupa Kasma",
                message = "Brawl Stars takım kodu: 83JSD92, kupa kasmak isteyen davet atsın veya seri odaya gelsin!",
                intervalMs = 2000,
                repeatCount = 15,
                mode = "TEXT_INJECTION"
            ),
            SpamConfig(
                title = "Metin Kutusu & Gönder (Oyun Uyumlu)",
                message = "Fasdfasd fsdasg hgdfsa fdsasf!",
                intervalMs = 1000,
                repeatCount = 20,
                mode = "AUTO_CLICK",
                useTargetA = true,
                targetAX = 350,
                targetAY = 800,
                useTargetB = true,
                targetBX = 950,
                targetBY = 800
            )
        )
        for (item in templates) {
            repository.insertConfig(item)
        }
    }

    fun saveConfig() {
        val titleText = codeConfigTitle.value.trim()
        val messageText = codeConfigMessage.value.trim()
        if (titleText.isEmpty() || messageText.isEmpty()) return

        val interval = codeIntervalMs.value.toLongOrNull() ?: 1000L
        val repeat = codeRepeatCount.value.toIntOrNull() ?: 10

        val config = SpamConfig(
            title = titleText,
            message = messageText,
            intervalMs = interval,
            repeatCount = repeat,
            mode = codeMode.value,
            useTargetA = codeUseTargetA.value,
            targetAX = codeTargetAX.value.toIntOrNull() ?: 0,
            targetAY = codeTargetAY.value.toIntOrNull() ?: 0,
            useTargetB = codeUseTargetB.value,
            targetBX = codeTargetBX.value.toIntOrNull() ?: 0,
            targetBY = codeTargetBY.value.toIntOrNull() ?: 0
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertConfig(config)
            // Clear input fields
            codeConfigTitle.value = ""
            codeConfigMessage.value = ""
        }
    }

    fun deleteConfig(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConfigById(id)
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLogs()
        }
    }
}
