package com.stockassistant.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stockassistant.app.data.model.RiskLevels
import com.stockassistant.app.data.model.VolatilityRegime
import com.stockassistant.app.ui.theme.*

@Composable
fun RiskCard(riskLevels: RiskLevels, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Risk Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                VolatilityChip(riskLevels.volatilityRegime)
            }

            Spacer(Modifier.height(16.dp))

            // Stop Loss section
            SectionLabel("STOP LOSS LEVELS")
            Spacer(Modifier.height(8.dp))
            RiskRow("Conservative (1.5× ATR)", riskLevels.stopLossConservative, LossRed.copy(alpha = 0.7f))
            RiskRow("Moderate (2.0× ATR)", riskLevels.stopLossModerate, LossRed)
            RiskRow("Aggressive (3.0× ATR)", riskLevels.stopLossAggressive, LossRed.copy(alpha = 0.6f))

            Spacer(Modifier.height(14.dp))
            Divider(color = OutlineDark)
            Spacer(Modifier.height(14.dp))

            // Take Profit section
            SectionLabel("TAKE PROFIT TARGETS")
            Spacer(Modifier.height(8.dp))
            RiskRow("Target 1  (1:1 R/R)", riskLevels.takeProfitR1, GainGreen.copy(alpha = 0.6f))
            RiskRow("Target 2  (1:2 R/R)", riskLevels.takeProfitR2, GainGreen)
            RiskRow("Target 3  (1:3 R/R)", riskLevels.takeProfitR3, GainGreen)

            Spacer(Modifier.height(14.dp))
            Divider(color = OutlineDark)
            Spacer(Modifier.height(14.dp))

            // Position sizing
            SectionLabel("POSITION SIZING")
            Spacer(Modifier.height(8.dp))
            InfoRow("ATR (14-day)", "$${String.format("%.2f", riskLevels.atr)}")
            InfoRow("Max position size", "${String.format("%.0f", riskLevels.maxPositionSizePercent)}% of portfolio")
            InfoRow("Dollar risk per \$100k (1% rule)", "$${String.format("%.0f", riskLevels.dollarRiskPer100k)}")
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 1.sp)
}

private val sp = androidx.compose.ui.unit.TextUnit.Unspecified

@Composable
private fun RiskRow(label: String, value: Double, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(
            "$${String.format("%.2f", value)}",
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VolatilityChip(regime: VolatilityRegime) {
    val (label, color) = when (regime) {
        VolatilityRegime.LOW -> "Low Vol" to GainGreen
        VolatilityRegime.NORMAL -> "Normal Vol" to BuyColor
        VolatilityRegime.ELEVATED -> "Elevated Vol" to HoldColor
        VolatilityRegime.HIGH -> "High Vol" to LossRed
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
