package com.kizvpn.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.data.repository.VpnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

data class DashboardUiState(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val onlineUsers: Int = 0,
    val totalNodes: Int = 0,
    val activeNodes: Int = 0,
    val totalTraffic: Long = 0,
    val totalUpload: Long = 0,
    val totalDownload: Long = 0,
    // Системные метрики
    val cpuUsage: Double = 0.0,
    val cpuCores: Int = 0,
    val ramUsage: Long = 0,
    val ramTotal: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val repository: VpnRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        // Загружаем данные при создании
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                launch { loadUsers() }
                launch { loadNodes() }
                launch { loadStats() }
                launch { loadSystemInfo() }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки данных: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun loadUsers() {
        try {
            repository.getUsers().catch { e ->
                // Игнорируем AbortFlowException (это нормальное поведение flow.first())
                if (e::class.simpleName == "AbortFlowException") {
                    return@catch
                }
                throw e
            }.first().let { users ->
                // Вычисляем онлайн пользователей по полю online_at
                // Считаем пользователя онлайн, если online_at был в последние 5 минут
                val now = System.currentTimeMillis()
                val fiveMinutesAgo = now - (5 * 60 * 1000)
                
                val onlineCount = users.count { user ->
                    if (user.status != "active") return@count false
                    
                    user.onlineAt?.let { onlineAtStr ->
                        try {
                            // Парсим ISO 8601 формат даты (например: "2025-12-14T08:18:07.099500Z")
                            val dateTime = java.time.Instant.parse(onlineAtStr)
                            val onlineAtMillis = dateTime.toEpochMilli()
                            onlineAtMillis > fiveMinutesAgo
                        } catch (e: Exception) {
                            // Если не удалось распарсить, не считаем онлайн
                            false
                        }
                    } ?: false
                }
                
                _uiState.update { 
                    it.copy(
                        totalUsers = users.size,
                        activeUsers = users.count { it.status == "active" },
                        onlineUsers = onlineCount
                    )
                }
            }
        } catch (e: Exception) {
            // Игнорируем AbortFlowException (это нормальное поведение flow.first())
            // Проверяем по имени класса, так как это internal класс
            if (e::class.simpleName != "AbortFlowException") {
                android.util.Log.w("DashboardViewModel", "Ошибка загрузки пользователей: ${e.message}")
            }
            // Игнорируем ошибки отдельных запросов
        }
    }
    
    private suspend fun loadNodes() {
        try {
            repository.getNodes().first().let { nodes ->
                _uiState.update { 
                    it.copy(
                        totalNodes = nodes.size,
                        activeNodes = nodes.count { it.status == "active" || it.status == null }
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("DashboardViewModel", "Ошибка загрузки нод: ${e.message}")
            // Игнорируем ошибки отдельных запросов
        }
    }
    
    private suspend fun loadStats() {
        // Всегда сначала пробуем вычислить из пользователей, так как /api/stats может быть недоступен
        try {
            android.util.Log.d("DashboardViewModel", "Вычисляем статистику из пользователей...")
            val computedStats = repository.getStatsComputed()
            android.util.Log.d("DashboardViewModel", "✅ Вычисленная статистика:")
            android.util.Log.d("DashboardViewModel", "  totalTraffic=${computedStats.getTotalTraffic()}")
            android.util.Log.d("DashboardViewModel", "  ram=${computedStats.getRamUsage()}/${computedStats.getRamTotal()}")
            android.util.Log.d("DashboardViewModel", "  onlineUsers=${computedStats.getOnlineUsersCount()} (из SystemInfo или вычислено)")
            
            // Обновляем UI с вычисленными данными
            // Используем onlineUsers из computedStats (который берет из SystemInfo, если доступно)
            _uiState.update { 
                it.copy(
                    totalTraffic = computedStats.getTotalTraffic(),
                    totalUpload = computedStats.getTotalUpload(),
                    totalDownload = computedStats.getTotalDownload(),
                    onlineUsers = computedStats.getOnlineUsersCount(), // Приоритетное значение из SystemInfo
                    cpuUsage = computedStats.getCpuUsage(),
                    cpuCores = computedStats.getCpuCores(),
                    ramUsage = computedStats.getRamUsage(),
                    ramTotal = computedStats.getRamTotal(),
                    isLoading = false
                )
            }
            
            // Также пробуем получить через /api/stats для доп. данных
            try {
                repository.getStats().first().let { stats ->
                // Логируем все полученные данные для отладки
                android.util.Log.d("DashboardViewModel", "========== Stats Response ==========")
                android.util.Log.d("DashboardViewModel", "totalTraffic: ${stats.totalTraffic}")
                android.util.Log.d("DashboardViewModel", "totalUp: ${stats.totalUp}")
                android.util.Log.d("DashboardViewModel", "totalDown: ${stats.totalDown}")
                android.util.Log.d("DashboardViewModel", "onlineUsers: ${stats.onlineUsers}")
                android.util.Log.d("DashboardViewModel", "cpuUsage: ${stats.cpuUsage}")
                android.util.Log.d("DashboardViewModel", "cpuPercent: ${stats.cpuPercent}")
                android.util.Log.d("DashboardViewModel", "cpuCores: ${stats.cpuCores}")
                android.util.Log.d("DashboardViewModel", "ramUsage: ${stats.ramUsage}")
                android.util.Log.d("DashboardViewModel", "ramTotal: ${stats.ramTotal}")
                android.util.Log.d("DashboardViewModel", "memoryUsage: ${stats.memoryUsage}")
                android.util.Log.d("DashboardViewModel", "memoryTotal: ${stats.memoryTotal}")
                android.util.Log.d("DashboardViewModel", "system: ${stats.system}")
                android.util.Log.d("DashboardViewModel", "cpu: ${stats.cpu}")
                android.util.Log.d("DashboardViewModel", "memory: ${stats.memory}")
                android.util.Log.d("DashboardViewModel", "mem: ${stats.mem}")
                android.util.Log.d("DashboardViewModel", "traffic: ${stats.traffic}")
                android.util.Log.d("DashboardViewModel", "getTotalTraffic(): ${stats.getTotalTraffic()}")
                android.util.Log.d("DashboardViewModel", "getTotalUpload(): ${stats.getTotalUpload()}")
                android.util.Log.d("DashboardViewModel", "getTotalDownload(): ${stats.getTotalDownload()}")
                android.util.Log.d("DashboardViewModel", "getCpuUsage(): ${stats.getCpuUsage()}")
                android.util.Log.d("DashboardViewModel", "getCpuCores(): ${stats.getCpuCores()}")
                android.util.Log.d("DashboardViewModel", "getRamUsage(): ${stats.getRamUsage()}")
                android.util.Log.d("DashboardViewModel", "getRamTotal(): ${stats.getRamTotal()}")
                android.util.Log.d("DashboardViewModel", "getOnlineUsersCount(): ${stats.getOnlineUsersCount()}")
                
                // Обновляем UI только если API вернул НЕ пустые данные (перезаписываем вычисленные)
                val apiTraffic = stats.getTotalTraffic()
                val apiRamUsage = stats.getRamUsage()
                val apiRamTotal = stats.getRamTotal()
                
                if (apiTraffic > 0 || (apiRamUsage > 0 && apiRamTotal > 0)) {
                    android.util.Log.d("DashboardViewModel", "✅ Обновляем UI данными из /api/stats (перезаписываем вычисленные)")
                    _uiState.update { 
                        it.copy(
                            totalTraffic = if (apiTraffic > 0) apiTraffic else it.totalTraffic,
                            totalUpload = stats.getTotalUpload(),
                            totalDownload = stats.getTotalDownload(),
                            onlineUsers = stats.getOnlineUsersCount(),
                            cpuUsage = stats.getCpuUsage(),
                            cpuCores = stats.getCpuCores(),
                            ramUsage = if (apiRamUsage > 0) apiRamUsage else it.ramUsage,
                            ramTotal = if (apiRamTotal > 0) apiRamTotal else it.ramTotal,
                            isLoading = false
                        )
                    }
                } else {
                    android.util.Log.d("DashboardViewModel", "⚠️ /api/stats вернул пустые данные, используем вычисленные")
                }
                }
            } catch (e: Exception) {
                android.util.Log.w("DashboardViewModel", "Ошибка получения /api/stats: ${e.message}, уже использованы вычисленные данные")
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardViewModel", "Критическая ошибка загрузки статистики", e)
            _uiState.update { 
                it.copy(isLoading = false)
            }
        }
    }
    
    private suspend fun loadSystemInfo() {
        try {
            repository.getSystemInfo().first().let { systemInfo ->
                android.util.Log.d("DashboardViewModel", "loadSystemInfo: onlineUsers=${systemInfo.onlineUsers}, ramUsage=${systemInfo.getRamUsage()}, ramTotal=${systemInfo.getRamTotal()}")
                
                _uiState.update { currentState ->
                    // Обновляем CPU/RAM из systemInfo, если они не были получены из stats
                    // onlineUsers всегда обновляем из SystemInfo, так как это более актуальное значение от API
                    currentState.copy(
                        cpuUsage = if (currentState.cpuUsage == 0.0) systemInfo.getCpuUsage() else currentState.cpuUsage,
                        cpuCores = if (currentState.cpuCores == 0) systemInfo.getCpuCores() else currentState.cpuCores,
                        ramUsage = if (currentState.ramUsage == 0L) systemInfo.getRamUsage() else currentState.ramUsage,
                        ramTotal = if (currentState.ramTotal == 0L) systemInfo.getRamTotal() else currentState.ramTotal,
                        // Приоритетное обновление onlineUsers из SystemInfo (API)
                        onlineUsers = systemInfo.onlineUsers ?: currentState.onlineUsers
                    )
                }
                
                android.util.Log.d("DashboardViewModel", "UI State обновлен из SystemInfo: onlineUsers=${_uiState.value.onlineUsers}")
            }
        } catch (e: Exception) {
            android.util.Log.w("DashboardViewModel", "Ошибка загрузки системной информации: ${e.message}")
            // Не критично, продолжаем работу
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
}

