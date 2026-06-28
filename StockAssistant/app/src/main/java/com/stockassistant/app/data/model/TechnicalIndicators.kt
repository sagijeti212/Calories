package com.stockassistant.app.data.model

data class TechnicalIndicators(
    val ticker: String,
    // Moving Averages
    val sma20: Double,
    val sma50: Double,
    val sma200: Double,
    val ema12: Double,
    val ema26: Double,
    // Momentum
    val rsi14: Double,
    // MACD
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    // Bollinger Bands
    val bollingerUpper: Double,
    val bollingerMiddle: Double,
    val bollingerLower: Double,
    // Volume
    val obv: Double,           // On-Balance Volume
    val volumeRatio: Double,   // Current vs avg volume ratio
    // Volatility
    val atr14: Double,         // Average True Range
    val historicalVolatility: Double
) {
    // Derived signals
    val isAboveSma20: Boolean get() = rsi14 > 0   // placeholder, set per currentPrice context
    val isMacdBullish: Boolean get() = macdLine > macdSignal
    val isRsiOversold: Boolean get() = rsi14 < 30.0
    val isRsiOverbought: Boolean get() = rsi14 > 70.0
    val isRsiNeutral: Boolean get() = rsi14 in 40.0..60.0
    val macdCrossSignal: MacdSignal get() = when {
        macdHistogram > 0 && macdLine > macdSignal -> MacdSignal.BULLISH_CROSS
        macdHistogram < 0 && macdLine < macdSignal -> MacdSignal.BEARISH_CROSS
        else -> MacdSignal.NEUTRAL
    }
}

enum class MacdSignal { BULLISH_CROSS, BEARISH_CROSS, NEUTRAL }

data class PriceSignals(
    val ticker: String,
    val currentPrice: Double,
    val isAboveSma20: Boolean,
    val isAboveSma50: Boolean,
    val isAboveSma200: Boolean,
    val isGoldenCross: Boolean,   // SMA50 > SMA200
    val isDeathCross: Boolean,    // SMA50 < SMA200
    val isBollingerSqueeze: Boolean,
    val priceNearBollingerUpper: Boolean,
    val priceNearBollingerLower: Boolean
)
