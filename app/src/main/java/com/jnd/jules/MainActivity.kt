package com.jnd.jules

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jnd.jules.ui.SessionListScreen
import com.jnd.jules.ui.SessionDetailScreen
import com.jnd.jules.ui.SessionViewModel
import com.jnd.jules.ui.SettingsScreen
import com.jnd.jules.ui.SettingsViewModel
import com.jnd.jules.ui.theme.JulesTheme
import com.jnd.jules.util.PreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.init(this)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            
            val darkTheme = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> isSystemInDarkTheme() // System
            }
            
            JulesTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val sessionViewModel: SessionViewModel = viewModel()

                NavHost(navController = navController, startDestination = "sessions") {
                    composable("sessions") {
                        SessionListScreen(
                            viewModel = sessionViewModel,
                            onSessionClick = { session ->
                                session.id?.let { id ->
                                    navController.navigate("session/$id")
                                }
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            viewModel = settingsViewModel
                        )
                    }
                    composable(
                        route = "session/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                        SessionDetailScreen(
                            sessionId = sessionId,
                            viewModel = sessionViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
