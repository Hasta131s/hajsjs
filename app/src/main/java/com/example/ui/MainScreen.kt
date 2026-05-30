package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
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
        containerColor = Color(0xFF0F1016)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F1016))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Screen Header title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Flood Icon",
                    tint = Color(0xFFA594FD),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "OYUN FLOOD ARACI",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Oyunlarda ve uygulamalarda dilediğiniz hızda otomatik spam gönderin.",
                color = Color(0xFF9EA3B0),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // SECTION 1: SYSTEM PERMISSIONS DASHBOARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C26)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sistem İzinleri",
                            color = Color(0xFFA594FD),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { refreshPermissions() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = Color.LightGray
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E213A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Yüzen Yönetim Paneli",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Oyunların içindeyken flood başlatıp durdurmak için ekran üstü konsolu aktifleştirin.",
                        color = Color(0xFFBAC2DE),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA594FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Panel Aç"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paneli Başlat")
                        }

                        Button(
                            onClick = {
                                val intent = Intent(context, ControlPanelOverlayService::class.java)
                                context.stopService(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF38BA8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Panel Kapat"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paneli Kapat")
                        }
                    }
                }
            }

            // SECTION 3: ADD NEW CONFIGURATION TEMPLATE
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C26)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Yeni Flood Şablonu Ekle",
                        color = Color(0xFFA594FD),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = configTitle,
                        onValueChange = { viewModel.codeConfigTitle.value = it },
                        label = { Text("Şablon Başlığı (Örn: Clash Of Clans)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA594FD),
                            unfocusedBorderColor = Color(0xFF313244),
                            focusedLabelColor = Color(0xFFA594FD),
                            unfocusedLabelColor = Color.LightGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = configMessage,
                        onValueChange = { viewModel.codeConfigMessage.value = it },
                        label = { Text("Spamlanacak Mesaj İçeriği") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA594FD),
                            unfocusedBorderColor = Color(0xFF313244),
                            focusedLabelColor = Color(0xFFA594FD),
                            unfocusedLabelColor = Color.LightGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
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
                                focusedBorderColor = Color(0xFFA594FD),
                                unfocusedBorderColor = Color(0xFF313244),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
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
                                focusedBorderColor = Color(0xFFA594FD),
                                unfocusedBorderColor = Color(0xFF313244),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
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
                        color = Color.White,
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
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA594FD))
                        )
                        Column {
                            Text(
                                "Metin Alanına Yazdır (Erişilebilirlik)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Aktif odaklı klavye kutusunu bulur ve yazıyı enjekte eder.",
                                color = Color.Gray,
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
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA594FD))
                        )
                        Column {
                            Text(
                                "Fiziksel Tıklamayı Simüle Et (Oyun Modu)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Belirlediğiniz koordinatlara basarak klavye ve gönderimi tetikler.",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Expand coordinator coordinates inputs if game mode trigger is configured
                    if (mode == "AUTO_CLICK") {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF313244))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Target A setup switch and absolute coordinates UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Hedef A'yı Kullan (Yazı Alanı Konumu)",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = useTargetA,
                                onCheckedChange = { viewModel.codeUseTargetA.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA594FD))
                            )
                        }

                        if (useTargetA) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = targetAX,
                                    onValueChange = { viewModel.codeTargetAX.value = it },
                                    label = { Text("A_X Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA594FD)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                OutlinedTextField(
                                    value = targetAY,
                                    onValueChange = { viewModel.codeTargetAY.value = it },
                                    label = { Text("A_Y Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA594FD)),
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
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = useTargetB,
                                onCheckedChange = { viewModel.codeUseTargetB.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFA594FD))
                            )
                        }

                        if (useTargetB) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = targetBX,
                                    onValueChange = { viewModel.codeTargetBX.value = it },
                                    label = { Text("B_X Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA594FD)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                OutlinedTextField(
                                    value = targetBY,
                                    onValueChange = { viewModel.codeTargetBY.value = it },
                                    label = { Text("B_Y Koordinatı") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFA594FD)),
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
                                .background(Color(0xFF27293F), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Yardım",
                                tint = Color(0xFFF9E2AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "İpucu: Geliştirici Seçenekleri -> 'İşaretçi Konumu' ayarını açarak oyununuzdaki butonların tam X ve Y koordinatını kolayca bulabilirsiniz.",
                                color = Color(0xFFCDD6F4),
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.saveConfig() },
                        enabled = configTitle.isNotBlank() && configMessage.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA594FD)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Ekle")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Şablonu Kaydet", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SECTION 4: CONFIGURED SPAM TEMPLATES LIST
            Text(
                text = "Kaydedilmiş Şablonlar",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (configs.isEmpty()) {
                Text(
                    text = "Kayıtlı şablon bulunmamaktadır.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                configs.forEach { config ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1C26)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = config.title,
                                    color = Color.White,
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
                                        tint = Color(0xFFF38BA8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = config.message,
                                color = Color(0xFFC6CCE0),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF313244))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${config.intervalMs}ms",
                                        color = Color(0xFFA6E3A1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF25392D),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (config.repeatCount == 0) "Sonsuz" else "${config.repeatCount} Adet",
                                        color = Color(0xFFF9E2AF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF3A3626),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (config.mode == "AUTO_CLICK") "Oyun" else "Metin",
                                        color = Color(0xFF89B4FA),
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .background(
                                                Color(0xFF202A3C),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Quick launch start button
                                Button(
                                    onClick = {
                                        if (isAccessibilityGranted) {
                                            val intent = Intent(context, SpamAccessibilityService::class.java).apply {
                                                action = SpamAccessibilityService.ACTION_START_FLOOD
                                                putExtra(SpamAccessibilityService.EXTRA_CONFIG_ID, config.id)
                                            }
                                            context.startService(intent)
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF45475A)),
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Başlat",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hızlı Başlat", color = Color.White, fontSize = 10.sp)
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
                Text(
                    text = "Gönderim Günlüğü (Veritabanı)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Temizle",
                    color = Color(0xFFF38BA8),
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
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(logs) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatTimestamp(log.timestamp),
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = log.configTitle,
                                            color = Color(0xFFCBA6F7),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.message,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        lineHeight = 13.sp
                                    )
                                    if (log.errorMessage != null) {
                                        Text(
                                            text = log.errorMessage,
                                            color = Color(0xFFF38BA8),
                                            fontSize = 9.sp,
                                            lineHeight = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                // Success status badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (log.status == "SUCCESS") Color(0xFF25392D) else Color(0xFF4F2735))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (log.status == "SUCCESS") "OK" else "HATA",
                                        color = if (log.status == "SUCCESS") Color(0xFFA6E3A1) else Color(0xFFF38BA8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFF1E1E2E), modifier = Modifier.padding(vertical = 4.dp))
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
            .background(Color(0xFF252634), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFFA6E3A1) else Color(0xFFF38BA8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFFB4BEFE),
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Button(
            onClick = onGrantClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isGranted) Color(0xFF313244) else Color(0xFFA594FD)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Text(
                text = if (isGranted) "Aktif" else "Etkinleştir",
                color = if (isGranted) Color.Gray else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
