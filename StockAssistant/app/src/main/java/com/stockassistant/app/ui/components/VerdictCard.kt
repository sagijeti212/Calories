package com.stockassistant.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stockassistant.app.data.model.TradingVerdict
import com.stockassistant.app.data.model.VerdictSignal
import com.stockassistant.app.ui.theme.*

@Composable
fun VerdictCard(verdict: TradingVerdict, modifier: Modifier = Modifier) {
    val signalColor = verdictColor(verdict.signal)
    val gradient = Brush.verticalGradient(
        colors = listOf(signalColor.copy(alpha = 0.18f), signalColor.copy(alpha = 0.04f))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, signalColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Signal label
                Text(
                    text = verdict.signal.label,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = signalColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(4.dp))

                // Confidence bar
                ConfidenceBar(confidence = verdict.confidence, color = signalColor)

                Spacer(Modifier.height(16.dp))

                // Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PricePill(label = "ENTRY", value = verdict.entryPrice, color = TextPrimary)
                    PricePill(label = "STOP", value = verdict.stopLoss, color = LossRed)
                    PricePill(label = "TARGET", value = verdict.takeProfit2, color = GainGreen)
                }

                Spacer(Modifier.height(12.dp))

                // R:R badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = signalColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Risk/Reward  1 : ${String.format("%.1f", verdict.riskRewardRatio)}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = signalColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(14.dp))
                Divider(color = OutlineDark)
                Spacer(Modifier.height(14.dp))

                // Rationale
                verdict.rationale.forEach { line ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = signalColor, fontWeight = FontWeight.Bold)
                        Text(line, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBar(confidence: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Confidence $confidence%",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        val animatedProgress by animateFloatAsState(
            targetValue = confidence / 100f,
            animationSpec = tween(800, easing = EaseOutCubic),
            label = "confidence"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = OutlineDark
        )
    }
}

@Composable
private fun PricePill(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "$${String.format("%.2f", value)}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = color
        )
    }
}

fun verdictColor(signal: VerdictSignal): Color = when (signal) {
    VerdictSignal.STRONG_BUY -> StrongBuyColor
    VerdictSignal.BUY -> BuyColor
    VerdictSignal.HOLD -> HoldColor
    VerdictSignal.SELL -> SellColor
    VerdictSignal.STRONG_SELL -> StrongSellColor
}
