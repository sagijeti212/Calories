package com.stockassistant.app.data.model

data class TradingVerdict(
    val ticker: String,
    val signal: VerdictSignal,
    val confidence: Int,                 // 0-100 confidence score
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,             // Conservative target
    val takeProfit2: Double,             // Moderate target
    val takeProfit3: Double,             // Aggressive target
    val riskRewardRatio: Double,
    val rationale: List<String>,         // Bullet-point reasons
    val bullishFactors: List<String>,
    val bearishFactors: List<String>,
    val technicalScore: Int,             // -100 to +100
    val fundamentalScore: Int,           // -100 to +100
    val overallScore: Int,               // Weighted composite
    val timestamp: Long = System.currentTimeMillis()
)

enum class VerdictSignal(val label: String, val emoji: String) {
    STRONG_BUY("STRONG BUY", "🚀"),
    BUY("BUY", "📈"),
    HOLD("HOLD", "⏸"),
    SELL("SELL", "📉"),
    STRONG_SELL("STRONG SELL", "🔻")
}

data class RiskLevels(
    val ticker: String,
    val entryPrice: Double,
    val stopLossConservative: Double,    // ATR × 1.5 below entry
    val stopLossModerate: Double,        // ATR × 2.0 below entry
    val stopLossAggressive: Double,      // ATR × 3.0 below entry
    val takeProfitR1: Double,            // 1:1 Risk/Reward
    val takeProfitR2: Double,            // 1:2 Risk/Reward
    val takeProfitR3: Double,            // 1:3 Risk/Reward
    val maxPositionSizePercent: Double,  // Kelly Criterion derived
    val dollarRiskPer100k: Double,       // Dollar risk for $100k portfolio at 1% risk
    val atr: Double,
    val volatilityRegime: VolatilityRegime
)

enum class VolatilityRegime { LOW, NORMAL, ELEVATED, HIGH }
