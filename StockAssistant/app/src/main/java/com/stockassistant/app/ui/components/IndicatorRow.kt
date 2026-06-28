package com.stockassistant.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stockassistant.app.data.model.StockProfile
import com.stockassistant.app.data.model.TechnicalIndicators
import com.stockassistant.app.ui.theme.*

@Composable
fun TechnicalPanel(
    profile: StockProfile,
    indicators: TechnicalIndicators,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Technical Indicators",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            // RSI gauge
            RsiGauge(indicators.rsi14)

            Spacer(Modifier.height(16.dp))
            Divider(color = OutlineDark)
            Spacer(Modifier.height(12.dp))

            // Moving Averages
            SectionHeader("MOVING AVERAGES")
            MaRow("SMA 20", indicators.sma20, profile.currentPrice)
            MaRow("SMA 50", indicators.sma50, profile.currentPrice)
            MaRow("SMA 200", indicators.sma200, profile.currentPrice)
            MaRow("EMA 12", indicators.ema12, profile.currentPrice)
            MaRow("EMA 26", indicators.ema26, profile.currentPrice)

            Spacer(Modifier.height(12.dp))
            Divider(color = OutlineDark)
            Spacer(Modifier.height(12.dp))

            // MACD
            SectionHeader("MACD (12, 26, 9)")
            IndicatorValueRow("MACD Line", indicators.macdLine,
                bullish = indicators.macdLine > indicators.macdSignal)
            IndicatorValueRow("Signal Line", indicators.macdSignal, bullish = true, neutral = true)
            IndicatorValueRow("Histogram", indicators.macdHistogram,
                bullish = indicators.macdHistogram > 0)

            Spacer(Modifier.height(12.dp))
            Divider(color = OutlineDark)
            Spacer(Modifier.height(12.dp))

            // Bollinger Bands
            SectionHeader("BOLLINGER BANDS (20, 2)")
            IndicatorValueRow("Upper Band", indicators.bollingerUpper, bullish = true, neutral = true)
            IndicatorValueRow("Middle Band", indicators.bollingerMiddle, bullish = true, neutral = true)
            IndicatorValueRow("Lower Band", indicators.bollingerLower, bullish = true, neutral = true)
        }
    }
}

@Composable
private fun RsiGauge(rsi: Double) {
    val (label, color) = when {
        rsi < 20 -> "Extremely Oversold" to StrongBuyColor
        rsi < 30 -> "Oversold" to BuyColor
        rsi < 40 -> "Approaching Oversold" to GainGreen
        rsi < 60 -> "Neutral" to HoldColor
        rsi < 70 -> "Approaching Overbought" to SellColor
        rsi < 80 -> "Overbought" to LossRed
        else -> "Extremely Overbought" to StrongSellColor
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("RSI (14)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.weight(1f))
            Text(
                "${String.format("%.1f", rsi)}",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

private val sp = androidx.compose.ui.unit.TextUnit.Unspecified

@Composable
private fun MaRow(label: String, maValue: Double, currentPrice: Double) {
    val aboveMa = currentPrice >= maValue
    val (statusLabel, color) = if (aboveMa) "Above" to GainGreen else "Below" to LossRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$${String.format("%.2f", maValue)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Spacer(Modifier.width(6.dp))
            Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun IndicatorValueRow(
    label: String,
    value: Double,
    bullish: Boolean,
    neutral: Boolean = false
) {
    val color = when {
        neutral -> TextPrimary
        bullish -> GainGreen
        else -> LossRed
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            String.format("%.4f", value),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
