package com.kizvpn.admin.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kizvpn.admin.ui.screens.*
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executor

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Servers : Screen("servers")
    object Users : Screen("users")
    object Payments : Screen("payments")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
}

@Composable
fun AdminNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: com.kizvpn.admin.data.repository.AuthRepository? = null,
    viewModelFactory: com.kizvpn.admin.di.ViewModelFactory? = null,
    onApiReady: (String, String) -> Unit = { _, _ -> },
    mainActivity: AppCompatActivity,
    mainExecutor: Executor
) {
    android.util.Log.d("AdminNavGraph", "AdminNavGraph создан, mainActivity=${mainActivity.javaClass.simpleName}, mainExecutor=${mainExecutor.javaClass.simpleName}")
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            android.util.Log.d("AdminNavGraph", "Рендеринг LoginScreen")
            LoginScreen(
                onLoginSuccess = { apiUrl, token ->
                    onApiReady(apiUrl, token)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authRepository = authRepository,
                mainActivity = mainActivity,
                mainExecutor = mainExecutor
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToServers = { navController.navigate(Screen.Servers.route) },
                onNavigateToUsers = { navController.navigate(Screen.Users.route) },
                onNavigateToPayments = { navController.navigate(Screen.Payments.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                viewModelFactory = viewModelFactory ?: return@composable
            )
        }
        
        composable(Screen.Servers.route) {
            ServersScreen(
                onBack = { navController.popBackStack() },
                viewModelFactory = viewModelFactory ?: return@composable
            )
        }
        
        composable(Screen.Users.route) {
            if (viewModelFactory == null) {
                // Если ViewModelFactory не готов, показываем сообщение и возвращаемся
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ожидание инициализации...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        CircularProgressIndicator()
                    }
                }
                LaunchedEffect(Unit) {
                    android.util.Log.w("AdminNavGraph", "ViewModelFactory null при открытии UsersScreen, возврат на логин")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            UsersScreen(
                onBack = { navController.popBackStack() },
                viewModelFactory = viewModelFactory
            )
        }
        
        composable(Screen.Payments.route) {
            PaymentsScreen(
                onBack = { navController.popBackStack() },
                viewModelFactory = viewModelFactory ?: return@composable
            )
        }
        
        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                viewModelFactory = viewModelFactory ?: return@composable
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authRepository = authRepository
            )
        }
    }
}

