package com.stockassistant.app.domain

import com.stockassistant.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Singleton
class AnalysisEngine @Inject constructor() {

    // ── Public API ─────────────────────────────────────────────────────────

    fun generateVerdict(
        profile: StockProfile,
        indicators: TechnicalIndicators
    ): TradingVerdict {
        val techScore = computeTechnicalScore(profile, indicators)
        val fundScore = computeFundamentalScore(profile)
        // Weight: 60% technical, 40% fundamental
        val overall = (techScore * 0.6 + fundScore * 0.4).toInt()

        val signal = scoreToSignal(overall)
        val riskLevels = computeRiskLevels(profile.currentPrice, indicators.atr14)
        val (bullish, bearish) = collectFactors(profile, indicators)
        val rationale = buildRationale(signal, profile, indicators, overall)
        val confidence = computeConfidence(techScore, fundScore, indicators, profile)

        return TradingVerdict(
            ticker = profile.ticker,
            signal = signal,
            confidence = confidence,
            entryPrice = profile.currentPrice,
            stopLoss = riskLevels.stopLossModerate,
            takeProfit1 = riskLevels.takeProfitR1,
            takeProfit2 = riskLevels.takeProfitR2,
            takeProfit3 = riskLevels.takeProfitR3,
            riskRewardRatio = (riskLevels.takeProfitR2 - profile.currentPrice) /
                    max(0.01, profile.currentPrice - riskLevels.stopLossModerate),
            rationale = rationale,
            bullishFactors = bullish,
            bearishFactors = bearish,
            technicalScore = techScore,
            fundamentalScore = fundScore,
            overallScore = overall
        )
    }

    fun computeRiskLevels(entryPrice: Double, atr: Double): RiskLevels {
        val atrSafe = if (atr <= 0) entryPrice * 0.02 else atr
        val volatility = (atrSafe / entryPrice) * 100

        val regime = when {
            volatility < 1.0 -> VolatilityRegime.LOW
            volatility < 2.5 -> VolatilityRegime.NORMAL
            volatility < 4.0 -> VolatilityRegime.ELEVATED
            else -> VolatilityRegime.HIGH
        }

        val sl1 = entryPrice - atrSafe * 1.5   // Conservative
        val sl2 = entryPrice - atrSafe * 2.0   // Moderate
        val sl3 = entryPrice - atrSafe * 3.0   // Aggressive (wider stop)

        val riskPerShare = entryPrice - sl2
        val tp1 = entryPrice + riskPerShare        // 1:1
        val tp2 = entryPrice + riskPerShare * 2.0  // 1:2
        val tp3 = entryPrice + riskPerShare * 3.0  // 1:3

        // Kelly-simplified: max 5% of portfolio in any single position
        val maxPosition = when (regime) {
            VolatilityRegime.LOW -> 5.0
            VolatilityRegime.NORMAL -> 3.0
            VolatilityRegime.ELEVATED -> 2.0
            VolatilityRegime.HIGH -> 1.0
        }

        return RiskLevels(
            ticker = "",
            entryPrice = entryPrice,
            stopLossConservative = sl1,
            stopLossModerate = sl2,
            stopLossAggressive = sl3,
            takeProfitR1 = tp1,
            takeProfitR2 = tp2,
            takeProfitR3 = tp3,
            maxPositionSizePercent = maxPosition,
            dollarRiskPer100k = (100_000 * 0.01) / (riskPerShare / entryPrice) / 100,
            atr = atrSafe,
            volatilityRegime = regime
        )
    }

    // ── Scoring ────────────────────────────────────────────────────────────

