package com.kizvpn.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.data.repository.VpnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class StatisticsUiState(
    // Производительность сервера
    val cpuUsage: Double = 0.0,
    val cpuCores: Int = 0,
    val ramUsage: Long = 0,
    val ramTotal: Long = 0,
    val totalTraffic: Long = 0,
    val totalUpload: Long = 0,
    val totalDownload: Long = 0,
    val onlineUsers: Int = 0,
    // Общая статистика
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val totalNodes: Int = 0,
    val activeNodes: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class StatisticsViewModel(
    private val repository: VpnRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadAllStats()
    }
    
    fun loadAllStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Загружаем все данные параллельно
                launch { loadStats() }
                launch { loadUsers() }
                launch { loadNodes() }
                launch { loadSystemInfo() }
            } catch (e: Exception) {
                android.util.Log.e("StatisticsViewModel", "Ошибка загрузки статистики", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки статистики: ${e.message}"
                    )
                }
            }
        }
    }
    
    private suspend fun loadStats() {
        try {
            // Используем getStatsComputed, который работает надежно
            val computedStats = repository.getStatsComputed()
            _uiState.update { 
                it.copy(
                    cpuUsage = computedStats.getCpuUsage(),
                    cpuCores = computedStats.getCpuCores(),
                    ramUsage = computedStats.getRamUsage(),
                    ramTotal = computedStats.getRamTotal(),
                    totalTraffic = computedStats.getTotalTraffic(),
                    totalUpload = computedStats.getTotalUpload(),
                    totalDownload = computedStats.getTotalDownload(),
                    onlineUsers = computedStats.getOnlineUsersCount(),
                    totalUsers = computedStats.totalUsers ?: 0,
                    activeUsers = computedStats.activeUsers ?: 0,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("StatisticsViewModel", "Ошибка загрузки статистики: ${e.message}")
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "Ошибка загрузки статистики: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun loadUsers() {
        try {
            repository.getUsers().first().let { users ->
                _uiState.update { 
                    it.copy(
                        totalUsers = users.size,
                        activeUsers = users.count { it.status == "active" },
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            // Игнорируем AbortFlowException
            if (e::class.simpleName != "AbortFlowException") {
                android.util.Log.w("StatisticsViewModel", "Ошибка загрузки пользователей: ${e.message}")
            }
        }
    }
    
    private suspend fun loadNodes() {
        try {
            repository.getNodes().first().let { nodes ->
                _uiState.update { 
                    it.copy(
                        totalNodes = nodes.size,
                        activeNodes = nodes.count { it.status == "active" || it.status == null },
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("StatisticsViewModel", "Ошибка загрузки узлов: ${e.message}")
        }
    }
    
    private suspend fun loadSystemInfo() {
        try {
            repository.getSystemInfo().first().let { systemInfo ->
                _uiState.update { currentState ->
                    // Обновляем только если данные еще не были получены из stats
                    currentState.copy(
                        cpuUsage = if (currentState.cpuUsage == 0.0) systemInfo.getCpuUsage() else currentState.cpuUsage,
                        cpuCores = if (currentState.cpuCores == 0) systemInfo.getCpuCores() else currentState.cpuCores,
                        ramUsage = if (currentState.ramUsage == 0L) systemInfo.getRamUsage() else currentState.ramUsage,
                        ramTotal = if (currentState.ramTotal == 0L) systemInfo.getRamTotal() else currentState.ramTotal,
                        onlineUsers = systemInfo.onlineUsers ?: currentState.onlineUsers,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("StatisticsViewModel", "Ошибка загрузки системной информации: ${e.message}")
        }
    }
    
    fun refresh() {
        loadAllStats()
    }
}
