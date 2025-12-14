package com.kizvpn.admin.data.repository

import com.kizvpn.admin.data.api.ApiClient
import com.kizvpn.admin.data.model.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException

class VpnRepository(private val apiClient: ApiClient) {
    
    val api = apiClient.pasarguardApi
    
    fun getSystemInfo() = flow {
        try {
            android.util.Log.d("VpnRepository", "Попытка получить системную информацию через /api/system")
            
            // Пробуем получить сырой ответ для отладки
            try {
                val rawResponse = apiClient.pasarguardApi.getSystemInfo()
                android.util.Log.d("VpnRepository", "========== Сырой SystemInfo объект ==========")
                android.util.Log.d("VpnRepository", "SystemInfo.toString(): $rawResponse")
            } catch (e: Exception) {
                android.util.Log.w("VpnRepository", "Не удалось залогировать сырой ответ", e)
            }
            
            val systemInfo = api.getSystemInfo()
            android.util.Log.d("VpnRepository", "✅ Системная информация получена")
            android.util.Log.d("VpnRepository", "========== SystemInfo Raw ==========")
            android.util.Log.d("VpnRepository", "  version: ${systemInfo.version}")
            android.util.Log.d("VpnRepository", "  buildTime: ${systemInfo.buildTime}")
            android.util.Log.d("VpnRepository", "  cpuUsage: ${systemInfo.cpuUsage}")
            android.util.Log.d("VpnRepository", "  cpuPercent: ${systemInfo.cpuPercent}")
            android.util.Log.d("VpnRepository", "  cpuCores: ${systemInfo.cpuCores}")
            android.util.Log.d("VpnRepository", "  ramUsage: ${systemInfo.ramUsage}")
            android.util.Log.d("VpnRepository", "  ramTotal: ${systemInfo.ramTotal}")
            android.util.Log.d("VpnRepository", "  ramPercent: ${systemInfo.ramPercent}")
            android.util.Log.d("VpnRepository", "  memoryUsage: ${systemInfo.memoryUsage}")
            android.util.Log.d("VpnRepository", "  memoryTotal: ${systemInfo.memoryTotal}")
            android.util.Log.d("VpnRepository", "  memUsage: ${systemInfo.memUsage}")
            android.util.Log.d("VpnRepository", "  memUsed: ${systemInfo.memUsed}")
            android.util.Log.d("VpnRepository", "  memTotal: ${systemInfo.memTotal}")
            android.util.Log.d("VpnRepository", "  mem: ${systemInfo.mem}")
            android.util.Log.d("VpnRepository", "  memory: ${systemInfo.memory}")
            android.util.Log.d("VpnRepository", "  system: ${systemInfo.system}")
            android.util.Log.d("VpnRepository", "========== SystemInfo Computed ==========")
            android.util.Log.d("VpnRepository", "  getCpuUsage(): ${systemInfo.getCpuUsage()}")
            android.util.Log.d("VpnRepository", "  getCpuCores(): ${systemInfo.getCpuCores()}")
            android.util.Log.d("VpnRepository", "  getRamUsage(): ${systemInfo.getRamUsage()}")
            android.util.Log.d("VpnRepository", "  getRamTotal(): ${systemInfo.getRamTotal()}")
            android.util.Log.d("VpnRepository", "  getRamUsagePercent(): ${systemInfo.getRamUsagePercent()}")
            
            // Если ramUsage = 0, но есть ramPercent и ramTotal, вычисляем usage
            if (systemInfo.getRamUsage() == 0L && systemInfo.ramPercent != null && systemInfo.getRamTotal() > 0) {
                val computedUsage = (systemInfo.ramPercent!! * systemInfo.getRamTotal() / 100.0).toLong()
                android.util.Log.d("VpnRepository", "  ⚠️ ramUsage = 0, вычисляем из ramPercent: ${systemInfo.ramPercent}% * ${systemInfo.getRamTotal()} = $computedUsage")
            }
            
            emit(systemInfo)
        } catch (e: Exception) {
            // Игнорируем AbortFlowException - это нормальное поведение flow.first()
            if (e::class.simpleName != "AbortFlowException") {
                android.util.Log.w("VpnRepository", "GET /api/system ошибка: ${e.message}")
                // Возвращаем пустой SystemInfo
                emit(com.kizvpn.admin.data.model.SystemInfo())
            } else {
                throw e
            }
        }
    }
    
