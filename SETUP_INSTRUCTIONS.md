# 🔧 Инструкции по настройке для публичной версии

## ⚠️ Важно

Перед публикацией репозитория необходимо заменить все конфиденциальные данные на шаблоны или переменные конфигурации.

## 📋 Список файлов для изменения

### 1. `app/src/main/java/com/kizvpn/admin/ui/screens/LoginScreen.kt`

**Найти и заменить:**
```kotlin
// Строка ~86
var apiUrl by remember { mutableStateOf("https://host.kizvpn.ru/api") }
// Заменить на:
var apiUrl by remember { mutableStateOf("") } // или использовать BuildConfig.DEFAULT_API_URL

// Строка ~248
placeholder = { Text("https://host.kizvpn.ru/api") }
// Заменить на:
placeholder = { Text("https://your-api-server.com/api") }

// Строка ~370
text = "1. Открой в браузере: https://host.kizvpn.ru/panel/",
// Заменить на:
text = "1. Открой в браузере: https://your-panel-url.com/",
```

### 2. `app/src/main/java/com/kizvpn/admin/data/api/ApiClient.kt`

**Найти и заменить:**
```kotlin
// Строки ~88-96
base.contains("host.kizvpn.ru") || base.contains("kizvpn.ru") -> {
    val url = "http://10.10.10.120:8080"
    ...
}
base.contains("10.10.10.110") -> {
    "http://10.10.10.120:8080"
}
// Заменить на:
base.contains("your-domain.com") -> {
    val url = BuildConfig.BOT_API_URL // или использовать переменную
    ...
}
// Удалить проверку по IP адресу или сделать её конфигурируемой
```

**Найти и заменить:**
```kotlin
// Строка ~497-518
"10.10.10.120:8080"
// Заменить на:
BuildConfig.BOT_API_URL // или переменную конфигурации
```

### 3. `app/src/main/java/com/kizvpn/admin/data/model/ApiModels.kt`

**Найти и заменить:**
```kotlin
// Строка ~143
fun getSubscriptionUrl(baseUrl: String = "https://host.kizvpn.ru"): String? {
// Заменить на:
fun getSubscriptionUrl(baseUrl: String = ""): String? {
```

### 4. `app/src/main/java/com/kizvpn/admin/di/ViewModelFactory.kt`

**Найти и заменить:**
```kotlin
// Строка ~40
?: "https://host.kizvpn.ru"
// Заменить на:
?: BuildConfig.DEFAULT_SUBSCRIPTION_BASE_URL // или ""
```

### 5. `app/src/main/java/com/kizvpn/admin/ui/viewmodel/UsersViewModel.kt`

**Найти и заменить:**
```kotlin
// Строки ~179, 195, 300
?: "https://host.kizvpn.ru"
// Заменить на:
?: BuildConfig.DEFAULT_SUBSCRIPTION_BASE_URL // или ""
```

### 6. `app/src/main/java/com/kizvpn/admin/ui/screens/ServersScreen.kt`

**Найти и заменить:**
```kotlin
// Строки ~76, 79, 86, 89
"10.10.10.110"
"10.10.10.120"
// Заменить на:
BuildConfig.VPN_SERVER_IP
BuildConfig.BOT_SERVER_IP
```

### 7. `app/src/main/java/com/kizvpn/admin/ui/screens/UsersScreen.kt`

**Найти и заменить:**
```kotlin
// Строка ~476
val publicBaseUrl = "https://host.kizvpn.ru"
// Заменить на:
val publicBaseUrl = BuildConfig.DEFAULT_SUBSCRIPTION_BASE_URL // или получить из настроек
```

## 🔨 Настройка BuildConfig

Добавьте в `app/build.gradle.kts`:

```kotlin
android {
    ...
    buildTypes {
        debug {
            buildConfigField("String", "DEFAULT_API_URL", "\"\"")
            buildConfigField("String", "BOT_API_URL", "\"\"")
            buildConfigField("String", "DEFAULT_SUBSCRIPTION_BASE_URL", "\"\"")
            buildConfigField("String", "VPN_SERVER_IP", "\"\"")
            buildConfigField("String", "BOT_SERVER_IP", "\"\"")
        }
        release {
            buildConfigField("String", "DEFAULT_API_URL", "\"\"")
            buildConfigField("String", "BOT_API_URL", "\"\"")
            buildConfigField("String", "DEFAULT_SUBSCRIPTION_BASE_URL", "\"\"")
            buildConfigField("String", "VPN_SERVER_IP", "\"\"")
            buildConfigField("String", "BOT_SERVER_IP", "\"\"")
        }
    }
}
```

## 🔍 Поиск всех вхождений

Выполните поиск в проекте по следующим паттернам:

```bash
# В Android Studio:
# Ctrl+Shift+F (Windows/Linux) или Cmd+Shift+F (Mac)
# Поиск:
- "host.kizvpn.ru"
- "10.10.10.110"
- "10.10.10.120"
- "kizvpn.ru"
```

## ✅ Чек-лист перед публикацией

- [ ] Все хардкодные API URLs заменены на переменные
- [ ] Все IP адреса удалены или заменены на переменные
- [ ] Доменные имена заменены на шаблоны
- [ ] BuildConfig настроен
- [ ] Создан файл конфигурации-шаблона
- [ ] README обновлен с инструкциями
- [ ] Скриншоты готовы (если планируете добавить)
- [ ] Лицензия добавлена
- [ ] .gitignore настроен правильно

## 📝 Альтернативный подход

Вместо использования BuildConfig можно:

1. **Использовать файл конфигурации** - создать `config.properties` и добавить его в `.gitignore`
2. **Использовать environment variables** - через Gradle properties
3. **Вводить все данные вручную** - пользователь настраивает при первом запуске (текущий подход)

## 🚀 После настройки

1. Проверьте сборку проекта: `./gradlew clean build`
2. Убедитесь, что нет ошибок компиляции
3. Протестируйте приложение с новыми настройками
4. Зафиксируйте изменения: `git add .` и `git commit`
5. Запушьте в публичный репозиторий

