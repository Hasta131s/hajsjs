package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.AppDatabase
import com.example.data.SpamConfig
import com.example.ui.theme.*
import com.example.util.FloatingLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ControlPanelOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayLayout: View? = null
    private var floatingLifecycleOwner: FloatingLifecycleOwner? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        scope.cancel()
    }

    private fun showOverlay() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 150
        }

        floatingLifecycleOwner = FloatingLifecycleOwner()
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(floatingLifecycleOwner)
            setViewTreeViewModelStoreOwner(floatingLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(floatingLifecycleOwner)

            setContent {
                MaterialTheme {
                    OverlayContent(
                        params = params,
                        onUpdateParams = { updatedParams ->
                            try {
                                windowManager.updateViewLayout(this@apply, updatedParams)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onClose = {
                            stopSelf()
                        }
                    )
                }
            }
        }

        overlayLayout = composeView
        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayLayout?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingLifecycleOwner?.destroy()
        floatingLifecycleOwner = null
    }
}

@Composable
fun OverlayContent(
    params: WindowManager.LayoutParams,
    onUpdateParams: (WindowManager.LayoutParams) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val configsFlow = remember {
        db.dao().getAllConfigs().stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    val configs by configsFlow.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }
    var selectedConfig: SpamConfig? by remember { mutableStateOf(null) }

    // Read flood states from active Accessibility Service instance
    val serviceActive = SpamAccessibilityService.instance != null
    val isRunning = SpamAccessibilityService.instance?.isCurrentlyRunning() == true

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(1.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(
                containerColor = BentoCardBg.copy(alpha = 0.95f),
                contentColor = BentoTextBody
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .width(if (isExpanded) 240.dp else 72.dp)
            ) {
                // Header row containing draggable handle and size controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Custom drag handle detector
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    params.x += dragAmount.x.toInt()
                                    params.y += dragAmount.y.toInt()
                                    onUpdateParams(params)
                                }
                            }
                            .clip(CircleShape)
                            .background(BentoPurpleBg)
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Sürükle",
                            tint = BentoAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isExpanded) {
                        Text(
                            text = "Flood Panel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoAccent,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Toggle expand / collapse
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BentoPurpleBg)
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "DARAL" else "GENİŞLE",
                            color = BentoAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        if (!serviceActive) {
                            Text(
                                text = "Erişilebilirlik iznini aktifleştirin!",
                                fontSize = 11.sp,
                                color = BentoError,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Text(
                                text = if (isRunning) "Durum: Flood Aktif" else "Durum: Beklemede",
                                fontSize = 11.sp,
                                color = if (isRunning) BentoSuccess else BentoTextBody,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        // Selectable configuration template item selector
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hızlı Şablonlar:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoAccent
                        )

                        if (configs.isEmpty()) {
                            Text(
                                text = "Şablon bulunamadı. Lütfen ana ekran üzerinden yeni şablon oluşturun.",
                                fontSize = 10.sp,
                                color = BentoSubtext,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                            ) {
                                items(configs) { config ->
                                    val isCurrent = selectedConfig?.id == config.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) BentoPurpleBg else Color.Transparent)
                                            .clickable {
                                                selectedConfig = config
                                            }
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = config.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) BentoAccent else BentoTextGhost,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Trigger start or stop
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val currentConfig = selectedConfig ?: configs.firstOrNull()
                                    if (currentConfig != null && serviceActive) {
                                        SpamAccessibilityService.instance?.startFlood(currentConfig.id)
                                    }
                                },
                                enabled = serviceActive && configs.isNotEmpty() && !isRunning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoSuccess,
                                    contentColor = BentoSuccessDark,
                                    disabledContainerColor = BentoBorder,
                                    disabledContentColor = BentoSubtext
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Başlat",
                                    tint = BentoSuccessDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Başlat", color = BentoSuccessDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    if (serviceActive) {
                                        SpamAccessibilityService.instance?.stopFlood()
                                    }
                                },
                                enabled = serviceActive && isRunning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoError,
                                    contentColor = BentoErrorDark,
                                    disabledContainerColor = BentoBorder,
                                    disabledContentColor = BentoSubtext
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("■", color = BentoErrorDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Durdur", color = BentoErrorDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Mini toolbar collapse state buttons
                AnimatedVisibility(
                    visible = !isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        IconButton(
                            onClick = {
                                if (serviceActive && isRunning) {
                                    SpamAccessibilityService.instance?.stopFlood()
                                } else if (serviceActive) {
                                    val currentConfig = selectedConfig ?: configs.firstOrNull()
                                    if (currentConfig != null) {
                                        SpamAccessibilityService.instance?.startFlood(currentConfig.id)
                                    }
                                }
                            },
                            enabled = serviceActive && (configs.isNotEmpty() || isRunning),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) BentoError else BentoSuccess)
                        ) {
                            if (isRunning) {
                                Text("■", color = BentoErrorDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Mini Tetikleyici",
                                    tint = BentoSuccessDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Close button to terminate overlay
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = BentoSubtext,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