    fun getUsers() = flow {
        try {
            val response = api.getUsers()
            val users = response.getUsersList()
            // Логируем для отладки
            android.util.Log.d("VpnRepository", "Получено пользователей: ${users.size}")
            
            // Логируем сырой ответ для проверки полей
            try {
                val rawResponse = apiClient.pasarguardApi.getUsers()
                android.util.Log.d("VpnRepository", "Сырой ответ API (первые 1000 символов): ${rawResponse.toString().take(1000)}")
            } catch (e: Exception) {
                android.util.Log.w("VpnRepository", "Не удалось залогировать сырой ответ", e)
            }
            
            users.forEach { user ->
                android.util.Log.d("VpnRepository", 
                    "User ${user.id}: username=${user.username}, email=${user.email}, " +
                    "trafficUsed=${user.trafficUsed}, trafficLimit=${user.trafficLimit}, " +
                    "expiry=${user.expiryDate ?: user.expire}, " +
                    "subscriptionUrl='${user.subscriptionUrl}', subscription='${user.subscription}'"
                )
            }
            emit(users)
        } catch (e: Exception) {
            // Игнорируем AbortFlowException (это нормальное поведение flow.first())
            // Проверяем по имени класса, так как это internal класс
            if (e::class.simpleName == "AbortFlowException") {
                throw e
            }
            android.util.Log.e("VpnRepository", "Ошибка получения пользователей", e)
            throw e
        }
    }
    
    fun getUser(userId: Int) = flow {
        android.util.Log.d("VpnRepository", "Запрос пользователя $userId через GET /api/users/$userId")
        val user = api.getUser(userId)
        android.util.Log.d("VpnRepository", "Получен пользователь: id=${user.id}, username=${user.username}, email=${user.email}")
        android.util.Log.d("VpnRepository", "   subscriptionUrl=${user.subscriptionUrl}, subscription=${user.subscription}")
        android.util.Log.d("VpnRepository", "   Все поля пользователя: $user")
        emit(user)
    }
    
