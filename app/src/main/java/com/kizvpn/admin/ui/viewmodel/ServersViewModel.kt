package com.kizvpn.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.data.repository.VpnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class ServersUiState(
    val nodes: List<Node> = emptyList(),
    val inbounds: List<Inbound> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: ServerTab = ServerTab.NODES,
    val apiBaseUrl: String? = null // API URL для отображения вместо "Локальный сервер"
)

enum class ServerTab {
    NODES, INBOUNDS
}

class ServersViewModel(
    private val repository: VpnRepository,
    val apiBaseUrl: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServersUiState(apiBaseUrl = apiBaseUrl))
    val uiState: StateFlow<ServersUiState> = _uiState.asStateFlow()
    
    init {
        // Не загружаем автоматически
    }
    
    fun loadAll() {
        loadNodes()
        loadInbounds()
    }
    
    fun loadNodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val nodes = repository.getNodes().first()
                _uiState.update { 
                    it.copy(
                        nodes = nodes,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки узлов: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun loadInbounds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val inbounds = repository.getInbounds().first()
                _uiState.update { 
                    it.copy(
                        inbounds = inbounds,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки инбаундов: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun selectTab(tab: ServerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun refresh() {
        when (_uiState.value.selectedTab) {
            ServerTab.NODES -> loadNodes()
            ServerTab.INBOUNDS -> loadInbounds()
        }
    }
    
    suspend fun rebootServer(serverIp: String): Result<String> {
        return try {
            val result = repository.rebootServer(serverIp)
            result.fold(
                onSuccess = { response ->
                    Result.success(response.message)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

