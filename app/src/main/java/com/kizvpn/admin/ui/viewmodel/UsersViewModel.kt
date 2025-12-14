package com.kizvpn.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.data.repository.VpnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedUser: User? = null
)

class UsersViewModel(
    private val repository: VpnRepository,
    private val baseUrl: String? = null // Базовый URL для subscription links
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()
    
    init {
        // Не загружаем автоматически, загрузим при первом открытии экрана
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            try {
                android.util.Log.d("UsersViewModel", "Начало загрузки пользователей")
                _uiState.update { it.copy(isLoading = true, error = null) }
                android.util.Log.d("UsersViewModel", "Получение данных из repository")
                val users = repository.getUsers().first()
                android.util.Log.d("UsersViewModel", "Получено пользователей: ${users.size}")
                _uiState.update { 
                    it.copy(
                        users = users,
                        isLoading = false,
                        error = null
                    )
                }
                android.util.Log.d("UsersViewModel", "Загрузка завершена успешно")
            } catch (e: Exception) {
                android.util.Log.e("UsersViewModel", "Ошибка загрузки пользователей", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки: ${e.message ?: "Неизвестная ошибка"}"
                    )
                }
            }
        }
    }
    
    fun searchUsers(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun selectUser(user: User) {
        _uiState.update { it.copy(selectedUser = user) }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedUser = null) }
    }
    
    fun refresh() {
        loadUsers()
    }
    
    suspend fun createUser(request: CreateUserRequest): Result<User> {
        return try {
            android.util.Log.d("UsersViewModel", "Creating user with email: ${request.email}")
            val user = repository.createUser(request)
            android.util.Log.d("UsersViewModel", "User created: id=${user.id}, email=${user.email}")
            loadUsers() // Обновляем список
            android.util.Log.d("UsersViewModel", "User list refreshed")
            Result.success(user)
        } catch (e: Exception) {
            android.util.Log.e("UsersViewModel", "Error creating user", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: Int, request: UpdateUserRequest): Result<User> {
        return try {
            val user = repository.updateUser(userId, request)
            loadUsers()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(userId: Int): Result<Unit> {
        return try {
            android.util.Log.d("UsersViewModel", "Удаление пользователя $userId")
            repository.deleteUser(userId)
            android.util.Log.d("UsersViewModel", "Пользователь $userId удален, обновляем список")
            // Обновляем список пользователей после удаления
            loadUsers()
            android.util.Log.d("UsersViewModel", "Список пользователей обновлен")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("UsersViewModel", "Ошибка при удалении пользователя $userId", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserConfig(userId: Int): Result<String> {
        return try {
            android.util.Log.d("UsersViewModel", "========== Получение конфига для пользователя $userId ==========")
            
            // Сначала пробуем найти пользователя в уже загруженном списке
            var user: User? = _uiState.value.users.find { it.id == userId }
            
            if (user == null) {
                // Если не нашли в списке, пробуем получить через API
                android.util.Log.d("UsersViewModel", "Пользователь не найден в списке, пробуем через API...")
                try {
                    user = repository.getUser(userId).first()
                    android.util.Log.d("UsersViewModel", "✅ Пользователь получен через API: id=${user.id}")
                } catch (e: Exception) {
                    android.util.Log.w("UsersViewModel", "Не удалось получить пользователя через API: ${e.message}")
                }
            } else {
                android.util.Log.d("UsersViewModel", "✅ Пользователь найден в списке: id=${user.id}, username=${user.username}")
            }
            
            // Если нашли пользователя (из списка или API), пробуем получить subscription URL из Bot API (PostgreSQL)
            if (user != null && user.id != null) {
                // Пробуем получить subscription URL из Bot API (PostgreSQL) - приоритет 1
                try {
                    android.util.Log.d("UsersViewModel", "Попытка получить subscription URL через Bot API для vpn_user_id=${user.id}")
                    val botApiResponse = repository.getSubscriptionUrlFromBot(user.id)
                    if (botApiResponse?.subscriptionUrl != null && botApiResponse.subscriptionUrl.isNotBlank()) {
                        android.util.Log.d("UsersViewModel", "✅ Subscription URL получен из Bot API: ${botApiResponse.subscriptionUrl}")
                        return Result.success(botApiResponse.subscriptionUrl)
                    } else {
                        android.util.Log.w("UsersViewModel", "Bot API вернул пустой subscription URL: ${botApiResponse?.error}")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("UsersViewModel", "Не удалось получить subscription URL из Bot API: ${e.message}")
                }
                
                // Пробуем через username в Bot API - приоритет 2
                val username = user.username
                if (username != null && username.isNotBlank()) {
                    try {
                        android.util.Log.d("UsersViewModel", "Попытка получить subscription URL через Bot API по username=$username")
                        val botApiResponse = repository.getSubscriptionUrlFromBotByUsername(username)
                        if (botApiResponse?.subscriptionUrl != null && botApiResponse.subscriptionUrl.isNotBlank()) {
                            android.util.Log.d("UsersViewModel", "✅ Subscription URL получен из Bot API по username: ${botApiResponse.subscriptionUrl}")
                            return Result.success(botApiResponse.subscriptionUrl)
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("UsersViewModel", "Не удалось получить subscription URL из Bot API по username: ${e.message}")
                    }
                }
                
                // Fallback: пробуем через PasarGuard API endpoint - приоритет 3
                try {
                    android.util.Log.d("UsersViewModel", "Попытка получить subscription URL через PasarGuard API endpoint для пользователя $userId")
                    val subscriptionMap = repository.getUserSubscription(userId)
                    android.util.Log.d("UsersViewModel", "Ответ от /api/users/{id}/subscription: $subscriptionMap")
                    
                    val subUrl = subscriptionMap["subscription_url"] 
                        ?: subscriptionMap["url"] 
                        ?: subscriptionMap["subscription"]
                        ?: subscriptionMap["link"]
                        ?: subscriptionMap.values.firstOrNull()
                    
                    if (subUrl != null && subUrl.isNotBlank()) {
                        val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: "https://host.kizvpn.ru"
                        val fullUrl = if (subUrl.startsWith("http")) {
                            subUrl
                        } else if (subUrl.startsWith("/")) {
                            "$publicBaseUrl$subUrl"
                        } else {
                            "$publicBaseUrl/$subUrl"
                        }
                        android.util.Log.d("UsersViewModel", "✅ Subscription URL получен через PasarGuard API endpoint: $fullUrl")
                        return Result.success(fullUrl)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("UsersViewModel", "Не удалось получить subscription URL через PasarGuard API endpoint: ${e.message}")
                }
                
                // Если не получили через endpoint, пробуем построить из username (fallback)
                val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: "https://host.kizvpn.ru"
                android.util.Log.d("UsersViewModel", "   baseUrl=$baseUrl")
                android.util.Log.d("UsersViewModel", "   publicBaseUrl=$publicBaseUrl")
                android.util.Log.d("UsersViewModel", "   username=${user.username}")
                android.util.Log.d("UsersViewModel", "   subscriptionUrl=${user.subscriptionUrl}")
                android.util.Log.d("UsersViewModel", "   subscription=${user.subscription}")
                
                val subscriptionUrl = user.getSubscriptionUrl(publicBaseUrl)
                android.util.Log.d("UsersViewModel", "   getSubscriptionUrl() вернул: $subscriptionUrl")
                
                if (subscriptionUrl != null && subscriptionUrl.isNotBlank()) {
                    android.util.Log.d("UsersViewModel", "✅ Subscription URL построен из username: $subscriptionUrl")
                    return Result.success(subscriptionUrl)
                } else {
                    android.util.Log.w("UsersViewModel", "⚠️ getSubscriptionUrl() вернул null или пустую строку")
                }
            }
            
            // Если не получили из User, пробуем через endpoint /api/users/{id}/config
            val configMap = repository.getUserConfig(userId)
            android.util.Log.d("UsersViewModel", "Ответ API получен: $configMap")
            android.util.Log.d("UsersViewModel", "Тип ответа: ${configMap::class.simpleName}")
            android.util.Log.d("UsersViewModel", "Ключи в ответе: ${configMap.keys}")
            
            // API может вернуть конфиг в разных полях: "config", "link", "subscription", "vless", "vmess"
            var config: String? = null
            
            // Пробуем разные возможные ключи
            val possibleKeys = listOf("config", "link", "subscription", "vless", "vmess", "wireguard", "wg", "connection", "connection_string")
            for (key in possibleKeys) {
                val value = configMap[key]
                android.util.Log.d("UsersViewModel", "Проверяем ключ '$key': $value (тип: ${value?.javaClass?.simpleName})")
                if (value != null) {
                    config = when (value) {
                        is String -> value
                        is Map<*, *> -> {
                            // Если значение - это Map, пробуем найти строку внутри
                            val mapStr = value.toString()
                            android.util.Log.d("UsersViewModel", "Значение для '$key' - это Map, преобразуем в строку: $mapStr")
                            mapStr
                        }
                        else -> value.toString()
                    }
                    if (config != null && config.isNotBlank()) {
                        android.util.Log.d("UsersViewModel", "✅ Конфиг найден в ключе '$key'")
                        break
                    }
                }
            }
            
            // Если не нашли по ключам, пробуем все значения
            if (config == null || config.isBlank()) {
                android.util.Log.d("UsersViewModel", "Конфиг не найден по ключам, проверяем все значения")
                for ((key, value) in configMap) {
                    val strValue = when (value) {
                        is String -> value
                        else -> value?.toString() ?: ""
                    }
                    if (strValue.isNotBlank() && strValue.length > 10) { // Предполагаем, что конфиг - это длинная строка
                        config = strValue
                        android.util.Log.d("UsersViewModel", "✅ Конфиг найден в значении ключа '$key'")
                        break
                    }
                }
            }
            
            if (config != null && config.isNotBlank()) {
                android.util.Log.d("UsersViewModel", "✅ Конфиг получен успешно, длина: ${config.length} символов")
                Result.success(config)
            } else {
                android.util.Log.w("UsersViewModel", "❌ Конфиг не найден в ответе. Все ключи и значения: $configMap")
                Result.failure(Exception("Конфиг не найден в ответе API. Ответ: $configMap"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("UsersViewModel", "❌ HTTP ошибка получения конфига: ${e.code()}, body: $errorBody", e)
            Result.failure(Exception("Ошибка получения конфига: HTTP ${e.code()} - ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            android.util.Log.e("UsersViewModel", "❌ Общая ошибка получения конфига", e)
            Result.failure(Exception("Ошибка получения конфига: ${e.message ?: "Неизвестная ошибка"}"))
        }
    }
    
    suspend fun getUserSubscriptionLink(userId: Int): Result<String> {
        return try {
            android.util.Log.d("UsersViewModel", "========== Получение ссылки на подписку для пользователя $userId ==========")
            
            // Сначала пробуем найти пользователя в уже загруженном списке
            var user: User? = _uiState.value.users.find { it.id == userId }
            
            if (user == null) {
                // Если не нашли в списке, пробуем получить через API
                android.util.Log.d("UsersViewModel", "Пользователь не найден в списке, пробуем через API...")
                try {
                    user = repository.getUser(userId).first()
                    android.util.Log.d("UsersViewModel", "✅ Пользователь получен через API: id=${user.id}")
                } catch (e: Exception) {
                    android.util.Log.w("UsersViewModel", "Не удалось получить пользователя через API: ${e.message}")
                }
            } else {
                android.util.Log.d("UsersViewModel", "✅ Пользователь найден в списке: id=${user.id}, username=${user.username}")
            }
            
            // Если нашли пользователя (из списка или API), строим subscription URL
            if (user != null) {
                val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: "https://host.kizvpn.ru"
                val subscriptionUrl = user.getSubscriptionUrl(publicBaseUrl)
                if (subscriptionUrl != null && subscriptionUrl.isNotBlank()) {
                    android.util.Log.d("UsersViewModel", "✅ Subscription URL получен: $subscriptionUrl")
                    return Result.success(subscriptionUrl)
                }
            }
            
            // Если не получили из User, пробуем через endpoint /api/users/{id}/subscription
            val subscriptionMap = repository.getUserSubscription(userId)
            android.util.Log.d("UsersViewModel", "Ответ API получен: $subscriptionMap")
            android.util.Log.d("UsersViewModel", "Ключи в ответе: ${subscriptionMap.keys}")
            
            // API может вернуть ссылку в разных полях: "link", "subscription", "url"
            val possibleKeys = listOf("link", "subscription", "url", "subscribe_url", "sub_url")
            var link: String? = null
            
            for (key in possibleKeys) {
                val value = subscriptionMap[key]
                android.util.Log.d("UsersViewModel", "Проверяем ключ '$key': $value")
                if (value != null && value.isNotBlank()) {
                    link = value
                    android.util.Log.d("UsersViewModel", "✅ Ссылка найдена в ключе '$key'")
                    break
                }
            }
            
            // Если не нашли по ключам, пробуем первое значение
            if (link == null || link.isBlank()) {
                link = subscriptionMap.values.firstOrNull()
                android.util.Log.d("UsersViewModel", "Используем первое значение из ответа: $link")
            }
            
            if (link != null && link.isNotBlank()) {
                android.util.Log.d("UsersViewModel", "✅ Ссылка на подписку получена успешно")
                Result.success(link)
            } else {
                android.util.Log.w("UsersViewModel", "❌ Ссылка на подписку не найдена в ответе. Все ключи и значения: $subscriptionMap")
                Result.failure(Exception("Ссылка на подписку не найдена в ответе API. Ответ: $subscriptionMap"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("UsersViewModel", "❌ HTTP ошибка получения ссылки: ${e.code()}, body: $errorBody", e)
            Result.failure(Exception("Ошибка получения ссылки: HTTP ${e.code()} - ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            android.util.Log.e("UsersViewModel", "❌ Общая ошибка получения ссылки на подписку", e)
            Result.failure(Exception("Ошибка получения ссылки: ${e.message ?: "Неизвестная ошибка"}"))
        }
    }
    
    val filteredUsers: StateFlow<List<User>> = combine(
        _uiState,
        _uiState.map { it.searchQuery }
    ) { state, query ->
        if (query.isBlank()) {
            state.users
        } else {
            state.users.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                user.email?.contains(query, ignoreCase = true) == true ||
                user.protocol?.contains(query, ignoreCase = true) == true ||
                user.status?.contains(query, ignoreCase = true) == true ||
                user.id.toString().contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}

