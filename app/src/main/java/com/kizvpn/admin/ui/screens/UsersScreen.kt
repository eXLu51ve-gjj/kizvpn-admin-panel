package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizvpn.admin.data.model.User
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.viewmodel.UsersViewModel
import com.kizvpn.admin.util.formatBytes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onBack: () -> Unit = {},
    viewModelFactory: ViewModelFactory
) {
    android.util.Log.d("UsersScreen", "Экран пользователей создается")
    
    val viewModel: UsersViewModel = androidx.lifecycle.viewmodel.compose.viewModel<UsersViewModel>(
        factory = viewModelFactory
    ).also {
        android.util.Log.d("UsersScreen", "UsersViewModel создан успешно")
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val filteredUsers by viewModel.filteredUsers.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showUserDetails by remember { mutableStateOf<User?>(null) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Загружаем пользователей при первом открытии экрана
    LaunchedEffect(Unit) {
        android.util.Log.d("UsersScreen", "LaunchedEffect выполнен, проверка необходимости загрузки")
        android.util.Log.d("UsersScreen", "users.isEmpty: ${uiState.users.isEmpty()}, isLoading: ${uiState.isLoading}, error: ${uiState.error}")
        if (uiState.users.isEmpty() && !uiState.isLoading && uiState.error == null) {
            android.util.Log.d("UsersScreen", "Запуск загрузки пользователей")
            viewModel.loadUsers()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Пользователи (${filteredUsers.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Добавить")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Обновить")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Добавить пользователя")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поиск
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.searchUsers(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск по имени, email, ID...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
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
                        Text(
                            text = uiState.error ?: "Ошибка",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Повторить")
                        }
                    }
                }
            } else if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Пользователи не найдены",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isNotEmpty()) {
                            Text(
                                text = "Попробуйте изменить поисковый запрос",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onClick = { showUserDetails = user }
                        )
                    }
                }
            }
        }
    }
    
    // Диалог создания пользователя
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { 
                android.util.Log.d("UsersScreen", "AddUserDialog dismissed")
                showAddDialog = false 
            },
            onConfirm = { request ->
                android.util.Log.d("UsersScreen", "AddUserDialog onConfirm called with email: ${request.email}")
                scope.launch {
                    try {
                        android.util.Log.d("UsersScreen", "Calling viewModel.createUser...")
                        val result = viewModel.createUser(request)
                        android.util.Log.d("UsersScreen", "createUser result: success=${result.isSuccess}")
                        
                        result.onSuccess { user ->
                            android.util.Log.d("UsersScreen", "✅ User created successfully: id=${user.id}, email=${user.email}")
                            showAddDialog = false
                            showMessage = "Пользователь создан успешно!"
                        }.onFailure { error ->
                            android.util.Log.e("UsersScreen", "❌ Error creating user", error)
                            showMessage = "Ошибка создания: ${error.message ?: "Неизвестная ошибка"}"
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("UsersScreen", "❌ Exception in createUser", e)
                        showMessage = "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
                    }
                }
            }
        )
    }
    
    // Диалог деталей пользователя
    showUserDetails?.let { user ->
        UserDetailsDialog(
            user = user,
            viewModel = viewModel,
            context = context,
            onDismiss = { showUserDetails = null },
            onDelete = {
                scope.launch {
                    android.util.Log.d("UsersScreen", "Начало удаления пользователя ${user.id}")
                    val result = viewModel.deleteUser(user.id)
                    if (result.isSuccess) {
                        android.util.Log.d("UsersScreen", "Пользователь ${user.id} успешно удален")
                        showUserDetails = null
                        showMessage = "Пользователь успешно удален"
                    } else {
                        val error = result.exceptionOrNull()
                        android.util.Log.e("UsersScreen", "Ошибка удаления пользователя ${user.id}", error)
                        showMessage = "Ошибка удаления: ${error?.message ?: "Неизвестная ошибка"}"
                    }
                }
            }
        )
    }
    
    // Показываем сообщение об ошибке/успехе
    showMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            showMessage = null
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMessage = null },
            title = { Text(if (message.contains("Ошибка")) "Ошибка" else "Успех") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { showMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(user: User, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${user.id} • ${user.protocol?.uppercase() ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (user.status) {
                        "active" -> MaterialTheme.colorScheme.primaryContainer
                        "expired" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = user.status ?: "unknown",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Трафик",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatBytes(user.getTrafficUsed())} / ${formatBytes(user.getTrafficLimit())}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                user.getExpiryDateString()?.let { expiry ->
                    Column {
                        val daysRemaining = calculateDaysRemaining(expiry)
                        Text(
                            text = if (daysRemaining != null) {
                                "Осталось дней: $daysRemaining"
                            } else {
                                "Действителен до"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (daysRemaining != null) {
                                formatDate(expiry)
                            } else {
                                formatDate(expiry)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (daysRemaining != null && daysRemaining < 7) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.kizvpn.admin.data.model.CreateUserRequest) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("vless") }
    var inboundId by remember { mutableStateOf("1") }
    var expiryDays by remember { mutableStateOf("30") }
    var trafficLimitGb by remember { mutableStateOf("100") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить пользователя") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username *") },
                    placeholder = { Text("user_name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (опционально)") },
                    placeholder = { Text("user@example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = protocol,
                    onValueChange = { protocol = it },
                    label = { Text("Протокол") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = inboundId,
                    onValueChange = { inboundId = it },
                    label = { Text("Inbound ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it },
                    label = { Text("Срок действия (дней)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = trafficLimitGb,
                    onValueChange = { trafficLimitGb = it },
                    label = { Text("Лимит трафика (GB)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    android.util.Log.d("AddUserDialog", "========== CREATE BUTTON CLICKED ==========")
                    android.util.Log.d("AddUserDialog", "Username: '$username'")
                    android.util.Log.d("AddUserDialog", "Email: '$email'")
                    android.util.Log.d("AddUserDialog", "Protocol: '$protocol'")
                    android.util.Log.d("AddUserDialog", "InboundId: '$inboundId'")
                    android.util.Log.d("AddUserDialog", "ExpiryDays: '$expiryDays'")
                    android.util.Log.d("AddUserDialog", "TrafficLimitGb: '$trafficLimitGb'")
                    
                    if (username.isBlank()) {
                        android.util.Log.w("AddUserDialog", "⚠️ Username is blank, button should be disabled")
                        return@TextButton
                    }
                    
                    try {
                        val expiryDate = java.time.LocalDate.now()
                            .plusDays(expiryDays.toLongOrNull() ?: 30)
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toString()
                        
                        android.util.Log.d("AddUserDialog", "Expiry date calculated: $expiryDate")
                        
                        val request = com.kizvpn.admin.data.model.CreateUserRequest(
                            username = username.trim(),
                            email = email.takeIf { it.isNotBlank() },
                            protocol = protocol,
                            inboundId = inboundId.toIntOrNull() ?: 1,
                            expiry = expiryDate,
                            trafficLimit = (trafficLimitGb.toLongOrNull() ?: 100) * 1024 * 1024 * 1024
                        )
                        
                        android.util.Log.d("AddUserDialog", "Calling onConfirm with request")
                        onConfirm(request)
                        android.util.Log.d("AddUserDialog", "onConfirm called successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("AddUserDialog", "Error creating request", e)
                    }
                },
                enabled = username.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun UserDetailsDialog(
    user: User,
    viewModel: UsersViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val daysRemaining = user.getExpiryDateString()?.let { calculateDaysRemaining(it) }
    val scope = rememberCoroutineScope()
    var isLoadingConfig by remember { mutableStateOf(false) }
    var isLoadingSubscription by remember { mutableStateOf(false) }
    var subscriptionUrl by remember { mutableStateOf<String?>(null) }
    
    // Получаем subscription URL через ViewModel (который использует API)
    LaunchedEffect(user.id) {
        // Subscription URL должен быть получен через API, а не построен локально
        // Используем метод ViewModel для получения актуального URL
    }
    
    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(user.getDisplayName()) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Имя пользователя", user.username ?: "N/A")
                user.email?.let {
                    DetailRow("Email", it)
                }
                DetailRow("ID", user.id.toString())
                DetailRow("Протокол", user.protocol?.uppercase() ?: "N/A")
                DetailRow("Статус", user.status ?: "unknown")
                DetailRow("Трафик использовано", formatBytes(user.getTrafficUsed()))
                DetailRow("Трафик лимит", formatBytes(user.getTrafficLimit()))
                user.getExpiryDateString()?.let { expiry ->
                    DetailRow(
                        "Действителен до", 
                        formatDate(expiry) + if (daysRemaining != null) " ($daysRemaining дн.)" else ""
                    )
                }
                
                // Отображаем subscription URL если доступен
                subscriptionUrl?.let { url ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Subscription URL:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                
                // Кнопки для получения конфига и подписки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            android.util.Log.d("UsersScreen", "========== КНОПКА 'Получить конфиг' НАЖАТА для пользователя ${user.id} ==========")
                            scope.launch {
                                try {
                                    isLoadingConfig = true
                                    android.util.Log.d("UsersScreen", "Запуск получения конфига...")
                                    val result = viewModel.getUserConfig(user.id)
                                    android.util.Log.d("UsersScreen", "Результат получения конфига: success=${result.isSuccess}")
                                    isLoadingConfig = false
                                    
                                    result.onSuccess { config ->
                                        android.util.Log.d("UsersScreen", "✅ Конфиг получен, длина: ${config.length} символов")
                                        android.util.Log.d("UsersScreen", "Первые 100 символов конфига: ${config.take(100)}")
                                        copyToClipboard(config, "Subscription URL")
                                        Toast.makeText(
                                            context, 
                                            "Subscription URL скопирован!\nИспользуйте его в VPN клиенте для подписки.", 
                                            Toast.LENGTH_LONG
                                        ).show()
                                        subscriptionUrl = config // Обновляем отображаемый URL
                                    }.onFailure { error ->
                                        android.util.Log.e("UsersScreen", "❌ Ошибка получения конфига: ${error.message}", error)
                                        Toast.makeText(context, "Ошибка получения конфига: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("UsersScreen", "❌ Исключение при получении конфига", e)
                                    isLoadingConfig = false
                                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoadingConfig
                    ) {
                        if (isLoadingConfig) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Получить конфиг")
                        }
                    }
                    
                    Button(
                        onClick = {
                            android.util.Log.d("UsersScreen", "========== КНОПКА 'Ссылка на подписку' НАЖАТА для пользователя ${user.id} ==========")
                            scope.launch {
                                try {
                                    isLoadingSubscription = true
                                    android.util.Log.d("UsersScreen", "Запуск получения ссылки на подписку...")
                                    val result = viewModel.getUserSubscriptionLink(user.id)
                                    android.util.Log.d("UsersScreen", "Результат получения ссылки: success=${result.isSuccess}")
                                    isLoadingSubscription = false
                                    
                                    result.onSuccess { link ->
                                        android.util.Log.d("UsersScreen", "✅ Ссылка получена: ${link.take(100)}...")
                                        copyToClipboard(link, "Subscription URL")
                                        Toast.makeText(
                                            context, 
                                            "Subscription URL скопирован!\nИспользуйте его в VPN клиенте для подписки.", 
                                            Toast.LENGTH_LONG
                                        ).show()
                                        subscriptionUrl = link // Обновляем отображаемый URL
                                    }.onFailure { error ->
                                        android.util.Log.e("UsersScreen", "❌ Ошибка получения ссылки: ${error.message}", error)
                                        Toast.makeText(context, "Ошибка получения ссылки: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("UsersScreen", "❌ Исключение при получении ссылки", e)
                                    isLoadingSubscription = false
                                    Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoadingSubscription
                    ) {
                        if (isLoadingSubscription) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Ссылка на подписку")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        // Пробуем разные форматы даты
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        
        var date: java.util.Date? = null
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                date = inputFormat.parse(dateString)
                if (date != null) break
            } catch (e: Exception) {
                continue
            }
        }
        
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

fun calculateDaysRemaining(dateString: String): Int? {
    return try {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )
        
        var expiryDate: java.util.Date? = null
        for (format in formats) {
            try {
                val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                expiryDate = inputFormat.parse(dateString)
                if (expiryDate != null) break
            } catch (e: Exception) {
                continue
            }
        }
        
        if (expiryDate != null) {
            val now = java.util.Date()
            val diff = expiryDate.time - now.time
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
            if (days >= 0) days else null
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

