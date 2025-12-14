package com.kizvpn.admin.data.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val baseUrl: String, private val token: String) {
    
    init {
        android.util.Log.d("ApiClient", "========== ИНИЦИАЛИЗАЦИЯ ApiClient ==========")
        android.util.Log.d("ApiClient", "baseUrl: $baseUrl")
        android.util.Log.d("ApiClient", "token: ${token.take(20)}...")
    }
    
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .method(original.method, original.body)
            .build()
        
        // Логируем полный URL запроса
        android.util.Log.d("ApiClient", "${request.method} ${request.url}")
        
        val response = chain.proceed(request)
        
        // Логируем код ответа и тело ошибки
        if (!response.isSuccessful) {
            val responseBody = response.peekBody(2048).string()
            android.util.Log.w("ApiClient", "HTTP ${response.code} for ${request.method} ${request.url}")
            android.util.Log.w("ApiClient", "Response body: $responseBody")
        }
        
        response
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(
            com.kizvpn.admin.data.model.UsersResponse::class.java,
            UsersResponseDeserializer()
        )
        .registerTypeAdapter(
            com.kizvpn.admin.data.model.InboundsResponse::class.java,
            InboundsResponseDeserializer()
        )
        .registerTypeAdapter(
            com.kizvpn.admin.data.model.NodesResponse::class.java,
            NodesResponseDeserializer()
        )
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.removeSuffix("/") + "/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val pasarguardApi: PasarGuardApi = retrofit.create(PasarGuardApi::class.java)
    
    // Bot API клиент для получения данных из PostgreSQL
    // Базовый URL для Bot API работает на сервере 10.10.10.120:8080
    // Если PasarGuard API через домен (host.kizvpn.ru), то Bot API на том же домене, но порт 8080
    // Если PasarGuard API через IP (10.10.10.110), то Bot API на 10.10.10.120:8080
    val botApi: BotApi? = try {
        val base = baseUrl.removeSuffix("/api").removeSuffix("/")
        android.util.Log.d("ApiClient", "Исходный baseUrl: $baseUrl, base после обработки: $base")
        
        val botApiUrl = when {
            // Если это домен (host.kizvpn.ru), используем IP адрес бот сервера (порт 8080 не проброшен через домен)
            base.contains("host.kizvpn.ru") || base.contains("kizvpn.ru") -> {
                // Bot API работает только на локальной сети, используем IP адрес бот сервера
                val url = "http://10.10.10.120:8080"
                android.util.Log.d("ApiClient", "Домен найден, используем IP адрес бот сервера: $url")
                url
            }
            // Если это IP 10.10.10.110 (VPN сервер), Bot API на 10.10.10.120 (Бот сервер)
            base.contains("10.10.10.110") -> {
                "http://10.10.10.120:8080"
            }
            // Если это другой IP или localhost, заменяем порт на 8080
            base.contains(":8000") -> {
                base.replace(":8000", ":8080")
            }
            // По умолчанию - добавляем :8080
            else -> {
                val host = base.replace("https://", "").replace("http://", "").split("/").first()
                "http://$host:8080"
            }
        }
        
        android.util.Log.d("ApiClient", "Создание BotApi клиента для URL: $botApiUrl")
        
        // Создаем клиент без авторизации (Bot API не требует JWT)
        val botOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS) // Уменьшен таймаут для быстрого обнаружения проблем
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val botRetrofit = Retrofit.Builder()
            .baseUrl(botApiUrl.removeSuffix("/") + "/")
            .client(botOkHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
        val botApiInstance = botRetrofit.create(BotApi::class.java)
        android.util.Log.d("ApiClient", "✅ BotApi клиент создан успешно")
        botApiInstance
    } catch (e: Exception) {
        android.util.Log.e("ApiClient", "❌ Не удалось создать BotApi клиент: ${e.message}", e)
        null
    }
}


