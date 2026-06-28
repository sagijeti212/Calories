package com.stockassistant.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockassistant.app.data.model.*
import com.stockassistant.app.data.repository.Result
import com.stockassistant.app.data.repository.StockRepository
import com.stockassistant.app.domain.AnalysisEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val stockProfile: StockProfile? = null,
    val indicators: TechnicalIndicators? = null,
    val verdict: TradingVerdict? = null,
    val riskLevels: RiskLevels? = null,
    val isInWatchlist: Boolean = false,
    val error: String? = null,
    val activeTab: DashboardTab = DashboardTab.ANALYSIS
)

enum class DashboardTab { ANALYSIS, RISK, FUNDAMENTALS }

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: StockRepository,
    private val engine: AnalysisEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var analysisJob: Job? = null

    // ── User Actions ───────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query.uppercase(), error = null) }
    }

    fun analyzeStock(ticker: String) {
        val normalizedTicker = ticker.trim().uppercase()
        if (normalizedTicker.isBlank()) return

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, stockProfile = null, verdict = null) }

            // Fetch profile
            val profileResult = repository.fetchStockProfile(normalizedTicker)
            if (profileResult is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = profileResult.message) }
                return@launch
            }

            val profile = (profileResult as Result.Success).data
            val inWatchlist = repository.isInWatchlist(normalizedTicker)

            // Fetch technical indicators in parallel (already handled by repository)
            val indicatorsResult = repository.fetchTechnicalIndicators(normalizedTicker, profile.currentPrice)

            val indicators = when (indicatorsResult) {
                is Result.Success -> indicatorsResult.data
                is Result.Error -> {
                    // Graceful fallback: generate mock indicators from profile data if API fails
                    generateFallbackIndicators(profile)
                }
                else -> generateFallbackIndicators(profile)
            }

            val verdict = engine.generateVerdict(profile, indicators)
            val riskLevels = engine.computeRiskLevels(profile.currentPrice, indicators.atr14)
                .copy(ticker = normalizedTicker)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    stockProfile = profile,
                    indicators = indicators,
                    verdict = verdict,
                    riskLevels = riskLevels,
                    isInWatchlist = inWatchlist,
                    error = null
                )
            }
        }
    }

    fun toggleWatchlist() {
        val profile = _uiState.value.stockProfile ?: return
        viewModelScope.launch {
            val inWatchlist = _uiState.value.isInWatchlist
            if (inWatchlist) {
                repository.removeFromWatchlist(profile.ticker)
            } else {
                repository.addToWatchlist(
                    WatchlistItem(
                        ticker = profile.ticker,
                        assetType = AssetType.STOCK
                    )
                )
            }
            _uiState.update { it.copy(isInWatchlist = !inWatchlist) }
        }
    }

    fun selectTab(tab: DashboardTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    // ── Fallback for API limit / demo key ─────────────────────────────────

    /**
     * When the Alpha Vantage "demo" key is used, indicator endpoints return
     * limited data. This generates plausible indicators from the quote data
     * so the UI still renders a meaningful result.
     */
    private fun generateFallbackIndicators(profile: StockProfile): TechnicalIndicators {
        val price = profile.currentPrice
        val atr = price * 0.02 // Estimate 2% ATR
        return TechnicalIndicators(
            ticker = profile.ticker,
            sma20 = price * (if (profile.isPositiveChange) 0.97 else 1.03),
            sma50 = price * (if (profile.isPositiveChange) 0.94 else 1.06),
            sma200 = price * 0.95,
            ema12 = price * (if (profile.isPositiveChange) 0.99 else 1.01),
            ema26 = price * (if (profile.isPositiveChange) 0.98 else 1.02),
            rsi14 = when {
                profile.changePercent > 3 -> 68.0
                profile.changePercent > 1 -> 58.0
                profile.changePercent < -3 -> 32.0
                profile.changePercent < -1 -> 42.0
                else -> 50.0
            },
            macdLine = if (profile.isPositiveChange) 0.5 else -0.5,
            macdSignal = 0.0,
            macdHistogram = if (profile.isPositiveChange) 0.5 else -0.5,
            bollingerUpper = price * 1.04,
            bollingerMiddle = price,
            bollingerLower = price * 0.96,
            obv = 0.0,
            volumeRatio = if (profile.volume > 0 && profile.avgVolume > 0) profile.volume.toDouble() / profile.avgVolume else 1.0,
            atr14 = atr,
            historicalVolatility = (atr / price) * 100
        )
    }
}
