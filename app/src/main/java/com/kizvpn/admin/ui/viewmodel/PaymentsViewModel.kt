package com.kizvpn.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizvpn.admin.data.model.*
import com.kizvpn.admin.data.repository.VpnRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaymentsUiState(
    val payments: List<Payment> = emptyList(),
    val tariffs: List<Tariff> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedStatus: String? = null,
    val totalRevenue: Double = 0.0,
    val todayRevenue: Double = 0.0
)

class PaymentsViewModel(
    private val repository: VpnRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()
    
    init {
        // TODO: Загрузить платежи из BotApi когда будет готов endpoint
        // loadPayments()
        // loadTariffs()
    }
    
    fun loadPayments(status: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedStatus = status) }
            try {
                // TODO: Вызов API когда будет готов
                // val payments = repository.getPayments(status)
                // _uiState.update { 
                //     it.copy(
                //         payments = payments,
                //         isLoading = false,
                //         totalRevenue = calculateTotalRevenue(payments),
                //         todayRevenue = calculateTodayRevenue(payments)
                //     )
                // }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        payments = emptyList() // Временно пустой список
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки платежей: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun searchPayments(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun refresh() {
        loadPayments(_uiState.value.selectedStatus)
    }
    
    private fun calculateTotalRevenue(payments: List<Payment>): Double {
        return payments
            .filter { it.status == "paid" }
            .sumOf { it.amount }
    }
    
    private fun calculateTodayRevenue(payments: List<Payment>): Double {
        val today = java.time.LocalDate.now()
        return payments
            .filter { 
                it.status == "paid" && 
                it.confirmedAt?.startsWith(today.toString()) == true
            }
            .sumOf { it.amount }
    }
    
    val filteredPayments: StateFlow<List<Payment>> = combine(
        _uiState,
        _uiState.map { it.searchQuery },
        _uiState.map { it.selectedStatus }
    ) { state, query, status ->
        var filtered = state.payments
        
        // Фильтр по статусу
        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }
        
        // Поиск
        if (query.isNotBlank()) {
            filtered = filtered.filter { payment ->
                payment.paymentId?.contains(query, ignoreCase = true) == true ||
                payment.paymentMethod.contains(query, ignoreCase = true)
            }
        }
        
        filtered
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
}