    suspend fun createUser(request: CreateUserRequest): User {
        // Пытаемся создать пользователя через несколько endpoint'ов
        // Сначала пробуем /api/users
        try {
            android.util.Log.d("VpnRepository", "Попытка создания через POST /api/users")
            android.util.Log.d("VpnRepository", "Request: username=${request.username}, email=${request.email}, protocol=${request.protocol}, inboundId=${request.inboundId}, expiry=${request.expiry}, trafficLimit=${request.trafficLimit}")
            val user = api.createUser(request)
            android.util.Log.d("VpnRepository", "✅ User created successfully via /api/users: id=${user.id}, username=${user.username}, email=${user.email}")
            android.util.Log.d("VpnRepository", "   subscriptionUrl='${user.subscriptionUrl}', subscription='${user.subscription}'")
            android.util.Log.d("VpnRepository", "   Все поля созданного пользователя: $user")
            return user
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("VpnRepository", "HTTP ${e.code()} error: $errorBody")
            
            if (e.code() == 405) {
                android.util.Log.w("VpnRepository", "POST /api/users вернул 405, пробуем /api/user")
                try {
                    android.util.Log.d("VpnRepository", "Попытка создания через POST /api/user")
                    val user = api.createUserSingular(request)
                    android.util.Log.d("VpnRepository", "✅ User created successfully via /api/user: id=${user.id}, username=${user.username}, email=${user.email}")
                    android.util.Log.d("VpnRepository", "   subscriptionUrl='${user.subscriptionUrl}', subscription='${user.subscription}'")
                    android.util.Log.d("VpnRepository", "   Все поля созданного пользователя: $user")
                    return user
                } catch (e2: retrofit2.HttpException) {
                    val errorBody2 = e2.response()?.errorBody()?.string()
                    android.util.Log.e("VpnRepository", "❌ Error creating user via /api/user: HTTP ${e2.code()}, body: $errorBody2", e2)
                    throw Exception("Не удалось создать пользователя. API вернул ошибку: HTTP ${e2.code()} - ${errorBody2 ?: e2.message()}")
                } catch (e2: Exception) {
                    android.util.Log.e("VpnRepository", "❌ Error creating user via /api/user", e2)
                    throw Exception("Не удалось создать пользователя. API вернул ошибку: ${e2.message ?: "Неизвестная ошибка"}")
                }
            } else if (e.code() == 422) {
                android.util.Log.e("VpnRepository", "❌ Validation error (422): $errorBody")
                throw Exception("Ошибка валидации данных: ${errorBody ?: "Проверьте правильность введенных данных"}")
            } else {
                android.util.Log.e("VpnRepository", "❌ HTTP error creating user: ${e.code()}", e)
                throw Exception("Ошибка создания пользователя: HTTP ${e.code()} - ${errorBody ?: e.message()}")
            }
        } catch (e: Exception) {
            if (e is retrofit2.HttpException) {
                // Уже обработано выше
                throw e
            }
            android.util.Log.e("VpnRepository", "❌ Error creating user", e)
            throw e
        }
    }
    
    suspend fun updateUser(userId: Int, request: UpdateUserRequest): User {
        return api.updateUser(userId, request)
    }
    
    suspend fun deleteUser(userId: Int) {
        val updateRequest = UpdateUserRequest(status = "disabled")
        
        // Пробуем DELETE /api/users/{id}
        try {
            android.util.Log.d("VpnRepository", "Попытка удаления пользователя $userId через DELETE /api/users/$userId")
            api.deleteUser(userId)
            android.util.Log.d("VpnRepository", "DELETE /api/users/$userId выполнен успешно для пользователя $userId")
            return
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 404) {
                android.util.Log.w("VpnRepository", "DELETE /api/users/ вернул ${e.code()}, пробуем следующий метод")
            } else {
                android.util.Log.w("VpnRepository", "DELETE /api/users/ вернул 404, пробуем /api/user/")
            }
        } catch (e: Exception) {
            val is404 = e.message?.contains("404") == true || 
                       e.message?.contains("Not Found") == true
            if (!is404) {
                android.util.Log.w("VpnRepository", "DELETE /api/users/ ошибка (не 404): ${e.message}")
            }
        }
        
