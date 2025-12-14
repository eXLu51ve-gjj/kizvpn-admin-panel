package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.viewmodel.ServersViewModel
import com.kizvpn.admin.ui.viewmodel.ServerTab
import com.kizvpn.admin.util.formatBytes
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    onBack: () -> Unit = {},
    viewModelFactory: ViewModelFactory
) {
    val viewModel: ServersViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var showRebootMenu by remember { mutableStateOf(false) }
    var showRebootDialog by remember { mutableStateOf<String?>(null) }
    var isRebooting by remember { mutableStateOf(false) }
    
    // Загружаем данные при первом открытии
    LaunchedEffect(Unit) {
        if (uiState.nodes.isEmpty() && uiState.inbounds.isEmpty() && !uiState.isLoading && uiState.error == null) {
            viewModel.loadAll()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (uiState.selectedTab) {
                            ServerTab.NODES -> "Узлы VPN"
                            ServerTab.INBOUNDS -> "Входящие подключения"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    // Меню перезагрузки серверов
                    Box {
                        IconButton(onClick = { showRebootMenu = true }) {
                            Icon(Icons.Default.PowerSettingsNew, "Перезагрузить сервер")
                        }
                        DropdownMenu(
                            expanded = showRebootMenu,
                            onDismissRequest = { showRebootMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Перезагрузить VPN сервер (10.10.10.110)") },
                                onClick = {
                                    showRebootMenu = false
                                    showRebootDialog = "10.10.10.110"
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Cloud, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Перезагрузить Bot сервер (10.10.10.120)") },
                                onClick = {
                                    showRebootMenu = false
                                    showRebootDialog = "10.10.10.120"
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, null)
                                }
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Табы
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                Tab(
                    selected = uiState.selectedTab == ServerTab.NODES,
                    onClick = { viewModel.selectTab(ServerTab.NODES) },
                    text = { Text("Узлы") }
                )
                Tab(
                    selected = uiState.selectedTab == ServerTab.INBOUNDS,
                    onClick = { viewModel.selectTab(ServerTab.INBOUNDS) },
                    text = { Text("Inbounds") }
                )
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "Ошибка",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else {
                when (uiState.selectedTab) {
                    ServerTab.NODES -> {
                        if (uiState.nodes.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.CloudOff,
                                title = "Узлы не найдены",
                                description = "На сервере нет настроенных узлов VPN"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.nodes) { node ->
                                    NodeCard(node = node, apiBaseUrl = uiState.apiBaseUrl)
                                }
                            }
                        }
                    }
                    ServerTab.INBOUNDS -> {
                        if (uiState.inbounds.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.NetworkCheck,
                                title = "Inbounds не найдены",
                                description = "На сервере нет настроенных входящих подключений. Они будут отображаться здесь после настройки в PasarGuard."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.inbounds) { inbound ->
                                    InboundCard(inbound = inbound)
                                }
                            }
                        }
                    }
                }
            }
            
            // Диалог подтверждения перезагрузки
            showRebootDialog?.let { serverIp ->
                AlertDialog(
                    onDismissRequest = { if (!isRebooting) showRebootDialog = null },
                    title = { Text("Подтверждение перезагрузки") },
                    text = {
                        Column {
                            Text(
                                text = "Вы уверены, что хотите перезагрузить сервер $serverIp?\n\n" +
                                        "Сервер будет недоступен в течение нескольких минут."
                            )
                            if (isRebooting) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    isRebooting = true
                                    try {
                                        val result = viewModel.rebootServer(serverIp)
                                        result.fold(
                                            onSuccess = { message ->
                                                android.widget.Toast.makeText(
                                                    context,
                                                    message,
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                                showRebootDialog = null
                                            },
                                            onFailure = { error ->
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Ошибка перезагрузки: ${error.message}",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Ошибка: ${e.message}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        isRebooting = false
                                        showRebootDialog = null
                                    }
                                }
                            },
                            enabled = !isRebooting
                        ) {
                            if (isRebooting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Перезагрузить")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showRebootDialog = null },
                            enabled = !isRebooting
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun NodeCard(node: Node, apiBaseUrl: String? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок с названием и статусом
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = node.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${node.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Статус
                node.status?.let { status ->
                    StatusChip(
                        status = status,
                        isActive = status.lowercase() == "active" || status.lowercase() == "connected"
                    )
                }
            }
            
            Divider()
            
            // Информация о сервере
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "Адрес сервера",
                    value = if (node.address == "127.0.0.1" || node.address == "localhost") {
                        // Показываем API URL, если доступен, иначе "Локальный сервер"
                        apiBaseUrl?.let { url ->
                            try {
                                // Извлекаем хост из URL (например, из https://host.kizvpn.ru/api получаем host.kizvpn.ru)
                                val host = url
                                    .removePrefix("https://")
                                    .removePrefix("http://")
                                    .split("/")[0]
                                    .split(":")[0]
                                host
                            } catch (e: Exception) {
                                url
                            }
                        } ?: "Локальный сервер"
                    } else {
                        node.address
                    },
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    icon = Icons.Default.Info,
                    label = "Порт",
                    value = node.port.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Полный адрес с портом
            val displayAddress = if (node.address == "127.0.0.1" || node.address == "localhost") {
                // Для локального адреса показываем API URL, если доступен
                apiBaseUrl ?: node.address
            } else {
                node.address
            }
            
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (node.address == "127.0.0.1" || node.address == "localhost") {
                            // Для локального адреса показываем только хост без порта, если это API URL
                            apiBaseUrl?.let { url ->
                                try {
                                    val host = url
                                        .removePrefix("https://")
                                        .removePrefix("http://")
                                        .split("/")[0]
                                        .split(":")[0]
                                    host
                                } catch (e: Exception) {
                                    url
                                }
                            } ?: "${node.address}:${node.port}"
                        } else {
                            "${node.address}:${node.port}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Дата создания
            node.createdAt?.let { createdAt ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Создан: ${formatDate(createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InboundCard(inbound: Inbound) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inbound.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${inbound.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider()
            
            // Информация о протоколе и порте
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.Settings,
                    label = "Протокол",
                    value = inbound.protocol.uppercase(),
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    icon = Icons.Default.Info,
                    label = "Порт",
                    value = inbound.port.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Listen адрес, если есть
            inbound.listen?.let { listen ->
                if (listen.isNotBlank()) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.RadioButtonChecked,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Listen адрес",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = listen,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusChip(status: String, isActive: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isActive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = if (isActive) "Активен" else status.replaceFirstChar { it.uppercaseChar() },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "Не указано"
    return try {
        val dateTime = OffsetDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        dateTime.format(formatter)
    } catch (e: DateTimeParseException) {
        // Если не получилось распарсить, возвращаем как есть
        dateString
    } catch (e: Exception) {
        dateString
    }
}
