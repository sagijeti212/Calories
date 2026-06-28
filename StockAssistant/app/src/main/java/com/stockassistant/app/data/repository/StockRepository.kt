package com.stockassistant.app.data.repository

import com.stockassistant.app.BuildConfig
import com.stockassistant.app.data.api.StockApiService
import com.stockassistant.app.data.db.WatchlistDao
import com.stockassistant.app.data.db.toEntity
import com.stockassistant.app.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class StockRepository @Inject constructor(
    private val api: StockApiService,
    private val dao: WatchlistDao
) {
    private val apiKey get() = BuildConfig.ALPHA_VANTAGE_API_KEY

    // ── Market Data ────────────────────────────────────────────────────────

    suspend fun fetchStockProfile(ticker: String): Result<StockProfile> = runCatching {
        coroutineScope {
            val quoteDeferred = async { api.getGlobalQuote(symbol = ticker, apiKey = apiKey) }
            val overviewDeferred = async { api.getCompanyOverview(symbol = ticker, apiKey = apiKey) }

            val quoteResp = quoteDeferred.await()
            val overviewResp = overviewDeferred.await()

            val quote = quoteResp.body()?.globalQuote
                ?: return@coroutineScope Result.Error("No quote data for $ticker")
            val overview = overviewResp.body()

            StockProfile(
                ticker = ticker.uppercase(),
                companyName = overview?.name ?: ticker,
                currentPrice = quote.price.toDoubleOrNull() ?: 0.0,
                previousClose = quote.previousClose.toDoubleOrNull() ?: 0.0,
                openPrice = quote.open.toDoubleOrNull() ?: 0.0,
                dayHigh = quote.high.toDoubleOrNull() ?: 0.0,
                dayLow = quote.low.toDoubleOrNull() ?: 0.0,
                volume = quote.volume.toLongOrNull() ?: 0L,
                avgVolume = 0L, // Alpha Vantage free tier doesn't expose avg volume directly
                marketCap = overview?.marketCap?.toLongOrNull() ?: 0L,
                peRatio = overview?.peRatio?.toDoubleOrNull(),
                eps = overview?.eps?.toDoubleOrNull(),
                dividendYield = overview?.dividendYield?.toDoubleOrNull(),
                fiftyTwoWeekHigh = overview?.weekHigh52?.toDoubleOrNull() ?: 0.0,
                fiftyTwoWeekLow = overview?.weekLow52?.toDoubleOrNull() ?: 0.0,
                beta = overview?.beta?.toDoubleOrNull(),
                sector = overview?.sector,
                currency = overview?.currency ?: "USD",
                exchange = overview?.exchange ?: "UNKNOWN"
            )
        }
    }.fold(
        onSuccess = { if (it is Result<*>) @Suppress("UNCHECKED_CAST") (it as Result<StockProfile>) else Result.Success(it as StockProfile) },
        onFailure = { Result.Error(it.message ?: "Unknown error", it) }
    )

    suspend fun fetchTechnicalIndicators(ticker: String, currentPrice: Double): Result<TechnicalIndicators> =
        runCatching {
            coroutineScope {
                val rsiD = async { api.getRsi(symbol = ticker, apiKey = apiKey) }
                val macdD = async { api.getMacd(symbol = ticker, apiKey = apiKey) }
                val sma20D = async { api.getSma(symbol = ticker, timePeriod = 20, apiKey = apiKey) }
                val sma50D = async { api.getSma(symbol = ticker, timePeriod = 50, apiKey = apiKey) }
                val sma200D = async { api.getSma(symbol = ticker, timePeriod = 200, apiKey = apiKey) }
                val ema12D = async { api.getEma(symbol = ticker, timePeriod = 12, apiKey = apiKey) }
                val ema26D = async { api.getEma(symbol = ticker, timePeriod = 26, apiKey = apiKey) }
                val bbandsD = async { api.getBollingerBands(symbol = ticker, apiKey = apiKey) }
                val atrD = async { api.getAtr(symbol = ticker, apiKey = apiKey) }

                fun latestRsi() = rsiD.await().body()?.technicalAnalysis
                    ?.entries?.maxByOrNull { it.key }?.value?.rsi?.toDoubleOrNull() ?: 50.0
                fun latestMacd() = macdD.await().body()?.technicalAnalysis
                    ?.entries?.maxByOrNull { it.key }?.value
                fun latestSma(resp: retrofit2.Response<MovingAverageResponse>) =
                    resp.body()?.smaSeries?.entries?.maxByOrNull { it.key }?.value?.sma?.toDoubleOrNull() ?: currentPrice
                fun latestEma(resp: retrofit2.Response<MovingAverageResponse>) =
                    resp.body()?.emaSeries?.entries?.maxByOrNull { it.key }?.value?.ema?.toDoubleOrNull() ?: currentPrice
                fun latestBbands() = bbandsD.await().body()?.technicalAnalysis
                    ?.entries?.maxByOrNull { it.key }?.value
                fun latestAtr() = atrD.await().body()?.technicalAnalysis
                    ?.entries?.maxByOrNull { it.key }?.value?.atr?.toDoubleOrNull() ?: (currentPrice * 0.02)

                val macd = latestMacd()
                val bbands = latestBbands()
                val rsi = latestRsi()
                val atr = latestAtr()

                TechnicalIndicators(
                    ticker = ticker,
                    sma20 = latestSma(sma20D.await()),
                    sma50 = latestSma(sma50D.await()),
                    sma200 = latestSma(sma200D.await()),
                    ema12 = latestEma(ema12D.await()),
                    ema26 = latestEma(ema26D.await()),
                    rsi14 = rsi,
                    macdLine = macd?.macd?.toDoubleOrNull() ?: 0.0,
                    macdSignal = macd?.macdSignal?.toDoubleOrNull() ?: 0.0,
                    macdHistogram = macd?.macdHist?.toDoubleOrNull() ?: 0.0,
                    bollingerUpper = bbands?.upperBand?.toDoubleOrNull() ?: currentPrice * 1.02,
                    bollingerMiddle = bbands?.middleBand?.toDoubleOrNull() ?: currentPrice,
                    bollingerLower = bbands?.lowerBand?.toDoubleOrNull() ?: currentPrice * 0.98,
                    obv = 0.0,
                    volumeRatio = 1.0,
                    atr14 = atr,
                    historicalVolatility = (atr / currentPrice) * 100
                )
            }
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(it.message ?: "Indicator fetch failed", it) }
        )

    // ── Watchlist CRUD ─────────────────────────────────────────────────────

    fun observeWatchlist(): Flow<List<WatchlistItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addToWatchlist(item: WatchlistItem) = dao.upsert(item.toEntity())

    suspend fun removeFromWatchlist(ticker: String) = dao.deleteByTicker(ticker)

    suspend fun isInWatchlist(ticker: String): Boolean = dao.exists(ticker)
}