        // Пробуем DELETE /api/user/{id}
        try {
            android.util.Log.d("VpnRepository", "Попытка удаления пользователя $userId через DELETE /api/user/$userId")
            api.deleteUserSingular(userId)
            android.util.Log.d("VpnRepository", "DELETE /api/user/$userId выполнен успешно для пользователя $userId")
            return
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 404) {
                android.util.Log.w("VpnRepository", "DELETE /api/user/ вернул ${e.code()}, пробуем деактивацию")
            } else {
                android.util.Log.w("VpnRepository", "DELETE /api/user/ вернул 404, пробуем деактивацию")
            }
        } catch (e: Exception) {
            val is404 = e.message?.contains("404") == true || 
                       e.message?.contains("Not Found") == true
            if (!is404) {
                android.util.Log.w("VpnRepository", "DELETE /api/user/ ошибка (не 404): ${e.message}")
            }
        }
        
        // Если все DELETE методы вернули 404, пробуем деактивировать через PUT/PATCH
        android.util.Log.d("VpnRepository", "Все DELETE методы вернули 404, пробуем деактивировать пользователя $userId")
        
        // Пробуем PUT /api/users/{id}
        try {
            android.util.Log.d("VpnRepository", "Попытка деактивации через PUT /api/users/$userId")
            api.updateUser(userId, updateRequest)
            android.util.Log.d("VpnRepository", "PUT /api/users/$userId выполнен успешно, пользователь $userId деактивирован")
            return
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 404) {
                android.util.Log.w("VpnRepository", "PUT /api/users/ вернул ${e.code()}, пробуем следующий")
            } else {
                android.util.Log.w("VpnRepository", "PUT /api/users/ вернул 404, пробуем /api/user/")
            }
        } catch (e: Exception) {
            val is404 = e.message?.contains("404") == true || 
                       e.message?.contains("Not Found") == true
            if (!is404) {
                android.util.Log.w("VpnRepository", "PUT /api/users/ ошибка (не 404): ${e.message}")
            }
        }
        
        // Пробуем PUT /api/user/{id}
        try {
            android.util.Log.d("VpnRepository", "Попытка деактивации через PUT /api/user/$userId")
            api.updateUserSingular(userId, updateRequest)
            android.util.Log.d("VpnRepository", "PUT /api/user/$userId выполнен успешно, пользователь $userId деактивирован")
            return
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 404) {
                android.util.Log.w("VpnRepository", "PUT /api/user/ вернул ${e.code()}, пробуем PATCH")
            } else {
                android.util.Log.w("VpnRepository", "PUT /api/user/ вернул 404, пробуем PATCH")
            }
        } catch (e: Exception) {
            val is404 = e.message?.contains("404") == true || 
                       e.message?.contains("Not Found") == true
            if (!is404) {
                android.util.Log.w("VpnRepository", "PUT /api/user/ ошибка (не 404): ${e.message}")
            }
        }
        
        // Пробуем PATCH /api/users/{id}
        try {
            android.util.Log.d("VpnRepository", "Попытка деактивации через PATCH /api/users/$userId")
            api.patchUser(userId, updateRequest)
            android.util.Log.d("VpnRepository", "PATCH /api/users/$userId выполнен успешно, пользователь $userId деактивирован")
            return
        } catch (e: retrofit2.HttpException) {
            if (e.code() != 404) {
                android.util.Log.w("VpnRepository", "PATCH вернул ${e.code()}")
            } else {
                android.util.Log.w("VpnRepository", "PATCH вернул 404")
            }
        } catch (e: Exception) {
            val is404 = e.message?.contains("404") == true || 
                       e.message?.contains("Not Found") == true
            if (!is404) {
                android.util.Log.w("VpnRepository", "PATCH ошибка (не 404): ${e.message}")
            }
        }
        
        // Если все методы вернули 404, значит API не поддерживает удаление/обновление
        android.util.Log.e("VpnRepository", "Все методы удаления/деактивации вернули 404 для пользователя $userId")
        throw Exception("API не поддерживает удаление или обновление пользователей через REST API. Используйте веб-панель для удаления.")
    }
    
    suspend fun getUserSubscription(userId: Int): Map<String, String> {
        return try {
            android.util.Log.d("VpnRepository", "Попытка получения подписки через GET /api/users/$userId/subscription")
            val result = api.getUserSubscription(userId)
            android.util.Log.d("VpnRepository", "✅ Subscription получен: $result")
            result
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.w("VpnRepository", "GET /api/users/$userId/subscription вернул ${e.code()}, body: $errorBody")
            if (e.code() == 404) {
                android.util.Log.w("VpnRepository", "Пробуем /api/user/$userId/subscription")
                try {
                    val result = api.getUserSubscriptionSingular(userId)
                    android.util.Log.d("VpnRepository", "✅ Subscription получен через /api/user/{id}/subscription: $result")
                    result
                } catch (e2: Exception) {
                    android.util.Log.e("VpnRepository", "Ошибка получения подписки через /api/user/{id}/subscription", e2)
                    throw e2
                }
            } else {
                throw e
            }
        }
    }
    
    suspend fun getUserConfig(userId: Int): Map<String, Any> {
        return try {
            android.util.Log.d("VpnRepository", "Попытка получения конфига через GET /api/users/{id}/config")
            api.getUserConfig(userId)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                android.util.Log.w("VpnRepository", "GET /api/users/{id}/config вернул 404, пробуем /api/user/{id}/config")
                try {
                    api.getUserConfigSingular(userId)
                } catch (e2: Exception) {
                    android.util.Log.e("VpnRepository", "Ошибка получения конфига через /api/user/{id}/config", e2)
                    // Если и это не сработало, пробуем получить через детальный запрос пользователя
                    android.util.Log.w("VpnRepository", "Пробуем получить конфиг через GET /api/users/{id}")
                    try {
                        val user = api.getUser(userId)
                        android.util.Log.d("VpnRepository", "Получен пользователь: $user")
                        // Если в ответе есть поля конфига, вернем их
                        val configMap = mutableMapOf<String, Any>()
                        // Проверяем, может быть конфиг уже есть в объекте User
                        // Но так как User не содержит конфиг, просто пробросим ошибку
                        throw Exception("Конфиг не найден. API не поддерживает получение конфига через REST endpoints. Возможно, нужно использовать другой метод.")
                    } catch (e3: Exception) {
                        android.util.Log.e("VpnRepository", "Не удалось получить конфиг", e3)
                        throw e3
                    }
                }
            } else {
                throw e
            }
        }
    }
    
    fun getUserStats(userId: Int) = flow {
        emit(api.getUserStats(userId))
    }
    
    fun getInbounds() = flow {
        try {
            val response = api.getInbounds()
            emit(response.getInboundsList())
        } catch (e: Exception) {
            throw e
        }
    }
    
    fun getNodes() = flow {
        try {
            val response = api.getNodes()
            emit(response.getNodesList())
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun getStatsComputed(): com.kizvpn.admin.data.model.Stats {
        // Вычисляем статистику из пользователей
        val users = api.getUsers().getUsersList()
        var totalTraffic = 0L
        
        android.util.Log.d("VpnRepository", "Вычисление статистики из ${users.size} пользователей")
        users.forEach { user ->
            val used = user.getTrafficUsed()
            totalTraffic += used
            android.util.Log.d("VpnRepository", "  User ${user.id} (${user.username}): trafficUsed=${user.trafficUsed}, usedTraffic=${user.usedTraffic}, lifetimeUsedTraffic=${user.lifetimeUsedTraffic}, getTrafficUsed()=${used}")
        }
        android.util.Log.d("VpnRepository", "✅ Общий трафик: $totalTraffic байт (${totalTraffic / (1024*1024*1024)} GB)")
        
        // Пробуем получить системную информацию
        var systemInfo: com.kizvpn.admin.data.model.SystemInfo? = null
        try {
            systemInfo = api.getSystemInfo()
            android.util.Log.d("VpnRepository", "✅ Системная информация получена:")
            android.util.Log.d("VpnRepository", "  cpuUsage=${systemInfo.getCpuUsage()}")
            android.util.Log.d("VpnRepository", "  cpuCores=${systemInfo.getCpuCores()}")
            android.util.Log.d("VpnRepository", "  RAW ramUsage=${systemInfo.ramUsage}, memoryUsage=${systemInfo.memoryUsage}, memUsage=${systemInfo.memUsage}, memUsed=${systemInfo.memUsed}")
            android.util.Log.d("VpnRepository", "  RAW ramTotal=${systemInfo.ramTotal}, memoryTotal=${systemInfo.memoryTotal}, memTotal=${systemInfo.memTotal}")
            android.util.Log.d("VpnRepository", "  RAW ramPercent=${systemInfo.ramPercent}")
            android.util.Log.d("VpnRepository", "  RAW onlineUsers=${systemInfo.onlineUsers}")
            android.util.Log.d("VpnRepository", "  RAW incomingBandwidth=${systemInfo.incomingBandwidth} (${systemInfo.incomingBandwidth?.let { it / (1024*1024*1024) } ?: 0} GB)")
            android.util.Log.d("VpnRepository", "  RAW outgoingBandwidth=${systemInfo.outgoingBandwidth} (${systemInfo.outgoingBandwidth?.let { it / (1024*1024*1024) } ?: 0} GB)")
            val ramUsage = systemInfo.getRamUsage()
            val ramTotal = systemInfo.getRamTotal()
            android.util.Log.d("VpnRepository", "  COMPUTED ramUsage=${ramUsage} байт (${if (ramUsage > 0) ramUsage / (1024*1024) else 0} MB)")
            android.util.Log.d("VpnRepository", "  COMPUTED ramTotal=${ramTotal} байт (${if (ramTotal > 0) ramTotal / (1024*1024*1024) else 0} GB)")
        } catch (sysErr: Exception) {
            android.util.Log.w("VpnRepository", "Не удалось получить системную информацию: ${sysErr.message}")
        }
        
        // Вычисляем онлайн пользователей из списка
        val onlineUsersFromList = users.count { it.isOnline() }
        
        // Используем общий трафик из SystemInfo (incoming_bandwidth + outgoing_bandwidth), если доступно
        // Это более точное значение, чем суммирование трафика пользователей
        val serverTotalTraffic = if (systemInfo != null && systemInfo.incomingBandwidth != null && systemInfo.outgoingBandwidth != null) {
            val incoming = systemInfo.incomingBandwidth ?: 0L
            val outgoing = systemInfo.outgoingBandwidth ?: 0L
            val serverTotal = incoming + outgoing
            android.util.Log.d("VpnRepository", "Общий трафик сервера из SystemInfo: incoming=${incoming} (${incoming / (1024*1024*1024)} GB), outgoing=${outgoing} (${outgoing / (1024*1024*1024)} GB), total=${serverTotal} (${serverTotal / (1024*1024*1024)} GB)")
            serverTotal
        } else {
            android.util.Log.d("VpnRepository", "Используем вычисленный трафик из пользователей: ${totalTraffic} (${totalTraffic / (1024*1024*1024)} GB)")
            totalTraffic
        }
        
        // Создаем Stats объект с вычисленными данными
        return com.kizvpn.admin.data.model.Stats(
            totalUsers = users.size,
            activeUsers = users.count { it.status == "active" },
            totalTraffic = serverTotalTraffic,
            totalUp = systemInfo?.outgoingBandwidth,
            totalDown = systemInfo?.incomingBandwidth,
            // Используем onlineUsers из SystemInfo (если есть), иначе вычисляем из списка пользователей
            onlineUsers = systemInfo?.onlineUsers ?: onlineUsersFromList,
            // Системные метрики из SystemInfo
            cpuUsage = systemInfo?.getCpuUsage(),
            cpuPercent = systemInfo?.getCpuUsage(),
            cpuCores = systemInfo?.getCpuCores(),
            ramUsage = systemInfo?.getRamUsage(),
            ramTotal = systemInfo?.getRamTotal(),
            memoryUsage = systemInfo?.getRamUsage(),
            memoryTotal = systemInfo?.getRamTotal()
        )
    }
    
    fun getStats() = flow {
        try {
            // Пробуем получить через /api/stats
            android.util.Log.d("VpnRepository", "Попытка получить статистику через /api/stats")
            val stats = api.getStats()
            android.util.Log.d("VpnRepository", "✅ Статистика получена через /api/stats")
            emit(stats)
        } catch (e: Exception) {
            // Игнорируем AbortFlowException - это нормальное поведение flow.first()
            if (e::class.simpleName == "AbortFlowException") {
                throw e
            }
            
            // Если 404 или другая ошибка, возвращаем пустую статистику
            // Вычисление будет выполнено в ViewModel если нужно
            android.util.Log.w("VpnRepository", "Ошибка получения статистики: ${e.message}")
            emit(com.kizvpn.admin.data.model.Stats())
        }
    }
    
    fun getUsersStats() = flow {
        emit(api.getUsersStats())
    }
    
    suspend fun getSubscriptionUrlFromBot(vpnUserId: Int): SubscriptionUrlResponse? {
        return try {
            android.util.Log.d("VpnRepository", "Попытка получить subscription URL из Bot API для vpn_user_id=$vpnUserId")
            val botApi = apiClient.botApi
            if (botApi == null) {
                android.util.Log.w("VpnRepository", "BotApi не доступен")
                return null
            }
            val response = botApi.getSubscriptionUrl(vpnUserId)
            android.util.Log.d("VpnRepository", "✅ Bot API вернул subscription URL: ${response.subscriptionUrl?.take(50)}...")
            response
        } catch (e: Exception) {
            android.util.Log.w("VpnRepository", "Ошибка получения subscription URL из Bot API: ${e.message}")
            null
        }
    }
    
    suspend fun getSubscriptionUrlFromBotByUsername(username: String): SubscriptionUrlResponse? {
        return try {
            android.util.Log.d("VpnRepository", "Попытка получить subscription URL из Bot API для username=$username")
            val botApi = apiClient.botApi
            if (botApi == null) {
                android.util.Log.w("VpnRepository", "BotApi не доступен")
                return null
            }
            val response = botApi.getSubscriptionUrlByUsername(username)
            android.util.Log.d("VpnRepository", "✅ Bot API вернул subscription URL: ${response.subscriptionUrl?.take(50)}...")
            response
        } catch (e: Exception) {
            android.util.Log.w("VpnRepository", "Ошибка получения subscription URL из Bot API: ${e.message}")
            null
        }
    }
    
    fun getNodesStats() = flow {
        emit(api.getNodesStats())
    }
    
    suspend fun rebootServer(serverIp: String): Result<ServerRebootResponse> {
        return try {
            android.util.Log.d("VpnRepository", "Попытка перезагрузки сервера $serverIp")
            val botApi = apiClient.botApi
            if (botApi == null) {
                android.util.Log.e("VpnRepository", "Bot API клиент не инициализирован.")
                return Result.failure(Exception("Bot API недоступен. Убедитесь, что Bot API сервер запущен и доступен."))
            }
            
            android.util.Log.d("VpnRepository", "Отправка запроса POST /api/server/$serverIp/reboot")
            val response = botApi.rebootServer(serverIp)
            android.util.Log.d("VpnRepository", "✅ Ответ от Bot API: ${response.message}")
            Result.success(response)
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("VpnRepository", "Таймаут подключения к Bot API", e)
            Result.failure(Exception("Таймаут подключения к Bot API. Проверьте, что Bot API сервер запущен и доступен."))
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("VpnRepository", "Ошибка подключения к Bot API", e)
            Result.failure(Exception("Не удалось подключиться к Bot API. Убедитесь, что Bot API сервер запущен."))
        } catch (e: java.io.IOException) {
            android.util.Log.e("VpnRepository", "Ошибка ввода-вывода при подключении к Bot API", e)
            val errorMsg = when {
                e.message?.contains("unexpected end of stream") == true -> 
                    "Соединение с Bot API разорвано. Возможно, Bot API сервер не запущен или недоступен."
                e.message?.contains("Connection reset") == true -> 
                    "Соединение с Bot API было сброшено. Проверьте, что Bot API сервер запущен."
                else -> 
                    "Ошибка сети: ${e.message}. Проверьте подключение к Bot API серверу."
            }
            Result.failure(Exception(errorMsg))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("VpnRepository", "HTTP ошибка при перезагрузке сервера: ${e.code()}, body: $errorBody", e)
            Result.failure(Exception("Ошибка сервера (HTTP ${e.code()}): ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            android.util.Log.e("VpnRepository", "Неизвестная ошибка при перезагрузке сервера $serverIp: ${e.message}", e)
            Result.failure(Exception("Ошибка перезагрузки сервера: ${e.message ?: "Неизвестная ошибка"}"))
        }
    }
}

