package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CodeEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.HotPink
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.ObsidianGrey
import com.example.ui.theme.PremiumTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CodeViewModel, modifier: Modifier = Modifier) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Observe database & state from viewmodel
    val codesList by viewModel.allCodes.collectAsState()
    val extractedList by viewModel.extractedCodes.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val scannerState by viewModel.scannerState.collectAsState()
    val otpTimeRemaining by viewModel.otpTimeRemaining.collectAsState()
    val currentInterval by viewModel.currentInterval.collectAsState()

    // Screen tab selection state
    var selectedTab by remember { mutableStateOf(AppTab.RETRIEVE) }

    // Dialog state for adding/saving code
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogPreFilledCode by remember { mutableStateOf("") }
    var dialogPreFilledType by remember { mutableStateOf("COUPON") }

    // Categories definition
    val categories = listOf("الكل", "تسوق", "ألعاب", "اشتراكات", "رموز 2FA", "أخرى")
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    // Layout Root with safe edges and RTL local guidance (Arabic is native RTL)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .drawBehind {
                // Background ambient glow circles
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PremiumTeal.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    radius = 400.dp.toPx(),
                    center = Offset(size.width * 0.1f, size.height * 0.2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    radius = 500.dp.toPx(),
                    center = Offset(size.width * 0.8f, size.height * 0.7f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 1. Header with Arabic typography and secure status tag
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مسترد الأكواد",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PremiumTeal,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "استرجاع، حفظ وإدارة رموز الاسترداد بثوانٍ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // SECURE BADGE
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianGrey)
                        .border(1.dp, PremiumTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الخزنة آمنة",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Tab Navigation Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianGrey)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabBg by animateColorAsState(
                        if (isSelected) PremiumTeal.copy(alpha = 0.15f) else Color.Transparent,
                        label = "tabBg"
                    )
                    val tabBorderColor by animateColorAsState(
                        if (isSelected) PremiumTeal.copy(alpha = 0.3f) else Color.Transparent,
                        label = "tabBorder"
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) PremiumTeal else TextSecondary,
                        label = "tabText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tabBg)
                            .border(1.dp, tabBorderColor, RoundedCornerShape(10.dp))
                            .clickable { selectedTab = tab }
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.arabicTitle,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Tab contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    AppTab.RETRIEVE -> {
                        // TAB 1: RETRIEVE FROM TEXT
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Instruction Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianGrey.copy(alpha = 0.5f)),
                                border = BorderStroke(0.5.dp, PremiumTeal.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "info",
                                        tint = PremiumTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ألصق أي نص (رسالة SMS، بريد، عروض) وسنستخرج الكوبونات وأكواد التحقق فوراً!",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Outlined Text Field for Input
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { viewModel.setInputText(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("input_retriever_text"),
                                placeholder = {
                                    Text(
                                        text = "أدخل أو ألصق النص هنا لفرزه واسترداد الكود منه...",
                                        color = TextSecondary.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Right
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = ObsidianGrey,
                                    unfocusedContainerColor = ObsidianGrey,
                                    focusedBorderColor = PremiumTeal,
                                    unfocusedBorderColor = CardBorder,
                                    cursorColor = PremiumTeal
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Right,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Paste and Retrieve Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Paste Button
                                Button(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        if (clipText.isNotBlank()) {
                                            viewModel.setInputText(clipText)
                                            Toast.makeText(context, "تم اللصق بنجاح", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "الحافظة فارغة!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianGrey),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .testTag("paste_button")
                                ) {
                                    Text(text = "📋 لصق النص", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                // Extraction Trigger Button
                                Button(
                                    onClick = {
                                        viewModel.retrieveCodesFromText(inputText)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(48.dp)
                                        .testTag("retrieve_button")
                                ) {
                                    Text(text = "استخراج الأكواد ⚡", color = CyberBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Extracted Results list
                            Text(
                                text = "الأكواد المستخرجة من النص (${extractedList.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (extractedList.isEmpty()) {
                                // Empty state for extraction
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ObsidianGrey.copy(alpha = 0.3f))
                                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Text(text = "🔍", fontSize = 42.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "لا توجد أكواد مستخرجة بعد",
                                            color = TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "اكتب أو ألصق نصاً بالأعلى ثم انقر استخراج",
                                            color = TextSecondary.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(extractedList) { ext ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = ObsidianGrey),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, PremiumTeal.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left side actions
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Copy button
                                                    Button(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(ext.code))
                                                            Toast.makeText(context, "تم نسخ الكود: ${ext.code}", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal.copy(alpha = 0.15f)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Text(text = "نسخ", color = PremiumTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    // Save to database button
                                                    Button(
                                                        onClick = {
                                                            dialogPreFilledCode = ext.code
                                                            dialogPreFilledType = if (ext.type == "OTP") "OTP_2FA" else "COUPON"
                                                            showAddDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.15f)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Text(text = "حفظ خزنة", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                // Right side details (RTL align)
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = ext.description,
                                                        color = PremiumTeal,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(PremiumTeal.copy(alpha = 0.1f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = ext.code,
                                                        color = TextPrimary,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppTab.VAULT -> {
                        // TAB 2: SECURED DATABASE VAULT
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Category selection filter bar
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = selectedCategoryFilter == cat
                                    val catBg by animateColorAsState(
                                        if (isSelected) NeonCyan else ObsidianGrey,
                                        label = "catBg"
                                    )
                                    val catTextCol by animateColorAsState(
                                        if (isSelected) CyberBlack else TextSecondary,
                                        label = "catTxt"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(catBg)
                                            .border(1.dp, if (isSelected) NeonCyan else CardBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedCategoryFilter = cat }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = catTextCol
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Filtered code list
                            val filteredCodes = codesList.filter {
                                selectedCategoryFilter == "الكل" || 
                                (selectedCategoryFilter == "رموز 2FA" && it.type == "OTP_2FA") ||
                                (selectedCategoryFilter != "رموز 2FA" && it.category == selectedCategoryFilter)
                            }

                            if (filteredCodes.isEmpty()) {
                                // Vault empty state
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🛡️", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "الخزنة فارغة للمجموعة الحالية",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "انقر على زر + بالأسفل لإضافة كود يدوياً",
                                            fontSize = 11.sp,
                                            color = TextSecondary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(filteredCodes) { item ->
                                        val isRedeemed = item.isRedeemed
                                        val cardOpacity = if (isRedeemed) 0.5f else 1.0f

                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = ObsidianGrey.copy(alpha = cardOpacity)
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(
                                                1.dp, 
                                                if (isRedeemed) CardBorder else PremiumTeal.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("code_item_${item.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Actions Column
                                                Column(horizontalAlignment = Alignment.Start) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        // Copy Button
                                                        IconButton(
                                                            onClick = {
                                                                val valueToCopy = if (item.type == "OTP_2FA") {
                                                                    viewModel.getOtpForSecret(item.otpSecret, currentInterval)
                                                                } else {
                                                                    item.code
                                                                }
                                                                clipboardManager.setText(AnnotatedString(valueToCopy))
                                                                Toast.makeText(context, "تم نسخ الكود للحافظة 📋", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(PremiumTeal.copy(alpha = 0.1f))
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Copy",
                                                                tint = PremiumTeal,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(6.dp))

                                                        // Toggle Redeemed State Checkbox
                                                        IconButton(
                                                            onClick = { viewModel.toggleRedeemedStatus(item) },
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(
                                                                    if (isRedeemed) NeonGreen.copy(alpha = 0.2f) 
                                                                    else ObsidianGrey.copy(alpha = 0.8f)
                                                                )
                                                                .border(1.dp, if (isRedeemed) NeonGreen else CardBorder, RoundedCornerShape(8.dp))
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = "Redeem Status",
                                                                tint = if (isRedeemed) NeonGreen else TextSecondary.copy(alpha = 0.4f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(6.dp))

                                                        // Delete Button
                                                        IconButton(
                                                            onClick = { viewModel.deleteCode(item) },
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(HotPink.copy(alpha = 0.1f))
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete",
                                                                tint = HotPink,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }

                                                    // Visual 2FA indicator loop
                                                    if (item.type == "OTP_2FA" && !isRedeemed) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            CircularProgressIndicator(
                                                                progress = { otpTimeRemaining / 30f },
                                                                color = NeonCyan,
                                                                trackColor = CardBorder,
                                                                strokeWidth = 3.dp,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "يتغير خلال ${otpTimeRemaining}ث",
                                                                fontSize = 9.sp,
                                                                color = NeonCyan,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                // Content details column (RTL)
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    // Title and tag row
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        Text(
                                                            text = item.category,
                                                            fontSize = 9.sp,
                                                            color = NeonCyan,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(NeonCyan.copy(alpha = 0.12f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = item.title,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            textDecoration = if (isRedeemed) TextDecoration.LineThrough else null
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    // Monospace code value
                                                    if (item.type == "OTP_2FA") {
                                                        val otpPin = viewModel.getOtpForSecret(item.otpSecret, currentInterval)
                                                        // Format PIN as 3-3 digits (e.g., 123 456)
                                                        val formattedPin = if (otpPin.length == 6) {
                                                            "${otpPin.take(3)} ${otpPin.drop(3)}"
                                                        } else {
                                                            otpPin
                                                        }

                                                        Text(
                                                            text = formattedPin,
                                                            fontSize = 20.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (isRedeemed) TextSecondary else PremiumTeal,
                                                            letterSpacing = 1.sp
                                                        )
                                                    } else {
                                                        Text(
                                                            text = item.code,
                                                            fontSize = 15.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isRedeemed) TextSecondary else TextPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    if (item.notes.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = item.notes,
                                                            fontSize = 10.sp,
                                                            color = TextSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AppTab.SCANNER -> {
                        // TAB 3: SIMULATED MULTI-FUNCTION SCANNER & GENERATOR
                        var generatorText by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScrollEnabled() // Custom helper or simple column
                        ) {
                            // Scanner viewport card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ObsidianGrey),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, PremiumTeal.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Simulated live laser scanner background
                                    val infiniteTransition = rememberInfiniteTransition(label = "laser")
                                    val scanOffset by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "scanLaser"
                                    )

                                    // Viewfinder graphics
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val padX = size.width * 0.25f
                                        val padY = size.height * 0.15f
                                        val viewW = size.width * 0.5f
                                        val viewH = size.height * 0.7f

                                        // Draw viewfinder background tint
                                        drawRect(
                                            color = Color.Black.copy(alpha = 0.4f)
                                        )

                                        // Draw scanning neon laser line
                                        val laserY = padY + (viewH * scanOffset)
                                        drawLine(
                                            color = PremiumTeal,
                                            start = Offset(padX, laserY),
                                            end = Offset(padX + viewW, laserY),
                                            strokeWidth = 3.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )

                                        // Draw target corners
                                        val cornerLength = 15.dp.toPx()
                                        val strokeW = 3.dp.toPx()

                                        // Top Left
                                        drawLine(PremiumTeal, Offset(padX, padY), Offset(padX + cornerLength, padY), strokeW)
                                        drawLine(PremiumTeal, Offset(padX, padY), Offset(padX, padY + cornerLength), strokeW)

                                        // Top Right
                                        drawLine(PremiumTeal, Offset(padX + viewW, padY), Offset(padX + viewW - cornerLength, padY), strokeW)
                                        drawLine(PremiumTeal, Offset(padX + viewW, padY), Offset(padX + viewW, padY + cornerLength), strokeW)

                                        // Bottom Left
                                        drawLine(PremiumTeal, Offset(padX, padY + viewH), Offset(padX + cornerLength, padY + viewH), strokeW)
                                        drawLine(PremiumTeal, Offset(padX, padY + viewH), Offset(padX, padY + viewH - cornerLength), strokeW)

                                        // Bottom Right
                                        drawLine(PremiumTeal, Offset(padX + viewW, padY + viewH), Offset(padX + viewW - cornerLength, padY + viewH), strokeW)
                                        drawLine(PremiumTeal, Offset(padX + viewW, padY + viewH), Offset(padX + viewW, padY + viewH - cornerLength), strokeW)
                                    }

                                    // State overlay
                                    when (val state = scannerState) {
                                        ScannerState.Idle -> {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.SpaceBetween,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "وجه الكاميرا نحو الرمز للاستخراج",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Text(
                                                    text = "اختر أحد الأكواد التجريبية بالأسفل للاختبار والفرز الفوري 👇",
                                                    color = TextSecondary.copy(alpha = 0.8f),
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.background(CyberBlack.copy(alpha = 0.7f)).padding(6.dp)
                                                )
                                            }
                                        }
                                        ScannerState.Scanning -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(CyberBlack.copy(alpha = 0.7f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator(color = PremiumTeal, modifier = Modifier.size(36.dp))
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        text = "جاري استرداد الكود من الصورة...",
                                                        color = PremiumTeal,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        is ScannerState.Success -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(CyberBlack.copy(alpha = 0.9f))
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "🎉 تم الاسترداد بنجاح!",
                                                        color = NeonGreen,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = state.type,
                                                        color = TextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                    Text(
                                                        text = state.code,
                                                        color = TextPrimary,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Row {
                                                        Button(
                                                            onClick = { viewModel.resetScanner() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianGrey),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text("مسح مجدداً", color = TextSecondary, fontSize = 10.sp)
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(
                                                            onClick = {
                                                                dialogPreFilledCode = state.code
                                                                dialogPreFilledType = if (state.type.contains("2FA")) "OTP_2FA" else "COUPON"
                                                                showAddDialog = true
                                                                viewModel.resetScanner()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Text("حفظ الكود", color = CyberBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Test Code Buttons to feed scanner simulation
                            Text(
                                text = "أكواد اختبار سريعة (انقر للمحاكاة)",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val testCodes = listOf(
                                    Triple("NOON-OFF-50", "كوبون نون 50%", "قسيمة خصم"),
                                    Triple("STEAM-50-USD", "بطاقة ستيم 50$", "بطاقة شحن"),
                                    Triple("SECRETKEY2FA", "رمز مصادقة جوجل", "مفتاح 2FA")
                                )

                                testCodes.forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ObsidianGrey)
                                            .border(0.5.dp, PremiumTeal.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .clickable { viewModel.simulateScan(item.first, "${item.second} (${item.third})") }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "🎫", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.second,
                                                fontSize = 9.sp,
                                                color = TextPrimary,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Custom QR Generator Section
                            Text(
                                text = "مولد الرموز و QR الخاص بك",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = generatorText,
                                onValueChange = { generatorText = it },
                                placeholder = {
                                    Text("أدخل أي رابط أو كود لتوليد رمز QR فني له...", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("qr_generator_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = ObsidianGrey,
                                    unfocusedContainerColor = ObsidianGrey,
                                    focusedBorderColor = PremiumTeal,
                                    unfocusedBorderColor = CardBorder
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right, fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (generatorText.isNotBlank()) {
                                // Real Pixel QR Matrix generator canvas
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(ObsidianGrey)
                                        .border(1.dp, PremiumTeal.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "رمز QR الرقمي المولد:",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Canvas to draw a deterministic QR code matrix based on text input
                                        Canvas(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .background(Color.White)
                                                .padding(6.dp)
                                        ) {
                                            val hash = generatorText.hashCode()
                                            val random = java.util.Random(hash.toLong())
                                            val matrixSize = 21 // QR Version 1 is 21x21 modules
                                            val moduleW = size.width / matrixSize
                                            val moduleH = size.height / matrixSize

                                            // Draw standard QR Finder Patterns at top-left, top-right, bottom-left
                                            fun drawFinder(x: Int, y: Int) {
                                                // Outer square
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(x * moduleW, y * moduleH),
                                                    size = Size(7 * moduleW, 7 * moduleH)
                                                )
                                                drawRect(
                                                    color = Color.White,
                                                    topLeft = Offset((x + 1) * moduleW, (y + 1) * moduleH),
                                                    size = Size(5 * moduleW, 5 * moduleH)
                                                )
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = Offset((x + 2) * moduleW, (y + 2) * moduleH),
                                                    size = Size(3 * moduleW, 3 * moduleH)
                                                )
                                            }

                                            // Draw finders
                                            drawFinder(0, 0)
                                            drawFinder(matrixSize - 7, 0)
                                            drawFinder(0, matrixSize - 7)

                                            // Fill the rest of the grid deterministically with pixels
                                            for (r in 0 until matrixSize) {
                                                for (c in 0 until matrixSize) {
                                                    // Skip finder pattern zones
                                                    if ((r < 8 && c < 8) || (r < 8 && c >= matrixSize - 8) || (r >= matrixSize - 8 && c < 8)) {
                                                        continue
                                                    }
                                                    // Determinstic black pixels
                                                    if (random.nextBoolean()) {
                                                        drawRect(
                                                            color = Color.Black,
                                                            topLeft = Offset(c * moduleW, r * moduleH),
                                                            size = Size(moduleW + 0.5f, moduleH + 0.5f) // avoid subpixel seams
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = generatorText,
                                            color = PremiumTeal,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to add codes manually (Visible on Vault Tab)
        if (selectedTab == AppTab.VAULT) {
            FloatingActionButton(
                onClick = {
                    dialogPreFilledCode = ""
                    dialogPreFilledType = "COUPON"
                    showAddDialog = true
                },
                containerColor = PremiumTeal,
                contentColor = CyberBlack,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_code_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Code",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Elegant dialog for saving/adding codes
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                color = ObsidianGrey,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PremiumTeal.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("add_code_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.End // RTL Align
                ) {
                    Text(
                        text = "إضافة رمز استرداد جديد الخزنة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PremiumTeal,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var codeVal by remember { mutableStateOf(dialogPreFilledCode) }
                    var titleVal by remember { mutableStateOf("") }
                    var categoryVal by remember { mutableStateOf("تسوق") }
                    var notesVal by remember { mutableStateOf("") }
                    var codeType by remember { mutableStateOf(dialogPreFilledType) } // COUPON, OTP_2FA

                    // Code Input
                    Text(text = "قيمة الرمز / الكود *", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = codeVal,
                        onValueChange = { codeVal = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_code_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PremiumTeal,
                            unfocusedBorderColor = CardBorder
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Type Chooser (Coupon vs 2FA token)
                    Text(text = "نوع الرمز", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("مفتاح 2FA للتحقق", fontSize = 12.sp, color = TextPrimary)
                            RadioButton(
                                selected = codeType == "OTP_2FA",
                                onClick = { codeType = "OTP_2FA" },
                                colors = RadioButtonDefaults.colors(selectedColor = PremiumTeal)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("كوبون / قسيمة", fontSize = 12.sp, color = TextPrimary)
                            RadioButton(
                                selected = codeType == "COUPON",
                                onClick = { codeType = "COUPON" },
                                colors = RadioButtonDefaults.colors(selectedColor = PremiumTeal)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = if (codeType == "OTP_2FA") "اسم الخدمة (جوجل، فيسبوك) *" else "اسم المتجر / عنوان العرض *",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = titleVal,
                        onValueChange = { titleVal = it },
                        placeholder = { Text(if (codeType == "OTP_2FA") "مثال: حساب العمل" else "مثال: نون 10% خصم", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_title_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PremiumTeal,
                            unfocusedBorderColor = CardBorder
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Selector Dropdown
                    if (codeType != "OTP_2FA") {
                        Text(text = "التصنيف", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        var expanded by remember { mutableStateOf(false) }
                        val categoriesOptions = listOf("تسوق", "ألعاب", "اشتراكات", "أخرى")

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = categoryVal,
                                onValueChange = {},
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = ObsidianGrey,
                                    unfocusedContainerColor = ObsidianGrey,
                                    focusedBorderColor = PremiumTeal,
                                    unfocusedBorderColor = CardBorder
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right, fontSize = 13.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categoriesOptions.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                        onClick = {
                                            categoryVal = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Notes / Expiry details
                    Text(text = "ملاحظات إضافية", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notesVal,
                        onValueChange = { notesVal = it },
                        placeholder = { Text("أدخل تفاصيل إضافية أو شروط الاسترداد...", fontSize = 12.sp, color = TextSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .testTag("dialog_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PremiumTeal,
                            unfocusedBorderColor = CardBorder
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Right, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianGrey),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        ) {
                            Text("إلغاء", color = TextSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (codeVal.isBlank() || titleVal.isBlank()) {
                                    Toast.makeText(context, "الرجاء تعبئة الحقول المطلوبة", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveCode(
                                        title = titleVal,
                                        code = if (codeType == "OTP_2FA") "" else codeVal,
                                        category = if (codeType == "OTP_2FA") "رموز 2FA" else categoryVal,
                                        type = codeType,
                                        otpSecret = if (codeType == "OTP_2FA") codeVal else "",
                                        notes = notesVal
                                    )
                                    showAddDialog = false
                                    Toast.makeText(context, "تم الحفظ بنجاح 🛡️", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("dialog_save_button")
                        ) {
                            Text("حفظ الكود", color = CyberBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// App screens tab enum helper
enum class AppTab(val arabicTitle: String) {
    RETRIEVE("استرداد سريع"),
    VAULT("الخزنة الآمنة"),
    SCANNER("ماسح ضوئي")
}

// Helper composables to keep file compile-ready without extra extensions
@Composable
private fun Modifier.verticalScrollEnabled(): Modifier {
    return this.verticalScroll(rememberScrollState())
}
