package com.stockassistant.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE ticker = :ticker LIMIT 1")
    suspend fun findByTicker(ticker: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchlistEntity): Long

    @Delete
    suspend fun delete(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE ticker = :ticker")
    suspend fun deleteByTicker(ticker: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE ticker = :ticker)")
    suspend fun exists(ticker: String): Boolean
}
