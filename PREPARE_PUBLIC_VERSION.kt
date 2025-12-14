/**
 * СКРИПТ ДЛЯ ПОДГОТОВКИ ПУБЛИЧНОЙ ВЕРСИИ
 * 
 * Этот файл содержит список всех мест, где нужно заменить чувствительные данные
 * на плейсхолдеры перед публикацией репозитория.
 * 
 * ⚠️ ВАЖНО: Выполните все замены перед созданием публичного репозитория!
 */

// ============================================================================
// ФАЙЛЫ ДЛЯ ИЗМЕНЕНИЯ
// ============================================================================

/**
 * 1. app/src/main/java/com/kizvpn/admin/ui/screens/LoginScreen.kt
 * 
 * Строка 86:
 * ❌ var apiUrl by remember { mutableStateOf("https://host.kizvpn.ru/api") }
 * ✅ var apiUrl by remember { mutableStateOf("") }
 * 
 * Строка 248:
 * ❌ placeholder = { Text("https://host.kizvpn.ru/api") }
 * ✅ placeholder = { Text("https://your-api-server.com/api") }
 * 
 * Строка 370:
 * ❌ text = "1. Открой в браузере: https://host.kizvpn.ru/panel/",
 * ✅ text = "1. Открой в браузере: https://your-panel-url.com/",
 */

/**
 * 2. app/src/main/java/com/kizvpn/admin/data/api/ApiClient.kt
 * 
 * Строки 87-96:
 * ❌ base.contains("host.kizvpn.ru") || base.contains("kizvpn.ru") -> {
 *     val url = "http://10.10.10.120:8080"
 *     ...
 * }
 * base.contains("10.10.10.110") -> {
 *     "http://10.10.10.120:8080"
 * }
 * 
 * ✅ Заменить на динамическое определение Bot API URL:
 * val botApiUrl = when {
 *     base.contains(":8000") -> {
 *         base.replace(":8000", ":8080")
 *     }
 *     else -> {
 *         val host = base.replace("https://", "").replace("http://", "").split("/").first()
 *         "http://$host:8080" // Или использовать BuildConfig.BOT_API_URL
 *     }
 * }
 * 
 * Комментарии (строки 79-81):
 * ❌ // Базовый URL для Bot API работает на сервере 10.10.10.120:8080
 * ✅ // Bot API URL определяется динамически на основе базового URL
 */

/**
 * 3. app/src/main/java/com/kizvpn/admin/data/repository/VpnRepository.kt
 * 
 * Строки 497, 506, 509, 514, 516, 518:
 * ❌ "10.10.10.120:8080"
 * ✅ "Bot API сервер" или использовать переменную конфигурации
 * 
 * Пример:
 * ❌ return Result.failure(Exception("Bot API недоступен. Убедитесь, что сервер 10.10.10.120:8080 запущен."))
 * ✅ return Result.failure(Exception("Bot API недоступен. Убедитесь, что Bot API сервер запущен и доступен."))
 */

/**
 * 4. app/src/main/java/com/kizvpn/admin/ui/screens/ServersScreen.kt
 * 
 * Строки 76, 79:
 * ❌ text = { Text("Перезагрузить VPN сервер (10.10.10.110)") }
 * ❌ showRebootDialog = "10.10.10.110"
 * ✅ text = { Text("Перезагрузить VPN сервер") }
 * ✅ showRebootDialog = BuildConfig.VPN_SERVER_IP // или переменная конфигурации
 * 
 * Строки 86, 89:
 * ❌ text = { Text("Перезагрузить Bot сервер (10.10.10.120)") }
 * ❌ showRebootDialog = "10.10.10.120"
 * ✅ text = { Text("Перезагрузить Bot сервер") }
 * ✅ showRebootDialog = BuildConfig.BOT_SERVER_IP // или переменная конфигурации
 */

/**
 * 5. app/src/main/java/com/kizvpn/admin/data/model/ApiModels.kt
 * 
 * Строка 143:
 * ❌ fun getSubscriptionUrl(baseUrl: String = "https://host.kizvpn.ru"): String? {
 * ✅ fun getSubscriptionUrl(baseUrl: String = ""): String? {
 */

/**
 * 6. app/src/main/java/com/kizvpn/admin/di/ViewModelFactory.kt
 * 
 * Строка 40:
 * ❌ val publicBaseUrl = apiUrl?.removeSuffix("/api")?.removeSuffix("/") ?: "https://host.kizvpn.ru"
 * ✅ val publicBaseUrl = apiUrl?.removeSuffix("/api")?.removeSuffix("/") ?: ""
 */

/**
 * 7. app/src/main/java/com/kizvpn/admin/ui/viewmodel/UsersViewModel.kt
 * 
 * Строки 179, 195, 300:
 * ❌ val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: "https://host.kizvpn.ru"
 * ✅ val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: ""
 */

/**
 * 8. app/src/main/java/com/kizvpn/admin/ui/screens/UsersScreen.kt
 * 
 * Строка 476:
 * ❌ val publicBaseUrl = "https://host.kizvpn.ru"
 * ✅ val publicBaseUrl = baseUrl?.removeSuffix("/api")?.removeSuffix("/") ?: ""
 */

/**
 * 9. app/src/main/res/values/strings.xml
 * 
 * Строка 16:
 * ❌ <string name="login_api_url_hint">https://host.kizvpn.ru/api</string>
 * ✅ <string name="login_api_url_hint">https://your-api-server.com/api</string>
 */

/**
 * 10. Комментарии в коде
 * 
 * Проверить все комментарии на наличие IP адресов и доменов:
 * - "10.10.10.110" → "VPN_SERVER_IP"
 * - "10.10.10.120" → "BOT_SERVER_IP"
 * - "host.kizvpn.ru" → "YOUR_DOMAIN"
 */

// ============================================================================
// РЕКОМЕНДАЦИИ
// ============================================================================

/**
 * Для публичной версии рекомендуется:
 * 
 * 1. Использовать BuildConfig для конфигурации:
 *    buildConfigField "String", "DEFAULT_API_URL", "\"\""
 *    buildConfigField "String", "BOT_API_URL", "\"\""
 * 
 * 2. Создать файл конфигурации (Config.kt):
 *    object Config {
 *        const val DEFAULT_API_URL = "" // Пользователь должен настроить
 *        const val BOT_API_URL = "" // Опционально
 *    }
 * 
 * 3. Удалить хардкодные значения из всех файлов
 * 
 * 4. Добавить инструкции по настройке в README
 */

