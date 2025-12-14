package com.kizvpn.admin.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kizvpn.admin.data.api.ApiClient
import com.kizvpn.admin.data.repository.AuthRepository
import com.kizvpn.admin.data.repository.VpnRepository
import com.kizvpn.admin.ui.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val apiClient: ApiClient?
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        android.util.Log.d("ViewModelFactory", "Создание ViewModel: ${modelClass.simpleName}")
        
        if (apiClient == null) {
            android.util.Log.e("ViewModelFactory", "API Client не инициализирован!")
            throw IllegalStateException("API Client не инициализирован. Убедитесь, что вы вошли в систему.")
        }
        
        val repository = VpnRepository(apiClient)
        
        return try {
            val viewModel = when {
                modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                    android.util.Log.d("ViewModelFactory", "Создание DashboardViewModel")
                    DashboardViewModel(repository) as T
                }
                modelClass.isAssignableFrom(UsersViewModel::class.java) -> {
                    android.util.Log.d("ViewModelFactory", "Создание UsersViewModel")
                    // Получаем API URL из authRepository для формирования subscription URL
                    val apiUrl = kotlinx.coroutines.runBlocking { 
                        authRepository.apiUrl.first()
                    }
                    val publicBaseUrl = apiUrl?.removeSuffix("/api")?.removeSuffix("/") ?: ""
                    UsersViewModel(repository, publicBaseUrl) as T
                }
                modelClass.isAssignableFrom(ServersViewModel::class.java) -> {
                    android.util.Log.d("ViewModelFactory", "Создание ServersViewModel")
                    // Получаем API URL для отображения в узлах
                    val apiUrl = kotlinx.coroutines.runBlocking {
                        authRepository.apiUrl.first()
                    }
                    val baseUrl = apiUrl?.removeSuffix("/api")?.removeSuffix("/") ?: null
                    ServersViewModel(repository, baseUrl) as T
                }
                modelClass.isAssignableFrom(PaymentsViewModel::class.java) -> {
                    android.util.Log.d("ViewModelFactory", "Создание PaymentsViewModel")
                    PaymentsViewModel(repository) as T
                }
                modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> {
                    android.util.Log.d("ViewModelFactory", "Создание StatisticsViewModel")
                    StatisticsViewModel(repository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
            android.util.Log.d("ViewModelFactory", "ViewModel создан успешно: ${modelClass.simpleName}")
            viewModel
        } catch (e: Exception) {
            android.util.Log.e("ViewModelFactory", "Ошибка создания ViewModel: ${modelClass.simpleName}", e)
            throw RuntimeException("Ошибка создания ViewModel: ${e.message}", e)
        }
    }
}

