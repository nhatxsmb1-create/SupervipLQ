package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameState
import com.example.model.ScreenState
import com.example.service.CoachStateHub
import com.example.tactical.TacticalEngine
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.DefeatRed
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.vision.VisionAnalysisEngine
import com.example.voice.VoiceCoach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phòng Thử Nghiệm OCR & Phân Tích Ảnh Chụp Game Thực Tế.
 * Cho phép người dùng tải lên bất kỳ ảnh chụp màn hình trận đấu Liên Quân nào (hoặc dùng ảnh mẫu có sẵn)
 * để kiểm tra trực tiếp khả năng bóc tách đồng hồ (00:48), tỉ số, vàng và phân tích chiến thuật AI.
 */
@Composable
fun RealImageOcrTestLabCard(
    onGameStateParsed: (GameState) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analyzedGameState by remember { mutableStateOf<GameState?>(null) }
    var detectedTimeLabel by remember { mutableStateOf<String?>(null) }
    var adviceSummary by remember { mutableStateOf<String?>(null) }
    var rawOcrSnippet by remember { mutableStateOf<String?>(null) }

    val visionEngine = remember { VisionAnalysisEngine() }
    val tacticalEngine = remember { TacticalEngine() }
    val voiceCoach = remember { VoiceCoach(context) }

    fun processTestBitmap(bmp: Bitmap) {
        selectedBitmap = bmp
        isAnalyzing = true
        analyzedGameState = null
        detectedTimeLabel = null
        adviceSummary = null
        rawOcrSnippet = null

        scope.launch {
            val gameState = withContext(Dispatchers.Default) {
                visionEngine.analyzeFrameToGameState(bmp, captureIntervalMs = 1000L)
            }

            val currentTactical = CoachStateHub.tacticalState.value
            val eval = tacticalEngine.evaluateGameState(gameState, currentTactical)

            // Cập nhật lên Cửa Sổ Nổi Hub & Bàn cờ AI
            CoachStateHub.updateGameState(gameState)
            CoachStateHub.updateState(eval.newState)
            onGameStateParsed(gameState)

            val mins = (gameState.matchTimeSeconds ?: 0) / 60
            val secs = (gameState.matchTimeSeconds ?: 0) % 60
            val formattedTime = String.format("%02d:%02d", mins, secs)

            detectedTimeLabel = formattedTime
            adviceSummary = eval.newState.dangerWarning ?: "Đang theo dõi trận đấu..."
            rawOcrSnippet = gameState.rawOcrSummary
            analyzedGameState = gameState
            isAnalyzing = false

            // Đọc giọng nói HLV ngay lập tức
            eval.voiceCallout?.let { phrase ->
                voiceCoach.speakCallout(
                    callout = phrase,
                    tag = eval.calloutTag,
                    priority = eval.calloutPriority
                )
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bmp != null) {
                    processTestBitmap(bmp)
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EsportsCardGradient)
            .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("real_image_ocr_test_card")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(ArcaneCyan.copy(alpha = 0.2f))
                            .border(1.dp, ArcaneCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Test Quét Ảnh",
                            tint = ArcaneCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TEST QUÉT & ĐỌC MÀN HÌNH GAME THỰC TẾ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = TextGold
                        )
                        Text(
                            text = "Tải ảnh chụp trận đấu của bạn để AI nhận diện OCR & phân tích",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nút Tải Ảnh Lên Từ Thư Viện
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHỌN ẢNH CHỤP GAME ĐỂ TEST",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Xem trước ảnh & Trạng thái phân tích
            if (isAnalyzing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EsportsSurfaceElevated)
                        .padding(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ImperialGold,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đang quét OCR phân vùng đồng hồ, tỉ số & bóc tách chiến thuật...",
                        fontSize = 11.sp,
                        color = ImperialGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            selectedBitmap?.let { bmp ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Ảnh chụp màn hình test",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Kết quả OCR & Phân Tích Thực Chiến
            analyzedGameState?.let { gs ->
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EsportsSurface)
                        .border(1.dp, if (gs.screenState == ScreenState.IN_MATCH) SuccessGreen else GoldBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "KẾT QUẢ AI BÓC TÁCH TỪ ẢNH:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessGreen
                                )
                            }

                            Text(
                                text = "Thời gian: ${detectedTimeLabel ?: "--"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = ImperialGold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Trạng thái: ${gs.screenState.titleVi} (Độ tin cậy ${(gs.overallConfidence * 100).toInt()}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "• Tỉ số: ${gs.allyKills ?: 0} vs ${gs.enemyKills ?: 0} | Vàng: ${if ((gs.goldDifference ?: 0) >= 0) "+" else ""}${gs.goldDifference ?: 0}G",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Hiển thị Đội hình bóc tách được nếu có
                        if (gs.allyRoster.isNotEmpty() || gs.enemyRoster.isNotEmpty()) {
                            val allyNames = gs.allyRoster.map { "${it.heroName} (${if (it.kda.isNotBlank()) it.kda else "${it.gold}G"})" }.joinToString(", ")
                            val enemyNames = gs.enemyRoster.map { "${it.heroName} (${if (it.kda.isNotBlank()) it.kda else "${it.gold}G"})" }.joinToString(", ")

                            if (allyNames.isNotBlank()) {
                                Text(
                                    text = "🔵 Phe Ta: $allyNames",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcaneCyan
                                )
                            }
                            if (enemyNames.isNotBlank()) {
                                Text(
                                    text = "🔴 Phe Địch: $enemyNames",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DefeatRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (gs.currentShopGold != null && gs.currentShopGold > 0) {
                            Text(
                                text = "💰 Vàng Kho Đồ: ${gs.currentShopGold} Vàng",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ImperialGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F1E38))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "📢 Lời khuyên HLV: ${adviceSummary ?: ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcaneCyan
                            )
                        }

                        if (!rawOcrSnippet.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ký tự OCR đọc được: \"$rawOcrSnippet\"",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
