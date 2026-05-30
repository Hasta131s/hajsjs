package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ControlPanelOverlayService
import com.example.service.SpamAccessibilityService
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configs by viewModel.configs.collectAsState()
    val logs by viewModel.logs.collectAsState()

    // Configuration input observers
    val configTitle by viewModel.codeConfigTitle.collectAsState()
    val configMessage by viewModel.codeConfigMessage.collectAsState()
    val intervalMs by viewModel.codeIntervalMs.collectAsState()
    val repeatCount by viewModel.codeRepeatCount.collectAsState()
    val mode by viewModel.codeMode.collectAsState()

    val useTargetA by viewModel.codeUseTargetA.collectAsState()
    val targetAX by viewModel.codeTargetAX.collectAsState()
    val targetAY by viewModel.codeTargetAY.collectAsState()

    val useTargetB by viewModel.codeUseTargetB.collectAsState()
    val targetBX by viewModel.codeTargetBX.collectAsState()
    val targetBY by viewModel.codeTargetBY.collectAsState()

    // Live permission ticks
    var isOverlayGranted by remember { mutableStateOf(false) }
    var isAccessibilityGranted by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        isOverlayGranted = Settings.canDrawOverlays(context)
        isAccessibilityGranted = SpamAccessibilityService.instance != null
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
        NotificationHelper.createNotificationChannels(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BentoBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BentoBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Bento Header Card
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoAccent)
                    ) {
                        Text("⚡", color = BentoAccentDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Flood Kontrol Paneli",
                            color = BentoTextBody,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Oyunlarda ve uygulamalarda otomatik yüksek hızlı spam aracı.",
                            color = BentoSubtext,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // SECTION 1: SYSTEM PERMISSIONS DASHBOARD
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoAccent.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = BentoPurpleBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BentoAccent.copy(alpha = 0.15f))
                            ) {
                                Text("🔒", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sistem İzinleri",
                                color = BentoAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { refreshPermissions() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = BentoSubtext
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Indicator box 1: Accessibility Service
                    PermissionIndicator(
                        title = "1. Erişilebilirlik Hizmeti",
                        description = "Otomatik mesaj göndermek ve fiziksel tıklama hareketlerini simüle etmek için gereklidir.",
                        isGranted = isAccessibilityGranted,
                        onGrantClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Indicator box 2: Overlay Permission (System alert window)
                    PermissionIndicator(
                        title = "2. Diğer Uygulamaların Üzerinde Gösterim",
                        description = "Oyun oynarken ekran üstünde kalan mini yüzen kontrol konsolunu etkinleştirir.",
                        isGranted = isOverlayGranted,
                        onGrantClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // SECTION 2: FLOATING OVERLAY PANEL TRIGGER
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BentoAccent.copy(alpha = 0.15f))
                        ) {
                            Text("📱", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yüzen Yönetim Paneli",
                            color = BentoAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Oyunların içindeyken flood başlatıp durdurmak için ekran üstü konsolu aktifleştirin.",
                        color = BentoSubtext,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (Settings.canDrawOverlays(context)) {
                                    val intent = Intent(context, ControlPanelOverlayService::class.java)
                                    context.startService(intent)
                                    NotificationHelper.showInstantErrorNotification(
                                        context,
                                        "Kontrol Paneli",
                                        "Yüzen kontrol paneli başarıyla başlatıldı ve ekran kenarına yerleştirildi."
                                    )
                                } else {
                                    NotificationHelper.showInstantErrorNotification(
                                        context,
                                        "İzin Gerekli",
                                        "Ekran üzerinde gösterim izniniz kapalı. Lütfen yukarıdan izin verin."
                                    )
                                }
                            },
                            enabled = isOverlayGranted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoAccent, 
                                contentColor = BentoAccentDark,
                                disabledContainerColor = BentoBorder,
                                disabledContentColor = BentoSubtext
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Panel Aç",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Başlat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
 
                        Button(
                            onClick = {
                                val intent = Intent(context, ControlPanelOverlayService::class.java)
                                context.stopService(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoError, contentColor = BentoErrorDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Panel Kapat",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Durdur", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECTION 3: ADD NEW CONFIGURATION TEMPLATE
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BentoAccent.copy(alpha = 0.15f))
                        ) {
                            Text("✍️", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Yeni Flood Şablonu Ekle",
                            color = BentoAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = configTitle,
                        onValueChange = { viewModel.codeConfigTitle.value = it },
                        label = { Text("Şablon Başlığı (Örn: Clash Of Clans)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoAccent,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoAccent,
                            unfocusedLabelColor = BentoSubtext,
                            focusedTextColor = BentoTextBody,
                            unfocusedTextColor = BentoTextBody
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = configMessage,
                        onValueChange = { viewModel.codeConfigMessage.value = it },
                        label = { Text("Spamlanacak Mesaj İçeriği") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoAccent,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoAccent,
                            unfocusedLabelColor = BentoSubtext,
                            focusedTextColor = BentoTextBody,
                            unfocusedTextColor = BentoTextBody
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Period interval list speed and repeat parameter inputs
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = intervalMs,
                            onValueChange = { viewModel.codeIntervalMs.value = it },
                            label = { Text("Gönderim Aralığı (ms)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoAccent,
                                unfocusedLabelColor = BentoSubtext,
                                focusedTextColor = BentoTextBody,
                                unfocusedTextColor = BentoTextBody
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )

                        OutlinedTextField(
                            value = repeatCount,
                            onValueChange = { viewModel.codeRepeatCount.value = it },
                            label = { Text("Mesaj Sayısı (0=Sonsuz)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoAccent,
                                unfocusedLabelColor = BentoSubtext,
                                focusedTextColor = BentoTextBody,
                                unfocusedTextColor = BentoTextBody
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Type mode selector
                    Text(
                        text = "Gönderim Yöntemi:",
                        color = BentoTextBody,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.codeMode.value = "TEXT_INJECTION" }
                    ) {
                        RadioButton(
                            selected = mode == "TEXT_INJECTION",
                            onClick = { viewModel.codeMode.value = "TEXT_INJECTION" },
                            colors = RadioButtonDefaults.colors(selectedColor = BentoAccent)
                        )
                        Column {
                            Text(
                                "Metin Alanına Yazdır (Erişilebilirlik)",
                                color = BentoTextBody,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Aktif odaklı klavye kutusunu bulur ve yazıyı enjekte eder.",
                                color = BentoSubtext,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.codeMode.value = "AUTO_CLICK" }
                    ) {
                        RadioButton(
                            selected = mode == "AUTO_CLICK",
                            onClick = { viewModel.codeMode.value = "AUTO_CLICK" },
                            colors = RadioButtonDefaults.colors(selectedColor = BentoAccent)
                        )
                        Column {
                            Text(
                                "Fiziksel Tıklamayı Simüle Et (Oyun Modu)",
                                color = BentoTextBody,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Belirlediğiniz koordinatlara basarak klavye ve gönderimi tetikler.",
                                color = BentoSubtext,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Expand coordinator coordinates inputs if game mode trigger is configured
                    if (mode == "AUTO_CLICK") {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BentoBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Target A setup switch and absolute coordinates UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hedef A'yı Kullan (Yazı Alanı Konumu)",
                                color = BentoSubtext,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = useTargetA,
                                onCheckedChange = { viewModel.codeUseTargetA.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = BentoAccent)
                            )
                        }

                        if (useTargetA) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = targetAX,
                                    onValueChange = { viewModel.codeTargetAX.value = it },
                                    label = { Text("A_X Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoAccent,
                                        unfocusedBorderColor = BentoBorder,
                                        focusedTextColor = BentoTextBody,
                                        unfocusedTextColor = BentoTextBody
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                OutlinedTextField(
                                    value = targetAY,
                                    onValueChange = { viewModel.codeTargetAY.value = it },
                                    label = { Text("A_Y Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoAccent,
                                        unfocusedBorderColor = BentoBorder,
                                        focusedTextColor = BentoTextBody,
                                        unfocusedTextColor = BentoTextBody
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Target B setup switch and absolute coordinates UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hedef B'yi Kullan (Gönderim Tuşu)",
                                color = BentoSubtext,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = useTargetB,
                                onCheckedChange = { viewModel.codeUseTargetB.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = BentoAccent)
                            )
                        }

                        if (useTargetB) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = targetBX,
                                    onValueChange = { viewModel.codeTargetBX.value = it },
                                    label = { Text("B_X Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoAccent,
                                        unfocusedBorderColor = BentoBorder,
                                        focusedTextColor = BentoTextBody,
                                        unfocusedTextColor = BentoTextBody
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                OutlinedTextField(
                                    value = targetBY,
                                    onValueChange = { viewModel.codeTargetBY.value = it },
                                    label = { Text("B_Y Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoAccent,
                                        unfocusedBorderColor = BentoBorder,
                                        focusedTextColor = BentoTextBody,
                                        unfocusedTextColor = BentoTextBody
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BentoPurpleBg, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Yardım",
                                tint = BentoAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "İpucu: Geliştirici Seçenekleri -> 'İşaretçi Konumu' ayarını açarak oyununuzdaki butonların tam X ve Y koordinatını kolayca bulabilirsiniz.",
                                color = BentoTextGhost,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.saveConfig() },
                        enabled = configTitle.isNotBlank() && configMessage.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoAccent,
                            contentColor = BentoAccentDark,
                            disabledContainerColor = BentoBorder,
                            disabledContentColor = BentoSubtext
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Ekle")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Şablonu Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 4: CONFIGURED SPAM TEMPLATES LIST
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BentoAccent.copy(alpha = 0.15f))
                ) {
                    Text("📁", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kaydedilmiş Şablonlar",
                    color = BentoAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (configs.isEmpty()) {
                Text(
                    text = "Kayıtlı şablon bulunmamaktadır.",
                    color = BentoSubtext,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            } else {
                configs.forEach { config ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = config.title,
                                    color = BentoTextBody,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { viewModel.deleteConfig(config.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint = BentoError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = config.message,
                                color = BentoTextGhost,
                                fontSize = 12.sp,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BentoBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Interval Badge
                                    Text(
                                        text = "${config.intervalMs}ms",
                                        color = BentoSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(BentoSuccessDark, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Count Badge
                                    Text(
                                        text = if (config.repeatCount == 0) "Sonsuz" else "${config.repeatCount} Adet",
                                        color = BentoAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(BentoPurpleBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Mode Badge
                                    Text(
                                        text = if (config.mode == "AUTO_CLICK") "Oyun" else "Metin",
                                        color = BentoTextBody,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .background(BentoBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Quick launch start button
                                Button(
                                    onClick = {
                                        if (isAccessibilityGranted) {
                                            // Direct activation via active static instance
                                            SpamAccessibilityService.instance?.startFlood(config.id)
                                            NotificationHelper.showInstantErrorNotification(
                                                context,
                                                "Flood Başlatıldı",
                                                "Şablon: '${config.title}' hedef uygulamalara doğru başlatıldı."
                                            )
                                        } else {
                                            NotificationHelper.showInstantErrorNotification(
                                                context,
                                                "Erişilebilirlik Kapalı",
                                                "Spam gönderebilmek için sol üstteki Erişilebilirlik İzni butonunu etkinleştirmelisiniz."
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccent, contentColor = BentoAccentDark),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Başlat",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hızlı Başlat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 5: REAL-TIME SECURE DATABASE LOGS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BentoAccent.copy(alpha = 0.15f))
                    ) {
                        Text("📋", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gönderim Günlüğü",
                        color = BentoAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Temizle",
                    color = BentoError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.clearLogHistory() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Text(
                    text = "Gönderim kaydı bulunmamaktadır. Flood başlattığınızda loglar burada görünecektir.",
                    color = BentoSubtext,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = BentoOverlayLogBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        items(logs) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatTimestamp(log.timestamp),
                                            color = BentoSubtext,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = log.configTitle,
                                            color = BentoAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.message,
                                        color = BentoTextBody,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        lineHeight = 13.sp
                                    )
                                    if (log.errorMessage != null) {
                                        Text(
                                            text = log.errorMessage,
                                            color = BentoError,
                                            fontSize = 9.sp,
                                            lineHeight = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Success status badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (log.status == "SUCCESS") BentoSuccessDark else BentoPurpleBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (log.status == "SUCCESS") "OK" else "HATA",
                                        color = if (log.status == "SUCCESS") BentoSuccess else BentoError,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionIndicator(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BentoCardBg)
            .border(androidx.compose.foundation.BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) BentoSuccess else BentoError,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = BentoTextBody,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = BentoSubtext,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onGrantClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) BentoSuccessDark else BentoAccent,
                contentColor = if (isGranted) BentoSuccess else BentoAccentDark
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = if (isGranted) "Aktif" else "Etkinleştir",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
