package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.AppDatabase
import com.example.data.SendLog
import com.example.data.SpamConfig
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SpamAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var floodJob: Job? = null
    private val isSpamming = AtomicBoolean(false)

    companion object {
        var instance: SpamAccessibilityService? = null
            private set

        const val ACTION_START_FLOOD = "com.example.ACTION_START_FLOOD"
        const val ACTION_STOP_FLOOD = "com.example.ACTION_STOP_FLOOD"
        const val EXTRA_CONFIG_ID = "com.example.EXTRA_CONFIG_ID"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFlood()
        instance = null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_START_FLOOD -> {
                    val configId = intent.getIntExtra(EXTRA_CONFIG_ID, -1)
                    if (configId != -1) {
                        startFlood(configId)
                    }
                }
                ACTION_STOP_FLOOD -> {
                    stopAllFloodJobs("Kullanıcı tarafından durduruldu.")
                }
            }
        }
        return START_NOT_STICKY
    }

    fun isCurrentlyRunning(): Boolean {
        return isSpamming.get() && floodJob?.isActive == true
    }

    fun startFlood(configId: Int) {
        if (isSpamming.get()) {
            stopAllFloodJobs("Önceki flood durduruluyor.")
        }

        isSpamming.set(true)
        floodJob = serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            // Fetch configuration directly using first()
            val list = db.dao().getAllConfigs().first()
            val targetConfig = list.find { it.id == configId }
            if (targetConfig != null) {
                // Start the execution loop using fetched model
                runFloodLoop(targetConfig)
            } else {
                logDatabaseError(db, "Konfigürasyon bulunamadı!", "ID: $configId")
                isSpamming.set(false)
            }
        }
    }

    private suspend fun runFloodLoop(config: SpamConfig) {
        val db = AppDatabase.getDatabase(applicationContext)
        val repeatTarget = config.repeatCount
        var sentCount = 0

        // Inform user they started flood
        NotificationHelper.createNotificationChannels(applicationContext)
        val foregroundNotification = NotificationHelper.getServiceNotificationBuilder(
            applicationContext,
            "Flood Çalışıyor: ${config.title}\nHız: ${config.intervalMs}ms"
        ).build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(1001, foregroundNotification)

        while (isSpamming.get() && (repeatTarget == 0 || sentCount < repeatTarget)) {
            delay(config.intervalMs)

            if (!isSpamming.get()) break

            val success = executeSpamAction(config)
            sentCount++

            if (success) {
                db.dao().insertLog(
                    SendLog(
                        message = config.message,
                        configTitle = config.title,
                        status = "SUCCESS"
                    )
                )
            } else {
                val errorMsg = "Sistem üzerinde mesaj gönderilemedi. Hedef alan(lar) bulunamadı veya cihaz kilitli."
                db.dao().insertLog(
                    SendLog(
                        message = config.message,
                        configTitle = config.title,
                        status = "ERROR",
                        errorMessage = errorMsg
                    )
                )
                // Instantly notify critical error on operational channel
                serviceScope.launch(Dispatchers.Main) {
                    NotificationHelper.showInstantErrorNotification(
                        applicationContext,
                        "Hata: ${config.title}",
                        errorMsg
                    )
                }
            }
        }

        // Flood completed
        isSpamming.set(false)
        manager.cancel(1001)
        db.dao().insertLog(
            SendLog(
                message = "Flood tamamlandı.",
                configTitle = config.title,
                status = "SUCCESS",
                errorMessage = "Toplam $sentCount mesaj başarıyla gönderildi."
            )
        )
    }

    private fun executeSpamAction(config: SpamConfig): Boolean {
        if (config.mode == "TEXT_INJECTION") {
            // Copy message to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("flood", config.message)
            clipboard.setPrimaryClip(clip)

            // Dynamic accessibility text injection to currently focused editor node
            val rootNode = rootInActiveWindow ?: return false
            val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focusedNode != null && focusedNode.isEditable) {
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    config.message
                )
                val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                focusedNode.recycle()
                rootNode.recycle()
                return success
            }
            rootNode.recycle()
            return false
        } else {
            // Touch gestures simulation (AUTO_CLICK)
            var actionSuccessful = true

            // Send Tap gesture at Target A coordinates (simulates message typing/tapping)
            if (config.useTargetA) {
                val pathA = Path().apply {
                    moveTo(config.targetAX.toFloat(), config.targetAY.toFloat())
                }
                val gestureA = GestureDescription.StrokeDescription(pathA, 0, 50)
                val builder = GestureDescription.Builder()
                builder.addStroke(gestureA)

                val syncObj = Object()
                var gestureResult = false

                dispatchGesture(builder.build(), object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        gestureResult = true
                        synchronized(syncObj) { syncObj.notify() }
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        gestureResult = false
                        synchronized(syncObj) { syncObj.notify() }
                    }
                }, null)

                // Wait shortly for async gesture completion
                synchronized(syncObj) {
                    try {
                        syncObj.wait(300)
                    } catch (e: InterruptedException) {
                        actionSuccessful = false
                    }
                }
            }

            // Short interval between Target A and Target B
            try {
                Thread.sleep(100)
            } catch (e: Exception) {}

            // Send Tap gesture at Target B coordinates (simulates physical press of "Send" key)
            if (config.useTargetB && actionSuccessful) {
                val pathB = Path().apply {
                    moveTo(config.targetBX.toFloat(), config.targetBY.toFloat())
                }
                val gestureB = GestureDescription.StrokeDescription(pathB, 0, 50)
                val builder = GestureDescription.Builder()
                builder.addStroke(gestureB)

                val syncObj = Object()

                dispatchGesture(builder.build(), object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        synchronized(syncObj) { syncObj.notify() }
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        actionSuccessful = false
                        synchronized(syncObj) { syncObj.notify() }
                    }
                }, null)

                synchronized(syncObj) {
                    try {
                        syncObj.wait(300)
                    } catch (e: InterruptedException) {
                        actionSuccessful = false
                    }
                }
            }

            return actionSuccessful
        }
    }

    private fun logDatabaseError(db: AppDatabase, title: String, details: String) {
        serviceScope.launch(Dispatchers.IO) {
            db.dao().insertLog(
                SendLog(
                    message = details,
                    configTitle = title,
                    status = "ERROR",
                    errorMessage = title
                )
            )
        }
    }

    fun stopFlood() {
        stopAllFloodJobs("Hizmet sonlandırılıyor.")
    }

    private fun stopAllFloodJobs(reason: String) {
        isSpamming.set(false)
        floodJob?.cancel()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.cancel(1001)
        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            db.dao().insertLog(
                SendLog(
                    message = "Flood durduruldu.",
                    configTitle = "Sistem",
                    status = "SUCCESS",
                    errorMessage = reason
                )
            )
        }
    }
}
