package com.kizvpn.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kizvpn.admin.data.api.ApiClient
import com.kizvpn.admin.data.repository.AuthRepository
import com.kizvpn.admin.di.ViewModelFactory
import com.kizvpn.admin.ui.navigation.AdminNavGraph
import com.kizvpn.admin.ui.screens.SplashScreen
import com.kizvpn.admin.ui.theme.AdminTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var authRepository: AuthRepository
    private lateinit var mainExecutor: Executor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Устанавливаем черный фон окна сразу при запуске
            window.setBackgroundDrawableResource(android.R.color.black)
            window.statusBarColor = android.graphics.Color.BLACK
            window.navigationBarColor = android.graphics.Color.BLACK
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Ошибка настройки окна", e)
        }
        
        try {
            authRepository = AuthRepository(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Ошибка создания AuthRepository", e)
            // Показываем ошибку пользователю и завершаем
            finish()
            return
        }
        
        try {
            // Executor для биометрии - создаем вне setContent
            mainExecutor = ContextCompat.getMainExecutor(this)
            
            setContent {
            AdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    var viewModelFactory by remember { mutableStateOf<ViewModelFactory?>(null) }
                    var apiClient by remember { mutableStateOf<ApiClient?>(null) }
                    
                    // Показываем сплэш-экран с видео
                    if (showSplash) {
                        SplashScreen(
                            onSplashComplete = {
                                showSplash = false
                            }
                        )
                    } else {
                        // Пытаемся загрузить сохраненные данные и создать ApiClient
                        LaunchedEffect(Unit) {
                            try {
                                android.util.Log.d("MainActivity", "Попытка загрузить сохраненные данные")
                                val apiUrl = authRepository.apiUrl.first()
                                val token = authRepository.token.first()
                                android.util.Log.d("MainActivity", "apiUrl: ${apiUrl?.take(20)}..., token: ${token?.take(20)}...")
                                if (apiUrl != null && token != null && apiUrl.isNotEmpty() && token.isNotEmpty()) {
                                    android.util.Log.d("MainActivity", "Создание ApiClient и ViewModelFactory")
                                    apiClient = ApiClient(apiUrl, token)
                                    viewModelFactory = ViewModelFactory(authRepository, apiClient)
                                    android.util.Log.d("MainActivity", "ViewModelFactory создан успешно")
                                } else {
                                    android.util.Log.d("MainActivity", "Нет сохраненных данных для авторизации")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Ошибка загрузки сохраненных данных", e)
                            }
                        }
                        
                        // Сохраняем ссылку на mainExecutor для использования в Composable
                        val executor = remember { 
                            android.util.Log.d("MainActivity", "remember mainExecutor")
                            mainExecutor 
                        }
                        val activity = remember { 
                            android.util.Log.d("MainActivity", "remember mainActivity")
                            this@MainActivity 
                        }
                        
                        android.util.Log.d("MainActivity", "Рендеринг AdminNavGraph")
                        AdminNavGraph(
                            authRepository = authRepository,
                            viewModelFactory = viewModelFactory,
                            onApiReady = { apiUrl, token ->
                                android.util.Log.d("MainActivity", "onApiReady вызван, создание ApiClient")
                                try {
                                    apiClient = ApiClient(apiUrl, token)
                                    viewModelFactory = ViewModelFactory(authRepository, apiClient)
                                    android.util.Log.d("MainActivity", "ApiClient и ViewModelFactory созданы после входа")
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Ошибка создания ApiClient/ViewModelFactory", e)
                                }
                            },
                            mainActivity = activity,
                            mainExecutor = executor
                        )
                    }
                }
            }
        }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Критическая ошибка при установке контента", e)
            // Показываем ошибку и завершаем
            finish()
        }
    }
}

