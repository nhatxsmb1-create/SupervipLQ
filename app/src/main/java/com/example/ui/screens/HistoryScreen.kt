package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchRecord
import com.example.model.MatchResult
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.ArcaneCyanGlow
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VictoryGreen
import com.example.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val matches by viewModel.matches.collectAsState()
    val totalMatches by viewModel.totalMatches.collectAsState()
    val winRate by viewModel.winRate.collectAsState()
    val averageScore by viewModel.averageScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsDarkBg)
            .padding(16.dp)
            .testTag("history_screen_root")
    ) {
        // Tiêu đề đầu trang Esports
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LỊCH SỬ ĐẤU & CHẤM ĐIỂM AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = TextGold
                )
                Text(
                    text = "Dữ liệu hiệu quả chiến thuật và chấm điểm tuyển thủ",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.resetDemoData() },
                    modifier = Modifier.testTag("reset_demo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Nạp mẫu",
                        tint = ArcaneCyan
                    )
                }

                if (matches.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Xóa Lịch Sử",
                            tint = DefeatRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Khung Thống Kê Giải Đấu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EsportsStatBadge(
                title = "TỔNG TRẬN",
                value = "$totalMatches",
                color = ArcaneCyan,
                modifier = Modifier.weight(1f)
            )
            EsportsStatBadge(
                title = "TỈ LỆ THẮNG",
                value = "$winRate%",
                color = if (winRate >= 50) VictoryGreen else DefeatRed,
                modifier = Modifier.weight(1f)
            )
            EsportsStatBadge(
                title = "ĐIỂM AI TB",
                value = "$averageScore",
                color = ImperialGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Trống",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có dữ liệu trận đấu",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Hãy hoàn thành trận đấu với Trợ Lý AI hoặc bấm icon Reset để xem dữ liệu mẫu!",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    EsportsMatchCard(match = match)
                }
            }
        }
    }
}

@Composable
fun EsportsStatBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun EsportsMatchCard(match: MatchRecord) {
    val isWin = match.result == MatchResult.VICTORY
    val resultColor = if (isWin) VictoryGreen else DefeatRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EsportsCardGradient)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(resultColor.copy(alpha = 0.6f), Color(0x331E293B))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("match_item_${match.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultColor.copy(alpha = 0.2f))
                            .border(1.dp, resultColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = match.result.labelVi,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = resultColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = match.heroUsed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${match.heroRole})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Điểm AI",
                        tint = ImperialGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.coachScore} Điểm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = ImperialGoldLight,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Thời lượng",
                        tint = ArcaneCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.formattedDuration,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "KDA: ${match.finalKDA}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = if (match.finalGoldDiff >= 0) "+${match.finalGoldDiff} Vàng" else "${match.finalGoldDiff} Vàng",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (match.finalGoldDiff >= 0) VictoryGreen else DefeatRed,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EsportsSurfaceElevated)
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Mục tiêu",
                        tint = ImperialGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mục tiêu: ${match.topObjectiveContested} • ${match.tacticalNotes}",
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
