package com.stockassistant.app.data.api.dto

import com.google.gson.annotations.SerializedName

// Alpha Vantage GLOBAL_QUOTE response
data class GlobalQuoteResponse(
    @SerializedName("Global Quote") val globalQuote: GlobalQuote?
)

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String,
    @SerializedName("02. open") val open: String,
    @SerializedName("03. high") val high: String,
    @SerializedName("04. low") val low: String,
    @SerializedName("05. price") val price: String,
    @SerializedName("06. volume") val volume: String,
    @SerializedName("07. latest trading day") val latestTradingDay: String,
    @SerializedName("08. previous close") val previousClose: String,
    @SerializedName("09. change") val change: String,
    @SerializedName("10. change percent") val changePercent: String
)

// Alpha Vantage OVERVIEW response
data class CompanyOverviewResponse(
    @SerializedName("Symbol") val symbol: String?,
    @SerializedName("Name") val name: String?,
    @SerializedName("Exchange") val exchange: String?,
    @SerializedName("Currency") val currency: String?,
    @SerializedName("Sector") val sector: String?,
    @SerializedName("MarketCapitalization") val marketCap: String?,
    @SerializedName("PERatio") val peRatio: String?,
    @SerializedName("EPS") val eps: String?,
    @SerializedName("DividendYield") val dividendYield: String?,
    @SerializedName("Beta") val beta: String?,
    @SerializedName("52WeekHigh") val weekHigh52: String?,
    @SerializedName("52WeekLow") val weekLow52: String?,
    @SerializedName("200DayMovingAverage") val sma200: String?,
    @SerializedName("50DayMovingAverage") val sma50: String?,
    @SerializedName("AnalystTargetPrice") val analystTargetPrice: String?
)
