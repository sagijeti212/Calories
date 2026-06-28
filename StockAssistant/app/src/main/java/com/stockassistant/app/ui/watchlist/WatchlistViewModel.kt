package com.stockassistant.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockassistant.app.data.model.AssetType
import com.stockassistant.app.data.model.WatchlistItem
import com.stockassistant.app.data.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchlistUiState(
    val items: List<WatchlistItem> = emptyList(),
    val showAddDialog: Boolean = false,
    val newTickerInput: String = "",
    val selectedAssetType: AssetType = AssetType.STOCK
)

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWatchlist().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }

    fun dismissAddDialog() = _uiState.update {
        it.copy(showAddDialog = false, newTickerInput = "", selectedAssetType = AssetType.STOCK)
    }

    fun onTickerInputChange(input: String) =
        _uiState.update { it.copy(newTickerInput = input.uppercase()) }

    fun onAssetTypeChange(type: AssetType) =
        _uiState.update { it.copy(selectedAssetType = type) }

    fun addTicker() {
        val ticker = _uiState.value.newTickerInput.trim()
        if (ticker.isBlank()) return
        viewModelScope.launch {
            repository.addToWatchlist(
                WatchlistItem(
                    ticker = ticker,
                    assetType = _uiState.value.selectedAssetType
                )
            )
            dismissAddDialog()
        }
    }

    fun removeTicker(ticker: String) {
        viewModelScope.launch { repository.removeFromWatchlist(ticker) }
    }
}
