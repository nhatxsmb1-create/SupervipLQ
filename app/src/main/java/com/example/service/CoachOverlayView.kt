package com.example.service

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debug.DeveloperDebugHudCard
import com.example.model.CoachStatus
import com.example.model.DangerLevel
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.VictoryGreen

@Composable
fun CoachOverlayScreen(
    transparencyAlpha: Float = 0.94f,
    onCloseClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleVoiceMute: () -> Unit,
    onDrag: (dx: Int, dy: Int) -> Unit = { _, _ -> }
) {
    val tacticalState by CoachStateHub.tacticalState.collectAsState()
    val debugState by CoachStateHub.debugState.collectAsState()
    val isExpanded by CoachStateHub.isOverlayExpanded.collectAsState()

    val isInMatch = tacticalState.coachStatus == CoachStatus.IN_MATCH_READY ||
            tacticalState.coachStatus == CoachStatus.IN_MATCH_ANALYZING

    val dangerColor = when (tacticalState.dangerLevel) {
        DangerLevel.SAFE -> VictoryGreen
        DangerLevel.LOW -> ArcaneCyan
        DangerLevel.MEDIUM -> ImperialGold
        DangerLevel.HIGH -> AlertOrange
        DangerLevel.CRITICAL -> DefeatRed
    }

    val winProb = tacticalState.winProbability ?: 50
    val winColor = when {
        winProb >= 55 -> VictoryGreen
        winProb <= 45 -> DefeatRed
        else -> ImperialGold
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF091428).copy(alpha = transparencyAlpha),
            Color(0xFF060B14).copy(alpha = transparencyAlpha)
        )
    )

    Box(
        modifier = Modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgGradient)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        if (isInMatch) ImperialGold.copy(alpha = 0.8f) else ArcaneCyan.copy(alpha = 0.6f),
                        Color(0xFF1E3A5F),
                        Color(0xFF0F1E38)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = if (isInMatch) ImperialGold else ArcaneCyan)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("coach_floating_overlay_root")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thanh Tiêu Đề Esports Kéo Thả (Gọn gàng, không đè chữ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x.toInt(), dragAmount.y.toInt())
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bên Trái: Chấm Trạng Thái + Tiêu Đề + Đồng Hồ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleExpand() }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isInMatch) dangerColor else VictoryGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isInMatch) "AI COACH" else "TRỢ LÝ LIÊN QUÂN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = ImperialGoldLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isInMatch && tacticalState.formattedTime != "--:--") {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = tacticalState.formattedTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ArcaneCyan
                        )
                    }
                }

                // Bên Phải: Nhóm Phím Tắt Tiện Ích
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // Nút Quét / Phân Tích Màn Hình Tức Thì
                    IconButton(
                        onClick = {
                            LiveCoachService.triggerInstantScan()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("overlay_snap_scan_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Phân tích nhanh",
                            tint = ImperialGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Nút Bật / Tắt Giọng Nói
                    IconButton(
                        onClick = onToggleVoiceMute,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("overlay_voice_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (tacticalState.isVoiceMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Bật/Tắt Giọng Nói",
                            tint = if (tacticalState.isVoiceMuted) Color.Gray else ArcaneCyan,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Nút Thu Gọn / Mở Rộng
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("overlay_expand_collapse_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Thu gọn/Mở rộng",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Nút Đóng HUD
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("overlay_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng Trợ Lý",
                            tint = DefeatRed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Chế độ Thu Gọn (1 Dòng Mini HUD Tinh Gọn)
            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!isInMatch) "🟢 Chờ vào trận..." else "⏱ ${tacticalState.formattedTime}",
                        color = if (isInMatch) ArcaneCyan else VictoryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isInMatch) (tacticalState.currentObjective?.displayName ?: "54% Thắng 🏆") else "Mở Liên Quân",
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Chế độ Đầy Đủ (Hiển thị thông minh theo trạng thái thực tế)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp)
                ) {
                    if (!isInMatch) {
                        // TRẠNG THÁI: NGOÀI TRẬN ĐẤU (Gọn gàng, đơn giản, không số liệu giả)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F1E38))
                                .border(1.dp, ArcaneCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Trạng thái",
                                        tint = VictoryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = tacticalState.coachStatus.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ImperialGoldLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mở Liên Quân Mobile để AI tự động phát hiện trận đấu & phân tích chiến thuật thời gian thực.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    } else {
                        // TRẠNG THÁI: ĐANG TRONG TRẬN ĐẤU (Số liệu thực chiến gọn gàng)
                        
                        // 1. Thanh Tỉ Lệ Thắng & Chênh Lệch Vàng
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TỈ LỆ THẮNG: ${tacticalState.formattedWinRate}",
                                color = winColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = tacticalState.formattedGoldDiff,
                                color = if (tacticalState.teamGoldDiff >= 0) VictoryGreen else DefeatRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { (tacticalState.winProbability ?: 50) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = winColor,
                            trackColor = Color(0x40000000)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. Thẻ Chiến Thuật Trọng Tâm (Mục tiêu + Cảnh báo + Scoreboard/Shop kết hợp)
                        val isScoreboard = tacticalState.detectedUIMode == com.example.model.DetectedScreenMode.SCOREBOARD_OPEN
                        val isShop = tacticalState.detectedUIMode == com.example.model.DetectedScreenMode.SHOP_OPEN

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isScoreboard) Color(0xFF132238) else if (isShop) Color(0xFF241C10) else Color(0xFF0D1B33))
                                .border(1.dp, if (isScoreboard) ArcaneCyan else if (isShop) ImperialGold else dangerColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                if (isScoreboard) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "📊 PHÂN TÍCH ĐỘI HÌNH ĐỊCH",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ArcaneCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                } else if (isShop) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🛒 PHÂN TÍCH KHO ĐỒ & MUA SẮM",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ImperialGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                } else if (tacticalState.currentObjective != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "Mục tiêu",
                                            tint = ImperialGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tacticalState.currentObjective?.displayName ?: "",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ImperialGoldLight,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                }

                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Cảnh báo",
                                        tint = if (isScoreboard) ArcaneCyan else if (isShop) ImperialGold else dangerColor,
                                        modifier = Modifier
                                            .size(13.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tacticalState.dangerWarning.ifBlank { "Không có nguy hiểm cận kề" },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }

                        // 3. Gợi Ý Lên Đồ Khắc Chế Nhanh
                        val topCounter = tacticalState.itemRecommendations.firstOrNull { it.isCounter }
                        if (topCounter != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🛡️ Lên đồ: ${topCounter.itemName} (${topCounter.reason})",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (debugState.isEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                DeveloperDebugHudCard(debugState = debugState)
            }
        }
    }
}
