package com.example.service

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DangerLevel
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TextGold
import com.example.ui.theme.VictoryGreen

@Composable
fun CoachOverlayScreen(
    transparencyAlpha: Float = 0.94f,
    onCloseClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleVoiceMute: () -> Unit
) {
    val tacticalState by CoachStateHub.tacticalState.collectAsState()
    val isExpanded by CoachStateHub.isOverlayExpanded.collectAsState()

    val dangerColor = when (tacticalState.dangerLevel) {
        DangerLevel.SAFE -> VictoryGreen
        DangerLevel.LOW -> ArcaneCyan
        DangerLevel.MEDIUM -> ImperialGold
        DangerLevel.HIGH -> AlertOrange
        DangerLevel.CRITICAL -> DefeatRed
    }

    val winColor = when {
        tacticalState.winProbability >= 55 -> VictoryGreen
        tacticalState.winProbability <= 45 -> DefeatRed
        else -> ImperialGold
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF091224).copy(alpha = transparencyAlpha),
            Color(0xFF060A14).copy(alpha = transparencyAlpha)
        )
    )

    Box(
        modifier = Modifier
            .widthIn(min = 210.dp, max = 330.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bgGradient)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        ImperialGold.copy(alpha = 0.85f),
                        ArcaneCyan.copy(alpha = 0.6f),
                        Color(0xFF0F1E38)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = ImperialGold)
            .padding(10.dp)
            .testTag("coach_floating_overlay_root")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thanh Tiêu Đề Esports Kéo Thả
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onToggleExpand() }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(dangerColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRỢ LÝ LIÊN QUÂN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = ImperialGoldLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tacticalState.formattedTime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = ArcaneCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nút Bật / Tắt Giọng Nói
                    IconButton(
                        onClick = onToggleVoiceMute,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("overlay_voice_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (tacticalState.isVoiceMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Bật/Tắt Giọng Nói",
                            tint = if (tacticalState.isVoiceMuted) Color.Gray else ArcaneCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Nút Thu Gọn / Mở Rộng
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("overlay_expand_collapse_btn")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Thu gọn/Mở rộng",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Nút Đóng HUD
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("overlay_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng Trợ Lý",
                            tint = DefeatRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Chế độ Thu Gọn (1 Dòng Mini HUD)
            if (!isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THẮNG: ${tacticalState.winProbability}%",
                        color = winColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = tacticalState.currentObjective.displayName,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Chế độ Đầy Đủ (Tactical Esports HUD)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    // Thanh Tỉ Lệ Thắng + Chênh Lệch Vàng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TỈ LỆ THẮNG: ${tacticalState.winProbability}%",
                            color = winColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = tacticalState.formattedGoldDiff,
                            color = if (tacticalState.teamGoldDiff >= 0) VictoryGreen else DefeatRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { tacticalState.winProbability / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = winColor,
                        trackColor = Color(0x40000000)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Thẻ Mục Tiêu Ưu Tiên
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D1B33))
                            .border(1.dp, ImperialGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Mục Tiêu",
                                tint = ImperialGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "MỤC TIÊU ƯU TIÊN:",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ImperialGoldLight,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = tacticalState.currentObjective.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cảnh Báo Nguy Hiểm & Bị Gank
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(dangerColor.copy(alpha = 0.15f))
                            .border(1.dp, dangerColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh Báo",
                                tint = dangerColor,
                                modifier = Modifier
                                    .size(15.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "CẢNH BÁO: (${tacticalState.dangerLevel.labelVi.uppercase()})",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dangerColor,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = tacticalState.dangerWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Lời Khuyên Giao Tranh
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LỜI KHUYÊN CHIẾN THUẬT:",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = ArcaneCyan,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = tacticalState.teamfightAdvice,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )

                    // Gợi Ý Trang Bị Khắc Chế
                    val topCounter = tacticalState.itemRecommendations.firstOrNull { it.isCounter }
                    if (topCounter != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF261836))
                                .border(1.dp, ImperialGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Trang Bị",
                                tint = ImperialGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lên: ${topCounter.itemName} (${topCounter.reason})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGoldLight,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
