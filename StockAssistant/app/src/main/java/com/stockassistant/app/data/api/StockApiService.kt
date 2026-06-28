package com.stockassistant.app.data.api

import com.stockassistant.app.data.api.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface targeting the Alpha Vantage REST API.
 * Docs: https://www.alphavantage.co/documentation/
 *
 * Replace BuildConfig.ALPHA_VANTAGE_API_KEY with a real key for production.
 * Free tier allows 25 requests/day; premium plans remove this limit.
 */
interface StockApiService {

    // ── Quote ──────────────────────────────────────────────────────────────

    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<GlobalQuoteResponse>

    @GET("query")
    suspend fun getCompanyOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): Response<CompanyOverviewResponse>

    // ── Momentum ───────────────────────────────────────────────────────────

    @GET("query")
    suspend fun getRsi(
        @Query("function") function: String = "RSI",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("time_period") timePeriod: Int = 14,
        @Query("series_type") seriesType: String = "close",
        @Query("apikey") apiKey: String
    ): Response<RsiResponse>

    @GET("query")
    suspend fun getMacd(
        @Query("function") function: String = "MACD",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("series_type") seriesType: String = "close",
        @Query("fastperiod") fastPeriod: Int = 12,
        @Query("slowperiod") slowPeriod: Int = 26,
        @Query("signalperiod") signalPeriod: Int = 9,
        @Query("apikey") apiKey: String
    ): Response<MacdResponse>

    // ── Moving Averages ────────────────────────────────────────────────────

    @GET("query")
    suspend fun getSma(
        @Query("function") function: String = "SMA",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("time_period") timePeriod: Int,
        @Query("series_type") seriesType: String = "close",
        @Query("apikey") apiKey: String
    ): Response<MovingAverageResponse>

    @GET("query")
    suspend fun getEma(
        @Query("function") function: String = "EMA",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("time_period") timePeriod: Int,
        @Query("series_type") seriesType: String = "close",
        @Query("apikey") apiKey: String
    ): Response<MovingAverageResponse>

    // ── Volatility ─────────────────────────────────────────────────────────

    @GET("query")
    suspend fun getBollingerBands(
        @Query("function") function: String = "BBANDS",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("time_period") timePeriod: Int = 20,
        @Query("series_type") seriesType: String = "close",
        @Query("nbdevup") nbDevUp: Int = 2,
        @Query("nbdevdn") nbDevDn: Int = 2,
        @Query("apikey") apiKey: String
    ): Response<BollingerBandsResponse>

    @GET("query")
    suspend fun getAtr(
        @Query("function") function: String = "ATR",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "daily",
        @Query("time_period") timePeriod: Int = 14,
        @Query("apikey") apiKey: String
    ): Response<AtrResponse>
}
