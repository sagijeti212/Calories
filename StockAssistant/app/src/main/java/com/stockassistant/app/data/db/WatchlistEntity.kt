package com.stockassistant.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stockassistant.app.data.model.AssetType
import com.stockassistant.app.data.model.WatchlistItem

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticker: String,
    val assetType: String,
    val alertPriceHigh: Double?,
    val alertPriceLow: Double?,
    val notes: String,
    val addedAt: Long
) {
    fun toDomain() = WatchlistItem(
        id = id,
        ticker = ticker,
        assetType = AssetType.valueOf(assetType),
        alertPriceHigh = alertPriceHigh,
        alertPriceLow = alertPriceLow,
        notes = notes,
        addedAt = addedAt
    )
}

fun WatchlistItem.toEntity() = WatchlistEntity(
    id = id,
    ticker = ticker,
    assetType = assetType.name,
    alertPriceHigh = alertPriceHigh,
    alertPriceLow = alertPriceLow,
    notes = notes,
    addedAt = addedAt
)
