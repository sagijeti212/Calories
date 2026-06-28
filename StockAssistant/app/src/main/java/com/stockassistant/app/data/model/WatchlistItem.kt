package com.stockassistant.app.data.model

data class WatchlistItem(
    val id: Long = 0,
    val ticker: String,
    val assetType: AssetType,
    val alertPriceHigh: Double? = null,
    val alertPriceLow: Double? = null,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

enum class AssetType(val label: String) {
    STOCK("Stock"),
    ETF("ETF"),
    CRYPTO("Crypto"),
    FOREX("Forex")
}
