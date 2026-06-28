package com.stockassistant.app.data.model

data class StockProfile(
    val ticker: String,
    val companyName: String,
    val currentPrice: Double,
    val previousClose: Double,
    val openPrice: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val volume: Long,
    val avgVolume: Long,
    val marketCap: Long,
    val peRatio: Double?,
    val eps: Double?,
    val dividendYield: Double?,
    val fiftyTwoWeekHigh: Double,
    val fiftyTwoWeekLow: Double,
    val beta: Double?,
    val sector: String?,
    val currency: String = "USD",
    val exchange: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val changeAmount: Double get() = currentPrice - previousClose
    val changePercent: Double get() = if (previousClose != 0.0) (changeAmount / previousClose) * 100 else 0.0
    val isPositiveChange: Boolean get() = changeAmount >= 0
    val distanceFromHigh: Double get() = if (fiftyTwoWeekHigh != 0.0) ((currentPrice - fiftyTwoWeekHigh) / fiftyTwoWeekHigh) * 100 else 0.0
    val distanceFromLow: Double get() = if (fiftyTwoWeekLow != 0.0) ((currentPrice - fiftyTwoWeekLow) / fiftyTwoWeekLow) * 100 else 0.0
}
