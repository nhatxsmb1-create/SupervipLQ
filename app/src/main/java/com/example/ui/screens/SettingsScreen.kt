package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SupportedGame
import com.example.service.CoachStateHub
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldGlowGradient
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val voiceEnabled by viewModel.voiceEnabled.collectAsState()
    val highPriorityOnly by viewModel.highPriorityOnly.collectAsState()
    val voiceSpeed by viewModel.voiceSpeed.collectAsState()
    val voicePitch by viewModel.voicePitch.collectAsState()
    val overlayAlpha by viewModel.overlayAlpha.collectAsState()
    val selectedGame by viewModel.selectedGame.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        // Tiêu đề
        Text(
            text = "CẤU HÌNH HỆ THỐNG TRỢ LÝ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = TextGold
        )
        Text(
            text = "Tùy chỉnh giọng nói chỉ huy tiếng Việt, HUD nổi và hồ sơ game",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cấu hình Giọng Nói Chỉ Huy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(EsportsCardGradient)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("voice_settings_card")
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Giọng nói",
                        tint = ImperialGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GIỌNG NÓI CHỈ HUY TIẾNG VIỆT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bật / Tắt giọng nói
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Bật Giọng Nói Chỉ Huy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Tự động phát âm thanh cảnh báo chiến thuật", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = { viewModel.setVoiceEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF261900),
                            checkedTrackColor = ImperialGold
                        ),
                        modifier = Modifier.testTag("switch_voice_enabled")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chỉ cảnh báo ưu tiên cao
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Chỉ Phát Cảnh Báo Nguy Cấp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Giảm tần suất giọng nói để tập trung tay đấu", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = highPriorityOnly,
                        onCheckedChange = { viewModel.setHighPriorityOnly(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = ArcaneCyan
                        ),
                        modifier = Modifier.testTag("switch_high_priority")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thanh trượt tốc độ đọc
                Text(
                    text = "Tốc độ đọc giọng nói: ${"%.2f".format(voiceSpeed)}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Slider(
                    value = voiceSpeed,
                    onValueChange = { viewModel.setVoiceSpeed(it) },
                    valueRange = 0.8f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = ArcaneCyan, activeTrackColor = ArcaneCyan),
                    modifier = Modifier.testTag("slider_voice_speed")
                )

                // Thanh trượt cao độ
                Text(
                    text = "Cao độ giọng nói: ${"%.2f".format(voicePitch)}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Slider(
                    value = voicePitch,
                    onValueChange = { viewModel.setVoicePitch(it) },
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = ImperialGold, activeTrackColor = ImperialGold),
                    modifier = Modifier.testTag("slider_voice_pitch")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { viewModel.testVoiceCallout("Cảnh báo: Kẻ địch đang di chuyển xuống Rồng!") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(GoldGlowGradient, RoundedCornerShape(10.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Test giọng nói",
                            tint = Color(0xFF261900),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "THỬ GIỌNG NÓI CHỈ HUY",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF261900)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cài đặt Cửa Sổ Nổi HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(EsportsCardGradient)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("overlay_settings_card")
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Cửa sổ nổi",
                        tint = ArcaneCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CỬA SỔ NỔI HUD TRONG TRẬN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Độ trong suốt nền HUD: ${(overlayAlpha * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Điều chỉnh để không che khuất tầm nhìn chiêu thức khi combat",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Slider(
                    value = overlayAlpha,
                    onValueChange = { viewModel.setOverlayAlpha(it) },
                    valueRange = 0.5f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ImperialGold, activeTrackColor = ImperialGold),
                    modifier = Modifier.testTag("slider_overlay_alpha")
                )

                Spacer(modifier = Modifier.height(10.dp))

                val debugState by CoachStateHub.debugState.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Chế Độ Nhà Phát Triển (Debug HUD)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArcaneCyan)
                        Text(text = "Hiển thị ROI bounding box, confidence, OCR log & frame time", fontSize = 11.sp, color = TextMuted)
                    }
                    Switch(
                        checked = debugState.isEnabled,
                        onCheckedChange = { CoachStateHub.setDebugHudEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = ArcaneCyan
                        ),
                        modifier = Modifier.testTag("switch_debug_hud")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chọn Hồ Sơ Tựa Game
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(EsportsCardGradient)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("game_profile_card")
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Hồ sơ game",
                        tint = ImperialGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HỒ SƠ TỰA GAME MOBA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SupportedGame.entries.forEach { game ->
                    val isSelected = selectedGame == game
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0F2644) else EsportsSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ImperialGold else GlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSelectedGame(game) }
                            .padding(12.dp)
                            .testTag("game_option_${game.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ImperialGoldLight else Color.White
                                )
                                Text(
                                    text = "Mục tiêu: ${game.defaultDragonName} • ${game.defaultSlayerName}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Đang chọn",
                                    tint = ImperialGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cam kết An Toàn Tuyệt Đối
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF061A12))
                .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("compliance_card")
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Bảo mật",
                    tint = SuccessGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TUÂN THỦ & AN TOÀN TÀI KHOẢN TUYỆT ĐỐI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ứng dụng tuyệt đối KHÔNG can thiệp bộ nhớ game, KHÔNG sửa đổi tệp, KHÔNG tự động bấm chiêu. 100% tuân thủ điều khoản Garena Liên Quân Mobile.",
                        fontSize = 11.sp,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
