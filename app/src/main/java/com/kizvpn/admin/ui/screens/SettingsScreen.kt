package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kizvpn.admin.util.BiometricHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    authRepository: com.kizvpn.admin.data.repository.AuthRepository? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val apiUrl by authRepository?.apiUrl?.collectAsState(initial = null) ?: remember { mutableStateOf<String?>(null) }
    val token by authRepository?.token?.collectAsState(initial = null) ?: remember { mutableStateOf<String?>(null) }
    val biometricEnabled by authRepository?.biometricEnabled?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    
    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    val biometricType = remember { BiometricHelper.getBiometricType(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
            // Информация о подключении
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Информация о подключении",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    apiUrl?.let {
                        SettingRow(
                            icon = Icons.Default.Link,
                            title = "API URL",
                            value = it
                        )
                    }
                    
                    token?.let {
                        SettingRow(
                            icon = Icons.Default.Lock,
                            title = "JWT токен",
                            value = if (it.length > 20) "${it.take(20)}..." else it
                        )
                    }
                }
            }
            
            // Настройки безопасности
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
                    Text(
                        text = "Безопасность",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Биометрическая аутентификация",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isBiometricAvailable) biometricType else "Недоступно на этом устройстве",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = biometricEnabled && isBiometricAvailable,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    authRepository?.setBiometricEnabled(enabled)
                                }
                            },
                            enabled = isBiometricAvailable
                        )
                    }
                }
            }
            
            // Версия приложения
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "Версия",
                        value = "1.0.0"
                    )
                    SettingRow(
                        icon = Icons.Default.Code,
                        title = "Сборка",
                        value = "1"
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка выхода
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }
}

@Composable
fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