    private fun computeTechnicalScore(profile: StockProfile, ind: TechnicalIndicators): Int {
        var score = 0
        val price = profile.currentPrice

        // Trend (±40 points)
        if (price > ind.sma20) score += 8
        if (price > ind.sma50) score += 12
        if (price > ind.sma200) score += 15
        if (ind.sma50 > ind.sma200) score += 5   // Golden Cross zone

        if (price < ind.sma20) score -= 8
        if (price < ind.sma50) score -= 12
        if (price < ind.sma200) score -= 15
        if (ind.sma50 < ind.sma200) score -= 5   // Death Cross zone

        // Momentum RSI (±25 points)
        score += when {
            ind.rsi14 < 25 -> 25   // Deeply oversold — strong mean reversion signal
            ind.rsi14 < 35 -> 18
            ind.rsi14 in 35.0..50.0 -> 10
            ind.rsi14 in 50.0..60.0 -> 5
            ind.rsi14 in 60.0..70.0 -> -5
            ind.rsi14 in 70.0..80.0 -> -15
            else -> -25            // Deeply overbought
        }

        // MACD (±20 points)
        if (ind.macdLine > ind.macdSignal) score += 10
        else score -= 10
        if (ind.macdHistogram > 0) score += 10
        else score -= 10

        // Bollinger Bands (±15 points)
        val bbRange = ind.bollingerUpper - ind.bollingerLower
        if (bbRange > 0) {
            val bbPosition = (price - ind.bollingerLower) / bbRange
            score += when {
                bbPosition < 0.1 -> 15    // Price near lower band = oversold
                bbPosition < 0.3 -> 8
                bbPosition in 0.4..0.6 -> 0
                bbPosition in 0.6..0.8 -> -5
                bbPosition > 0.9 -> -15   // Price near upper band = overbought
                else -> 0
            }
        }

        // EMA Cross (±10 points)
        if (ind.ema12 > ind.ema26) score += 10 else score -= 10

        return score.coerceIn(-100, 100)
    }

