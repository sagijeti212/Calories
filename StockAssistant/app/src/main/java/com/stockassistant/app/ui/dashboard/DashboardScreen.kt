package com.stockassistant.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockassistant.app.data.model.StockProfile
import com.stockassistant.app.data.model.TechnicalIndicators
import com.stockassistant.app.data.model.TradingVerdict
import com.stockassistant.app.data.model.RiskLevels
import com.stockassistant.app.ui.components.*
import com.stockassistant.app.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToWatchlist: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(52.dp))

            // Top bar
            TopBar(
                onWatchlistClick = onNavigateToWatchlist,
                isInWatchlist = state.isInWatchlist,
                hasStock = state.stockProfile != null,
                onToggleWatchlist = viewModel::toggleWatchlist
            )

            Spacer(Modifier.height(20.dp))

            // Search
            StockSearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearch = viewModel::analyzeStock,
                isLoading = state.isLoading
            )

            Spacer(Modifier.height(20.dp))

            // Error snackbar
            AnimatedVisibility(visible = state.error != null) {
                ErrorBanner(message = state.error ?: "", onDismiss = viewModel::dismissError)
                Spacer(Modifier.height(12.dp))
            }

            // Stock header
            AnimatedVisibility(
                visible = state.stockProfile != null,
                enter = fadeIn() + slideInVertically()
            ) {
                state.stockProfile?.let { profile ->
                    StockHeader(profile = profile)
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Loading skeleton
            if (state.isLoading) {
                AnalysisLoadingSkeleton()
            }

            // Tab bar + content
            AnimatedVisibility(
                visible = state.verdict != null && !state.isLoading,
                enter = fadeIn() + slideInVertically()
            ) {
                Column {
                    // Tab row
                    DashboardTabRow(
                        activeTab = state.activeTab,
                        onTabSelected = viewModel::selectTab
                    )
                    Spacer(Modifier.height(16.dp))

                    when (state.activeTab) {
                        DashboardTab.ANALYSIS -> {
                            state.verdict?.let { verdict ->
                                VerdictCard(verdict = verdict)
                                Spacer(Modifier.height(16.dp))
                                BullBearPanel(verdict = verdict)
                            }
                        }
                        DashboardTab.RISK -> {
                            state.riskLevels?.let { risk ->
                                RiskCard(riskLevels = risk)
                            }
                        }
                        DashboardTab.FUNDAMENTALS -> {
                            state.stockProfile?.let { profile ->
                                state.indicators?.let { ind ->
                                    FundamentalsPanel(profile = profile)
                                    Spacer(Modifier.height(16.dp))
                                    TechnicalPanel(profile = profile, indicators = ind)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TopBar(
    onWatchlistClick: () -> Unit,
    isInWatchlist: Boolean,
    hasStock: Boolean,
    onToggleWatchlist: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "StockAssistant",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                "AI-Powered Trading Analysis",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasStock) {
                IconButton(onClick = onToggleWatchlist) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Toggle watchlist",
                        tint = if (isInWatchlist) PrimaryGreen else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            TextButton(onClick = onWatchlistClick) {
                Text("Watchlist", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StockHeader(profile: StockProfile) {
    val changeColor = if (profile.isPositiveChange) GainGreen else LossRed
    val changeIcon = if (profile.isPositiveChange) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    profile.ticker,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    profile.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    "${profile.exchange} • ${profile.sector ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format("%.2f", profile.currentPrice)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(changeIcon, contentDescription = null, tint = changeColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${if (profile.isPositiveChange) "+" else ""}${String.format("%.2f", profile.changeAmount)} " +
                                "(${String.format("%.2f", profile.changePercent)}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Vol: ${formatVolume(profile.volume)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
            }
        }
    }
}

@Composable
private fun DashboardTabRow(activeTab: DashboardTab, onTabSelected: (DashboardTab) -> Unit) {
    val tabs = listOf(
        DashboardTab.ANALYSIS to "Analysis",
        DashboardTab.RISK to "Risk Mgmt",
        DashboardTab.FUNDAMENTALS to "Technicals"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (tab, label) ->
            val isActive = tab == activeTab
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) PrimaryGreen else Color.Transparent,
                onClick = { onTabSelected(tab) }
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Color.Black else TextSecondary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BullBearPanel(verdict: TradingVerdict) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FactorCard(
            title = "Bullish Factors",
            factors = verdict.bullishFactors,
            color = GainGreen,
            modifier = Modifier.weight(1f)
        )
        FactorCard(
            title = "Bearish Factors",
            factors = verdict.bearishFactors,
            color = LossRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FactorCard(
    title: String,
    factors: List<String>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))
            if (factors.isEmpty()) {
                Text("None identified", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
            } else {
                factors.take(5).forEach { factor ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = color, fontSize = 11.sp)
                        Text(factor, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FundamentalsPanel(profile: StockProfile) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Fundamental Data",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(14.dp))
            FundRow("P/E Ratio", profile.peRatio?.let { String.format("%.2f", it) } ?: "N/A")
            FundRow("EPS", profile.eps?.let { "$${String.format("%.2f", it)}" } ?: "N/A")
            FundRow("Market Cap", profile.marketCap.let { formatMarketCap(it) })
            FundRow("Dividend Yield", profile.dividendYield?.let { "${String.format("%.2f", it * 100)}%" } ?: "N/A")
            FundRow("Beta", profile.beta?.let { String.format("%.2f", it) } ?: "N/A")
            FundRow("52-Week High", "$${String.format("%.2f", profile.fiftyTwoWeekHigh)}")
            FundRow("52-Week Low", "$${String.format("%.2f", profile.fiftyTwoWeekLow)}")
            FundRow("From 52W High", "${String.format("%.1f", profile.distanceFromHigh)}%")
        }
    }
}

@Composable
private fun FundRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, LossRed.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = LossRed, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = LossRed)
            }
        }
    }
}

@Composable
private fun AnalysisLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(SurfaceVariantDark, RoundedCornerShape(16.dp))
            )
        }
    }
}

private fun formatVolume(volume: Long): String = when {
    volume >= 1_000_000_000 -> "${String.format("%.1f", volume / 1_000_000_000.0)}B"
    volume >= 1_000_000 -> "${String.format("%.1f", volume / 1_000_000.0)}M"
    volume >= 1_000 -> "${String.format("%.1f", volume / 1_000.0)}K"
    else -> volume.toString()
}

private fun formatMarketCap(cap: Long): String = when {
    cap >= 1_000_000_000_000 -> "$${String.format("%.2f", cap / 1_000_000_000_000.0)}T"
    cap >= 1_000_000_000 -> "$${String.format("%.2f", cap / 1_000_000_000.0)}B"
    cap >= 1_000_000 -> "$${String.format("%.2f", cap / 1_000_000.0)}M"
    cap > 0 -> "$$cap"
    else -> "N/A"
}
