package com.kizvpn.admin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kizvpn.admin.data.repository.AuthRepository
import com.kizvpn.admin.util.BiometricHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Вспомогательная функция для биометрической аутентификации
 */
fun authenticateWithBiometric(
    activity: androidx.fragment.app.FragmentActivity?,
    authRepository: AuthRepository?,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    if (activity == null) {
        android.util.Log.e("LoginScreen", "Activity is null, cannot show biometric")
        onError("Ошибка: Activity недоступна")
        return
    }
    
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    
    scope.launch {
        try {
            val apiUrl = authRepository?.apiUrl?.first() ?: ""
            val token = authRepository?.token?.first() ?: ""
            
            if (apiUrl.isEmpty() || token.isEmpty()) {
                onError("Нет сохраненных учетных данных")
                return@launch
            }
            
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Биометрическая аутентификация",
                subtitle = "Используйте отпечаток пальца или Face ID для входа в панель администратора",
                onSuccess = {
                    onSuccess(apiUrl, token)
                },
                onError = { error ->
                    android.util.Log.e("LoginScreen", "Ошибка биометрии: $error")
                    onError("Ошибка биометрии: $error")
                },
                onFailed = {
                    android.util.Log.w("LoginScreen", "Биометрическая аутентификация не удалась")
                    onError("Биометрическая аутентификация не удалась. Попробуйте снова.")
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("LoginScreen", "Критическая ошибка при биометрии", e)
            onError("Ошибка: ${e.message ?: "Неизвестная ошибка"}")
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    authRepository: AuthRepository? = null,
    mainActivity: androidx.appcompat.app.AppCompatActivity,
    mainExecutor: java.util.concurrent.Executor
) {
    android.util.Log.d("LoginScreen", "LoginScreen создан")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Загружаем сохраненные данные из DataStore
    val savedApiUrl by authRepository?.apiUrl?.collectAsState(initial = null) ?: remember { mutableStateOf<String?>(null) }
    val savedToken by authRepository?.token?.collectAsState(initial = null) ?: remember { mutableStateOf<String?>(null) }
    val biometricEnabled by authRepository?.biometricEnabled?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    
    var apiUrl by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInfo by remember { mutableStateOf(false) }
    var showTokenEntry by remember { mutableStateOf(false) } // Флаг для показа формы ввода токена
    
    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    val hasSavedCredentials = remember(savedApiUrl, savedToken) {
        !savedApiUrl.isNullOrEmpty() && !savedToken.isNullOrEmpty()
    }
    val shouldShowBiometric = remember(biometricEnabled, isBiometricAvailable, hasSavedCredentials) {
        val result = biometricEnabled && isBiometricAvailable && hasSavedCredentials
        android.util.Log.d("LoginScreen", "shouldShowBiometric: $result (enabled=$biometricEnabled, available=$isBiometricAvailable, hasCreds=$hasSavedCredentials)")
        result
    }
    
    // Загружаем сохраненные значения при первом рендере
    LaunchedEffect(savedApiUrl, savedToken) {
        savedApiUrl?.let { if (it.isNotEmpty()) apiUrl = it }
        savedToken?.let { if (it.isNotEmpty()) token = it }
    }
    
    // Автоматически показываем биометрию при открытии, если она доступна
    // Запускаем только один раз, когда биометрия становится доступной
    LaunchedEffect(Unit) {
        if (shouldShowBiometric && !showTokenEntry) {
            // Небольшая задержка для лучшего UX и чтобы UI успел отрисоваться
            kotlinx.coroutines.delay(800)
            if (shouldShowBiometric && !showTokenEntry) { // Проверяем еще раз, на случай если пользователь успел переключиться
                try {
                    authenticateWithBiometric(
                        activity = mainActivity as? androidx.fragment.app.FragmentActivity,
                        authRepository = authRepository,
                        onSuccess = onLoginSuccess,
                        onError = { error ->
                            errorMessage = error
                            // При ошибке не переключаем на форму ввода, пользователь может попробовать снова
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("LoginScreen", "Ошибка автоматической биометрии", e)
                    errorMessage = "Ошибка биометрии: ${e.message}"
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "KIZ VPN Admin Panel",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Если биометрия доступна и включена, показываем ТОЛЬКО биометрию (если не выбрана форма ввода)
        // Форма ввода токена должна быть полностью скрыта в этом случае
        if (shouldShowBiometric && !showTokenEntry) {
            // Экран биометрической аутентификации
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                LaunchedEffect(Unit) {
                    android.util.Log.d("LoginScreen", "Показываем экран биометрии (shouldShowBiometric=$shouldShowBiometric, showTokenEntry=$showTokenEntry)")
                }
                // Большая иконка отпечатка
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Биометрическая аутентификация",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Text(
                    text = "Используйте отпечаток пальца или Face ID для входа",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Кнопка для повторной попытки биометрии
                Button(
                    onClick = {
                        errorMessage = null
                        try {
                            authenticateWithBiometric(
                                activity = mainActivity as? androidx.fragment.app.FragmentActivity,
                                authRepository = authRepository,
                                onSuccess = onLoginSuccess,
                                onError = { error ->
                                    errorMessage = error
                                }
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("LoginScreen", "Ошибка при нажатии на биометрию", e)
                            errorMessage = "Ошибка: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Войти с биометрией")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Кнопка для перехода к форме ввода токена
                TextButton(
                    onClick = { showTokenEntry = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Войти через токен")
                }
            }
        } else {
            // Обычная форма ввода токена
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LaunchedEffect(Unit) {
                    android.util.Log.d("LoginScreen", "Показываем форму ввода токена (shouldShowBiometric=$shouldShowBiometric, showTokenEntry=$showTokenEntry)")
                }
                
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("URL API") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://your-api-server.com/api") },
                    supportingText = {
                        Text("Адрес API панели PasarGuard", style = MaterialTheme.typography.bodySmall)
                    }
                )
                
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("JWT токен") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Вставь JWT токен сюда") },
                    supportingText = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Получить токен",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { showInfo = !showInfo },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Инструкция",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
                
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Если биометрия доступна, но пользователь выбрал форму ввода токена, показываем кнопку возврата
                if (shouldShowBiometric && showTokenEntry) {
                    OutlinedButton(
                        onClick = {
                            showTokenEntry = false
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Вернуться к биометрии")
                    }
                }
                
                Button(
                    onClick = {
                        if (apiUrl.isBlank() || token.isBlank()) {
                            errorMessage = "Заполните все поля"
                            return@Button
                        }
                        
                        isLoading = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                // Сохраняем данные для следующего входа
                                authRepository?.saveApiUrl(apiUrl.trim())
                                authRepository?.saveToken(token.trim())
                                
                                // TODO: Проверить токен через API перед переходом
                                // Пока просто сохраняем и переходим дальше
                                onLoginSuccess(apiUrl.trim(), token.trim())
                            } catch (e: Exception) {
                                errorMessage = "Ошибка входа: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Войти")
                    }
                }
                
                // Инструкция в расширяемой карточке
                if (showInfo) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Как получить JWT токен:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "1. Открой в браузере: https://your-panel-url.com/",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "2. Войди в панель (логин/пароль админа)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "3. Нажми F12 (DevTools) → вкладка Network",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "4. Выполни любое действие в панели (например, открой список пользователей)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "5. Найди любой запрос → Headers → найди 'Authorization: Bearer ...'",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "6. Скопируй текст после 'Bearer ' (это твой JWT токен)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "7. Вставь токен в поле выше и нажми 'Войти'",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (!showInfo) {
                    TextButton(
                        onClick = { showInfo = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Показать инструкцию по получению токена")
                    }
                }
            }
        }
    }
}

