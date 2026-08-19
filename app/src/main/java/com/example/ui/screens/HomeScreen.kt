package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.LiveCoachService
import com.example.ui.components.EsportsHeroBanner
import com.example.ui.components.EsportsScenarioSelector
import com.example.ui.components.EsportsTacticalRadarCard
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.EsportsCardGradient
import com.example.ui.theme.EsportsDarkBg
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CoachViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: CoachViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val tacticalState by viewModel.tacticalState.collectAsState()

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, LiveCoachService::class.java).apply {
                action = LiveCoachService.ACTION_START
                putExtra(LiveCoachService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(LiveCoachService.EXTRA_RESULT_DATA, result.data)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            scope.launch {
                snackbarHostState.showSnackbar("Đã kích hoạt Trợ Lý Liên Quân AI kèm Cửa Sổ Nổi!")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Quyền chụp màn hình bị từ chối.")
            }
        }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Cần cấp quyền 'Hiển thị trên ứng dụng khác' để dùng HUD nổi.")
            }
        }
    }

    fun handleStartCoach() {
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EsportsDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_screen_root")
    ) {
        // Banner Hero Esports AAA
        EsportsHeroBanner(
            isServiceRunning = isServiceRunning,
            onLaunchClick = { handleStartCoach() },
            onStopClick = {
                viewModel.stopCoachService()
                scope.launch {
                    snackbarHostState.showSnackbar("Đã dừng dịch vụ Trợ Lý Liên Quân AI.")
                }
            },
            onSimulateClick = {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayPermissionLauncher.launch(intent)
                } else {
                    viewModel.startCoachService(isSimulation = true)
                    scope.launch {
                        snackbarHostState.showSnackbar("Đã khởi động chế độ mô phỏng chiến thuật mẫu!")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Radar Chiến Thuật Trực Tiếp
        EsportsTacticalRadarCard(
            tacticalState = tacticalState,
            onVoiceCalloutTest = { phrase ->
                viewModel.triggerVoiceCallout(phrase)
                scope.launch {
                    snackbarHostState.showSnackbar("Đang phát giọng nói: \"$phrase\"")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Trình Chọn Kịch Bản Thực Chiến Mẫu
        EsportsScenarioSelector(
            onScenarioSelected = { index ->
                viewModel.selectPresetScenario(index)
                scope.launch {
                    snackbarHostState.showSnackbar("Đã nạp tình huống mẫu ${index + 1} lên Radar & HUD nổi!")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lưu Trận Đấu Vào Lịch Sử
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(EsportsCardGradient)
                .border(1.dp, GoldBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Thành tích",
                            tint = ImperialGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LƯU KẾT QUẢ ĐẤU ĐỂ AI PHÂN TÍCH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = TextGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ghi nhận dữ liệu trận đấu hiện tại vào lịch sử để theo dõi tiến độ nâng cao kĩ năng và tỉ lệ thắng.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveCurrentMatchToHistory(isWin = true)
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã lưu trận CHIẾN THẮNG vào Lịch Sử Đấu!")
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SuccessGreen,
                            containerColor = Color(0x2200E676)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(SuccessGreen)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "LƯU THẮNG",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.saveCurrentMatchToHistory(isWin = false)
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã lưu trận THẤT BẠI vào Lịch Sử Đấu!")
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF2A4D),
                            containerColor = Color(0x22FF2A4D)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF2A4D))
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "LƯU THUA",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
