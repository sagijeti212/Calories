package com.stockassistant.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stockassistant.app.ui.dashboard.DashboardScreen
import com.stockassistant.app.ui.theme.StockAssistantTheme
import com.stockassistant.app.ui.watchlist.WatchlistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockAssistantTheme {
                AppNavGraph()
            }
        }
    }
}

private object Routes {
    const val DASHBOARD = "dashboard"
    const val WATCHLIST = "watchlist"
    const val ANALYZE_TICKER = "dashboard?ticker={ticker}"
}

@Composable
private fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToWatchlist = { navController.navigate(Routes.WATCHLIST) }
            )
        }
        composable(Routes.WATCHLIST) {
            WatchlistScreen(
                onBack = { navController.popBackStack() },
                onTickerSelected = { ticker ->
                    navController.popBackStack()
                    // Navigate back to dashboard — the DashboardViewModel will be triggered
                    // by a deep-link pattern if needed; for now the user can re-enter the ticker
                }
            )
        }
    }
}
