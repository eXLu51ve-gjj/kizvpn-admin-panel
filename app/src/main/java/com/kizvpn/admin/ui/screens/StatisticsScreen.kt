package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.components.StatCard
import com.kizvpn.admin.ui.viewmodel.StatisticsViewModel
import com.kizvpn.admin.util.formatBytes
import com.kizvpn.admin.util.formatRamInGB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit = {},
    viewModelFactory: ViewModelFactory
) {
    val viewModel: com.kizvpn.admin.ui.viewmodel.StatisticsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.error ?: "Ошибка",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Повторить")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Заголовок для метрик производительности
                Text(
                    text = "Производительность Сервера",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                
                // Метрики производительности (CPU, RAM, трафик, онлайн пользователи) - сетка 2x2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // CPU Usage
                    item {
                        StatCard(
                            title = "Использование CPU",
                            value = "%.1f%%".format(uiState.cpuUsage),
                            subtitle = if (uiState.cpuCores > 0) "${uiState.cpuCores} ядер" else "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                    
                    // RAM Usage
                    item {
                        StatCard(
                            title = "Использование RAM",
                            value = if (uiState.ramTotal > 0) {
                                "${formatRamInGB(uiState.ramUsage)} / ${formatRamInGB(uiState.ramTotal)}"
                            } else {
                                formatRamInGB(uiState.ramUsage)
                            },
                            subtitle = if (uiState.ramTotal > 0) {
                                "%.1f%%".format((uiState.ramUsage.toDouble() / uiState.ramTotal.toDouble()) * 100.0)
                            } else {
                                ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                    
                    // Total Traffic
                    item {
                        StatCard(
                            title = "Общий трафик",
                            value = formatBytes(uiState.totalTraffic),
                            subtitle = if (uiState.totalDownload > 0 || uiState.totalUpload > 0) {
                                "↓ ${formatBytes(uiState.totalDownload)} ↑ ${formatBytes(uiState.totalUpload)}"
                            } else {
                                "Всего использовано"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                    
                    // Online Users
                    item {
                        StatCard(
                            title = "Онлайн пользователи",
                            value = "${uiState.onlineUsers}",
                            subtitle = "${uiState.activeUsers} активных из ${uiState.totalUsers}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
                
                // Заголовок для общей статистики
                Text(
                    text = "Общая Статистика",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                
                // Общая статистика
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow("Всего пользователей", uiState.totalUsers.toString())
                        StatRow("Активных пользователей", uiState.activeUsers.toString())
                        StatRow("Всего узлов", uiState.totalNodes.toString())
                        StatRow("Активных узлов", uiState.activeNodes.toString())
                        StatRow("Всего трафика", formatBytes(uiState.totalTraffic))
                        if (uiState.totalUpload > 0 || uiState.totalDownload > 0) {
                            StatRow("Отправлено", formatBytes(uiState.totalUpload))
                            StatRow("Получено", formatBytes(uiState.totalDownload))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
