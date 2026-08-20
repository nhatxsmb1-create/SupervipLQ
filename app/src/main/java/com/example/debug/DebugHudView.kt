package com.example.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VictoryGreen

@Composable
fun DeveloperDebugHudCard(
    debugState: DebugState,
    modifier: Modifier = Modifier
) {
    val gs = debugState.gameState

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xEE0A0F1D))
            .border(1.dp, ArcaneCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛠 DEVELOPER DEBUG HUD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ArcaneCyan,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "CONF: ${(gs.overallConfidence * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (gs.overallConfidence >= 0.70f) VictoryGreen else ImperialGold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Screen State: ${gs.screenState.name}",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Match Time: ${gs.formattedMatchTime}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "KDA: ${gs.formattedKda}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Gold Diff: ${gs.formattedGoldDiff}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Proc Time: ${gs.frameProcessingTimeMs} ms",
                    fontSize = 10.sp,
                    color = ImperialGold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Interval: ${gs.captureIntervalMs} ms",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Frame Delta: ${String.format(Locale.US, "%.1f%%", gs.frameChangedPercent)}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Components: ${gs.detectedComponents.size}",
                    fontSize = 10.sp,
                    color = ArcaneCyan,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (gs.detectedComponents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "DETECTED UI ROIs:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = ArcaneCyan,
                fontFamily = FontFamily.Monospace
            )
            gs.detectedComponents.take(4).forEach { comp ->
                Text(
                    text = "• ${comp.componentName} [${comp.detectionMethod}] conf=${(comp.confidence * 100).toInt()}% rect=${comp.boundingBox.toCompactBoundsString()}",
                    fontSize = 8.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (gs.rawOcrSummary.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "OCR output: ${gs.rawOcrSummary}",
                fontSize = 8.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                maxLines = 2
            )
        }
    }
}

private fun android.graphics.RectF.toCompactBoundsString(): String {
    return "(${String.format(Locale.US, "%.2f", left)}, ${String.format(Locale.US, "%.2f", top)}, ${String.format(Locale.US, "%.2f", right)}, ${String.format(Locale.US, "%.2f", bottom)})"
}
