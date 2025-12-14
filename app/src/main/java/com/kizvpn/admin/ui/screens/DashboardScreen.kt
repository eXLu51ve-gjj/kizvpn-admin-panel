package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.components.DashboardCard
import com.kizvpn.admin.ui.viewmodel.DashboardViewModel

data class DashboardItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
fun DashboardScreen(
    onNavigateToServers: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModelFactory: ViewModelFactory
) {
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    val dashboardItems = listOf(
        DashboardItem(
            title = "Серверы VPN",
            subtitle = "Управление серверами и нодами",
            icon = Icons.Default.Cloud,
            onClick = onNavigateToServers
        ),
        DashboardItem(
            title = "Пользователи",
            subtitle = "Управление пользователями VPN",
            icon = Icons.Default.People,
            onClick = onNavigateToUsers
        ),
        DashboardItem(
            title = "Платежи",
            subtitle = "Управление платежами и тарифами",
            icon = Icons.Default.Payment,
            onClick = onNavigateToPayments
        ),
        DashboardItem(
            title = "Статистика",
            subtitle = "Статистика и аналитика",
            icon = Icons.Default.BarChart,
            onClick = onNavigateToStatistics
        ),
        DashboardItem(
            title = "Настройки",
            subtitle = "Настройки приложения",
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
        )
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Заголовок для навигации
                    Text(
                        text = "Панель Управления",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        fontWeight = FontWeight.Bold
                    )

                    // Карточки навигации по разделам
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dashboardItems) { item ->
                            DashboardCard(item = item)
                        }
                    }
                }
            }
        }
        
        // Кнопка обновления внизу справа
        FloatingActionButton(
            onClick = { viewModel.refresh() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Refresh, "Обновить")
        }
    }
}



