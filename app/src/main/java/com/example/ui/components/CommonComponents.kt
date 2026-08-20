package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DangerLevel
import com.example.model.TacticalState
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.ArcaneCyanGlow
import com.example.ui.theme.ArcaneIndigo
import com.example.ui.theme.CyanBorder
import com.example.ui.theme.CyanEnergyGradient
import com.example.ui.theme.DangerGlowGradient
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsGlassBg
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.EsportsSurfaceHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlowGradient
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldDark
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.ManaPurple
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VictoryGlowGradient
import com.example.ui.theme.VictoryGreen

@Composable
fun EsportsHeroBanner(
    isServiceRunning: Boolean,
    onLaunchClick: () -> Unit,
    onStopClick: () -> Unit,
    onSimulateClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        ImperialGold.copy(alpha = glowAlpha),
                        ArcaneCyan.copy(alpha = 0.6f),
                        ImperialGoldDark.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = ImperialGold)
            .testTag("hero_banner_card")
    ) {
        // Ảnh nền Battlefield Esports AAA
        Image(
            painter = painterResource(id = R.drawable.aov_hero_bg_1787143660079),
            contentDescription = "Đấu Trường Liên Quân",
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentScale = ContentScale.Crop
        )

        // Lớp phủ Gradient Tối & Đậm chất Esports Cinema
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99060A14),
                            Color(0xE6081124),
                            Color(0xFC060A14)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Thanh Trạng Thái Trợ Lý + Huy Hiệu Giải Đấu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Huy Hiệu Trạng Thái
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isServiceRunning) Color(0x3300E676) else Color(0x33FFC837))
                        .border(
                            width = 1.dp,
                            color = if (isServiceRunning) VictoryGreen.copy(alpha = glowAlpha) else ImperialGold.copy(alpha = glowAlpha),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isServiceRunning) VictoryGreen else ImperialGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isServiceRunning) "TRỢ LÝ ĐANG HOẠT ĐỘNG" else "TRỢ LÝ SẴN SÀNG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.75.sp,
                        color = if (isServiceRunning) VictoryGreen else ImperialGold
                    )
                }

                // Huy Hiệu Esports Pro
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF0F2644), Color(0xFF1E3A66))))
                        .border(1.dp, ArcaneCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Pro",
                            tint = ArcaneCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "AI ESPORTS PRO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = ArcaneCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tiêu Đề Chính & Slogan Vàng Kim
            Text(
                text = "TRỢ LÝ LIÊN QUÂN AI",
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = TextGold
            )

            Text(
                text = "Trợ lý chiến thuật trực tiếp chuẩn tuyển thủ: Phân tích màn hình, cảnh báo đảo đường, bắt bài giao tranh và chỉ huy giọng nói.",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2 Nút Hành Động Esports
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isServiceRunning) {
                    Button(
                        onClick = onLaunchClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(GoldGlowGradient, RoundedCornerShape(12.dp))
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = ImperialGold)
                            .testTag("launch_coach_overlay_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Mở",
                                tint = Color(0xFF261900),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BẬT CỬA SỔ NỔI",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFF261900),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onStopClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(DangerGlowGradient, RoundedCornerShape(12.dp))
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = DefeatRed)
                            .testTag("stop_coach_overlay_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Dừng",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DỪNG TRỢ LÝ",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSimulateClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ArcaneCyan,
                        containerColor = Color(0x3300F0FF)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(listOf(ArcaneCyan, Color(0xFF0066CC)))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("simulate_battle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CrisisAlert,
                        contentDescription = "Mô phỏng",
                        tint = ArcaneCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MÔ PHỎNG TRẬN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EsportsTacticalRadarCard(
    tacticalState: TacticalState,
    onVoiceCalloutTest: (String) -> Unit
) {
    val dangerColor = when (tacticalState.dangerLevel) {
        DangerLevel.SAFE -> VictoryGreen
        DangerLevel.LOW -> ArcaneCyan
        DangerLevel.MEDIUM -> ImperialGold
        DangerLevel.HIGH -> AlertOrange
        DangerLevel.CRITICAL -> DefeatRed
    }

    val winColor = when {
        (tacticalState.winProbability ?: 50) >= 55 -> VictoryGreen
        (tacticalState.winProbability ?: 50) <= 45 -> DefeatRed
        else -> ImperialGold
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("tactical_radar_card")
    ) {
        Column {
            // Thanh Tiêu Đề Radar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x3300F0FF))
                            .border(1.dp, ArcaneCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawCircle(color = ArcaneCyan.copy(alpha = 0.3f), style = Stroke(width = 1.5f))
                            drawCircle(color = ArcaneCyan.copy(alpha = 0.6f), style = Stroke(width = 1.5f), radius = size.minDimension / 3)
                            drawLine(
                                color = ArcaneCyan,
                                start = center,
                                end = Offset(
                                    x = center.x + (size.minDimension / 2) * kotlin.math.cos(Math.toRadians(sweepAngle.toDouble())).toFloat(),
                                    y = center.y + (size.minDimension / 2) * kotlin.math.sin(Math.toRadians(sweepAngle.toDouble())).toFloat()
                                ),
                                strokeWidth = 2f
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RADAR CHIẾN THUẬT ESPORTS",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = tacticalState.coachStatus.displayName,
                            fontSize = 10.sp,
                            color = ArcaneCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Đồng Hồ Thời Gian Trận
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF081226))
                        .border(1.dp, ArcaneCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tacticalState.formattedTime,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = ArcaneCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tacticalState.coachStatus != com.example.model.CoachStatus.IN_MATCH_READY) {
                // Thẻ thông báo trạng thái chưa vào trận
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EsportsSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = tacticalState.coachStatus.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = ImperialGoldLight,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tacticalState.coachStatus.detailText,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // 2 Thẻ Chỉ Số Vàng: Tỉ Lệ Thắng & Chênh Lệch Vàng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Khung Tỉ Lệ Thắng
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EsportsSurfaceElevated)
                            .border(1.dp, if ((tacticalState.winProbability ?: 50) >= 50) GoldBorder else GlassBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "TỈ LỆ THẮNG AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tacticalState.formattedWinRate,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = winColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (tacticalState.winProbability ?: 50) / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = winColor,
                                trackColor = Color(0x33000000)
                            )
                        }
                    }

                    // Khung Chênh Lệch Vàng
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EsportsSurfaceElevated)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "CHÊNH LỆCH VÀNG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tacticalState.formattedGoldDiff,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (tacticalState.teamGoldDiff >= 0) VictoryGreen else DefeatRed
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Hạ gục: ${tacticalState.allyKills} - ${tacticalState.enemyKills}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mục Tiêu Lớn Hiện Tại (Rồng / Caesar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F1E38))
                        .border(1.dp, GoldBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FFC837))
                                    .border(1.dp, ImperialGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Mục tiêu",
                                    tint = ImperialGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MỤC TIÊU LỚN TIẾP THEO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = ImperialGold
                                )
                                Text(
                                    text = tacticalState.currentObjective?.displayName ?: "--",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { onVoiceCalloutTest("Tập trung ${tacticalState.currentObjective?.displayName ?: "Mục tiêu lớn"}") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300F0FF))
                                .border(1.dp, ArcaneCyan.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Phát giọng nói",
                                tint = ArcaneCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cảnh Báo Nguy Hiểm & Báo Động Bị Gank
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(dangerColor.copy(alpha = 0.12f))
                        .border(1.dp, dangerColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(dangerColor.copy(alpha = 0.2f))
                                .border(1.dp, dangerColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = dangerColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MỨC ĐỘ: ${tacticalState.dangerLevel.labelVi.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = dangerColor
                            )
                            Text(
                                text = tacticalState.dangerWarning.ifBlank { "Không có nguy hiểm cận kề" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { onVoiceCalloutTest(tacticalState.dangerWarning) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Phát cảnh báo",
                                tint = dangerColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (tacticalState.teamfightAdvice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Hướng Dẫn Giao Tranh & Bắt Chủ Lực
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EsportsSurfaceElevated)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "HƯỚNG DẪN GIAO TRANH TỔNG",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = ArcaneCyan
                                )
                                Text(
                                    text = "Mục Tiêu: ${tacticalState.carryTarget.ifBlank { "Chủ Lực" }}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tacticalState.teamfightAdvice,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 16.sp
                            )
                            if (tacticalState.splitPushAdvice.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Đẩy đường: ${tacticalState.splitPushAdvice}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EsportsScenarioSelector(
    onScenarioSelected: (Int) -> Unit
) {
    val scenarios = listOf(
        "1. Đầu Trận: Cảnh Báo Gank Bụi Sông (Nakroth Cấp 4)" to ArcaneCyan,
        "2. Giữa Trận: Tranh Chấp Rồng Krayg (-4.2k Vàng)" to DefeatRed,
        "3. Mở Cửa Hàng: Khuyến Nghị Huân Chương Troy & Đao Truy Hồn" to ImperialGold,
        "4. Bảng Điểm: Cửa Thắng 5v4 (Ép Tà Thần Caesar)" to VictoryGreen,
        "5. Cuối Trận: Caesar Bạo Chúa Ép Trụ Siêu Cấp & Nhà Chính" to DefeatRed
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("scenario_selector_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ImperialGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TRÌNH MÔ PHỎNG THỰC CHIẾN MẪU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.75.sp,
                    color = ImperialGold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Trải nghiệm AI nhận diện tình huống và cảnh báo trực tiếp trên HUD nổi:",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            scenarios.forEachIndexed { index, (title, accent) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EsportsSurfaceElevated)
                        .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .clickable { onScenarioSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("scenario_btn_$index")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Chạy",
                                tint = accent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
