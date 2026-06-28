package com.stockassistant.app.data.api.dto

import com.google.gson.annotations.SerializedName

// Alpha Vantage RSI response
data class RsiResponse(
    @SerializedName("Meta Data") val metaData: Map<String, String>?,
    @SerializedName("Technical Analysis: RSI") val technicalAnalysis: Map<String, RsiPoint>?
)

data class RsiPoint(
    @SerializedName("RSI") val rsi: String
)

// Alpha Vantage MACD response
data class MacdResponse(
    @SerializedName("Meta Data") val metaData: Map<String, String>?,
    @SerializedName("Technical Analysis: MACD") val technicalAnalysis: Map<String, MacdPoint>?
)

data class MacdPoint(
    @SerializedName("MACD") val macd: String,
    @SerializedName("MACD_Signal") val macdSignal: String,
    @SerializedName("MACD_Hist") val macdHist: String
)

// Alpha Vantage SMA/EMA response
data class MovingAverageResponse(
    @SerializedName("Meta Data") val metaData: Map<String, String>?,
    @SerializedName("Technical Analysis: SMA") val smaSeries: Map<String, MaPoint>?,
    @SerializedName("Technical Analysis: EMA") val emaSeries: Map<String, MaPoint>?
)

data class MaPoint(
    @SerializedName("SMA") val sma: String?,
    @SerializedName("EMA") val ema: String?
)

// Alpha Vantage BBANDS response
data class BollingerBandsResponse(
    @SerializedName("Meta Data") val metaData: Map<String, String>?,
    @SerializedName("Technical Analysis: BBANDS") val technicalAnalysis: Map<String, BollingerPoint>?
)

data class BollingerPoint(
    @SerializedName("Real Upper Band") val upperBand: String,
    @SerializedName("Real Middle Band") val middleBand: String,
    @SerializedName("Real Lower Band") val lowerBand: String
)

// Alpha Vantage ATR response
data class AtrResponse(
    @SerializedName("Meta Data") val metaData: Map<String, String>?,
    @SerializedName("Technical Analysis: ATR") val technicalAnalysis: Map<String, AtrPoint>?
)

data class AtrPoint(
    @SerializedName("ATR") val atr: String
)