    private fun computeFundamentalScore(profile: StockProfile): Int {
        if (profile.peRatio == null && profile.eps == null) return 0

        var score = 0

        // P/E Ratio (±30 points)
        profile.peRatio?.let { pe ->
            score += when {
                pe < 0 -> -20           // Negative earnings
                pe < 10 -> 30           // Very cheap
                pe < 15 -> 20           // Cheap
                pe < 20 -> 10           // Fair
                pe < 25 -> 0
                pe < 35 -> -10          // Expensive
                pe < 50 -> -20          // Very expensive
                else -> -30             // Extreme valuation
            }
        }

        // 52-week position (±20 points) — contrarian signal
        val weekRange = profile.fiftyTwoWeekHigh - profile.fiftyTwoWeekLow
        if (weekRange > 0) {
            val weekPosition = (profile.currentPrice - profile.fiftyTwoWeekLow) / weekRange
            score += when {
                weekPosition < 0.2 -> 20   // Near 52w low — potential value
                weekPosition < 0.4 -> 10
                weekPosition in 0.4..0.6 -> 0
                weekPosition > 0.8 -> -15  // Near 52w high — momentum vs. extension
                else -> -5
            }
        }

        // Beta risk (±15 points)
        profile.beta?.let { beta ->
            score += when {
                beta < 0 -> 0            // Inverse — neutral treatment
                beta < 0.5 -> 10        // Low volatility (defensive)
                beta < 1.0 -> 5
                beta < 1.5 -> 0
                beta < 2.0 -> -5
                else -> -15             // Very high beta — elevated risk
            }
        }

        // EPS positive check (±15 points)
        profile.eps?.let { eps ->
            score += if (eps > 0) 15 else -15
        }

        // Dividend yield bonus (up to +10 points)
        profile.dividendYield?.let { yield ->
            score += when {
                yield > 0.05 -> 10   // >5% yield
                yield > 0.02 -> 5    // 2-5%
                else -> 0
            }
        }

        return score.coerceIn(-100, 100)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun scoreToSignal(score: Int): VerdictSignal = when {
        score >= 55 -> VerdictSignal.STRONG_BUY
        score >= 20 -> VerdictSignal.BUY
        score >= -20 -> VerdictSignal.HOLD
        score >= -55 -> VerdictSignal.SELL
        else -> VerdictSignal.STRONG_SELL
    }

    private fun computeConfidence(
        techScore: Int,
        fundScore: Int,
        ind: TechnicalIndicators,
        profile: StockProfile
    ): Int {
        // Confidence is higher when multiple signals agree
        val agreementBonus = if (techScore > 0 && fundScore > 0) 15
        else if (techScore < 0 && fundScore < 0) 15
        else 0

        // Higher confidence when RSI is extreme (clear signal)
        val rsiBonus = when {
            ind.rsi14 < 30 || ind.rsi14 > 70 -> 10
            else -> 0
        }

        val dataBonus = if (profile.peRatio != null && profile.eps != null) 10 else 0

        val baseConfidence = (abs(techScore) * 0.5 + abs(fundScore) * 0.3).toInt()
        return min(95, baseConfidence + agreementBonus + rsiBonus + dataBonus)
    }

    private fun collectFactors(
        profile: StockProfile,
        ind: TechnicalIndicators
    ): Pair<List<String>, List<String>> {
        val bullish = mutableListOf<String>()
        val bearish = mutableListOf<String>()
        val price = profile.currentPrice

        if (price > ind.sma50) bullish.add("Price above 50-day SMA")
        else bearish.add("Price below 50-day SMA")

        if (price > ind.sma200) bullish.add("Price above 200-day SMA (long-term uptrend)")
        else bearish.add("Price below 200-day SMA (long-term downtrend)")

        if (ind.sma50 > ind.sma200) bullish.add("Golden Cross (SMA50 > SMA200)")
        else bearish.add("Death Cross (SMA50 < SMA200)")

        if (ind.rsi14 < 35) bullish.add("RSI ${String.format("%.1f", ind.rsi14)} — oversold territory")
        if (ind.rsi14 > 65) bearish.add("RSI ${String.format("%.1f", ind.rsi14)} — overbought territory")

        if (ind.macdLine > ind.macdSignal) bullish.add("MACD bullish crossover")
        else bearish.add("MACD bearish crossover")

        if (ind.macdHistogram > 0) bullish.add("MACD histogram positive (momentum building)")
        else bearish.add("MACD histogram negative (momentum fading)")

        profile.peRatio?.let { pe ->
            if (pe in 1.0..20.0) bullish.add("Attractive P/E ratio of ${String.format("%.1f", pe)}x")
            if (pe > 40) bearish.add("Elevated P/E ratio of ${String.format("%.1f", pe)}x")
        }

        profile.eps?.let { eps ->
            if (eps > 0) bullish.add("Positive EPS of \$${String.format("%.2f", eps)}")
            else bearish.add("Negative EPS — company not yet profitable")
        }

        if (profile.changePercent > 2.0) bullish.add("Strong session gain of +${String.format("%.1f", profile.changePercent)}%")
        if (profile.changePercent < -2.0) bearish.add("Significant session loss of ${String.format("%.1f", profile.changePercent)}%")

        return bullish to bearish
    }

    private fun buildRationale(
        signal: VerdictSignal,
        profile: StockProfile,
        ind: TechnicalIndicators,
        score: Int
    ): List<String> = buildList {
        add("${profile.ticker} scores $score/100 on our composite model.")
        when (signal) {
            VerdictSignal.STRONG_BUY -> add("Multiple technical and fundamental indicators align for a strong bullish case.")
            VerdictSignal.BUY -> add("Overall technical picture is constructive with moderate fundamental support.")
            VerdictSignal.HOLD -> add("Conflicting signals suggest waiting for a clearer directional catalyst.")
            VerdictSignal.SELL -> add("Technical deterioration suggests reducing exposure or tightening stops.")
            VerdictSignal.STRONG_SELL -> add("Broad technical breakdown with weak fundamentals — risk/reward is unfavorable.")
        }
        add("RSI(14) at ${String.format("%.1f", ind.rsi14)} — ${rsiContext(ind.rsi14)}.")
        if (profile.peRatio != null) {
            add("Trading at ${String.format("%.1f", profile.peRatio)}x earnings.")
        }
    }

    private fun rsiContext(rsi: Double): String = when {
        rsi < 30 -> "deeply oversold, mean reversion likely"
        rsi < 40 -> "approaching oversold"
        rsi < 50 -> "slightly bearish momentum"
        rsi < 60 -> "neutral to mildly bullish"
        rsi < 70 -> "bullish momentum"
        else -> "overbought, pullback risk elevated"
    }
}
