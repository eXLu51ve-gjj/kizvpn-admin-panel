<div align="center">

<img src="logo.png" width="200"/>

# KIZ VPN Admin Panel

**Современная мобильная панель управления VPN сервером**

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Languages / Языки:** 🇷🇺 [Русский](#русский) | 🇬🇧 [English](#english)

---

</div>

---

## Русский

### 📱 Описание

**KIZ VPN Admin Panel** — современное Android приложение для управления VPN серверами на базе PasarGuard Panel. Предоставляет полный функционал для администрирования VPN инфраструктуры прямо с вашего Android устройства.

### ✨ Возможности

#### 👥 Управление пользователями
- Просмотр списка всех пользователей VPN
- Создание, редактирование и удаление пользователей
- Детальная статистика трафика для каждого пользователя
- Просмотр оставшихся дней подписки
- Получение конфигурационных файлов и subscription-ссылок
- Поиск и фильтрация пользователей

#### 🖥 Управление серверами
- Мониторинг узлов (Nodes) VPN сети
- Просмотр настроенных входящих подключений (Inbounds)
- Статус и состояние серверов в реальном времени
- Управление серверами (перезагрузка)

#### 💰 Платежи и биллинг
- Интеграция с PostgreSQL для учета платежей
- Список всех платежей с детальной информацией
- Фильтрация платежей по статусу и периоду
- Генерация QR-кодов для оплаты тарифов
- Статистика доходов по периодам

#### 📊 Статистика и аналитика
- Мониторинг производительности сервера (CPU, RAM, трафик)
- Количество активных пользователей
- Общая статистика трафика сервера
- Визуализация данных в удобном формате

#### 🔐 Безопасность
- Биометрическая аутентификация (отпечаток пальца/Face ID)
- Безопасное хранение токенов доступа в DataStore
- Защищенное соединение через HTTPS

#### 🎨 Интерфейс
- Современный Material Design 3
- Темная тема
- Интуитивно понятная навигация
- Видео-интро при запуске
- Адаптивный дизайн для разных размеров экранов

### 📸 Скриншоты

<table>
<tr>
<td><img src="screenshots/dashboard.jpg" width="240"/><br/><b>Dashboard</b></td>
<td><img src="screenshots/users.jpg" width="240"/><br/><b>Users</b></td>
<td><img src="screenshots/payments.jpg" width="240"/><br/><b>Payments</b></td>
</tr>
<tr>
<td><img src="screenshots/statistics.jpg" width="240"/><br/><b>Statistics</b></td>
<td><img src="screenshots/login.jpg" width="240"/><br/><b>Login</b></td>
</tr>
</table>

### 🚀 Скачать

#### Последний релиз

**Версия:** `v1.0.0`  
**Размер:** `~68 MB`  
**Минимальная версия Android:** `7.0 (API 24)`

[📥 Скачать APK](releases/KIZ-VPN-Panel-PUBLIC.apk)

> **Примечание:** Публичный APK требует настройки API сервера перед использованием. См. раздел "Настройка" ниже.

### 🛠 Технологии

- **Язык**: Kotlin
- **UI Framework**: Jetpack Compose
- **Архитектура**: MVVM (Model-View-ViewModel)
- **Сетевая библиотека**: Retrofit 2 + OkHttp
- **Локальное хранилище**: DataStore Preferences
- **Биометрия**: androidx.biometric
- **Видео**: ExoPlayer
- **Минимальная версия**: Android 7.0 (API 24)

### 📋 Требования

- **Android Studio**: Hedgehog | 2023.1.1 или новее
- **JDK**: 17 или выше
- **Android SDK**: API Level 34
- **Интернет**: требуется для работы с API

### ⚙️ Настройка

Перед использованием приложения необходимо настроить:

1. **API URL**: Введите адрес вашего PasarGuard API (например, `https://your-server.com/api`)
2. **JWT Token**: Укажите токен аутентификации

#### Как получить JWT токен:

1. Откройте веб-панель PasarGuard в браузере
2. Войдите в систему с учетными данными администратора
3. Откройте DevTools (F12) → вкладка Network
4. Выполните любое действие в панели
5. Найдите запрос → Headers → найдите `Authorization: Bearer ...`
6. Скопируйте токен после `Bearer ` (это ваш JWT токен)

### 📦 Установка из исходников

1. Клонируйте репозиторий:
```bash
git clone https://github.com/eXLu51ve-gjj/kizvpn-admin-panel.git
cd kizvpn-admin-panel
```

2. Откройте проект в Android Studio

3. Настройте API конфигурацию (см. `SETUP_INSTRUCTIONS.md`)

4. Соберите проект:
```bash
./gradlew assembleDebug
```

APK файл будет находиться в `app/build/outputs/apk/debug/app-debug.apk`

### 📖 Использование

#### Первый запуск

1. Запустите приложение
2. Введите URL вашего API сервера
3. Вставьте JWT токен доступа
4. Нажмите "Войти"

#### Настройка биометрической аутентификации

1. Перейдите в "Настройки"
2. Включите переключатель "Биометрическая аутентификация"
3. При следующем входе используйте отпечаток пальца или Face ID

### 🏗 Архитектура проекта

```
app/
├── src/main/
│   ├── java/com/kizvpn/admin/
│   │   ├── data/              # Слой данных
│   │   │   ├── api/           # API клиенты (Retrofit)
│   │   │   ├── model/         # Модели данных
│   │   │   └── repository/    # Репозитории
│   │   ├── di/                # Dependency Injection
│   │   ├── ui/                # UI слой
│   │   │   ├── navigation/    # Навигация
│   │   │   ├── screens/       # Экраны (Composable)
│   │   │   └── viewmodel/     # ViewModels
│   │   └── util/              # Утилиты
│   ├── res/                   # Ресурсы
│   └── AndroidManifest.xml
└── build.gradle.kts
```

### 🤝 Вклад в проект

Мы приветствуем вклад в развитие проекта! Пожалуйста:

1. Форкните репозиторий
2. Создайте ветку для новой функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

### 📄 Лицензия

Этот проект распространяется под лицензией [MIT License](LICENSE).

---

## English

### 📱 Description

**KIZ VPN Admin Panel** is a modern Android application for managing VPN servers based on PasarGuard Panel. Provides full functionality for administering VPN infrastructure directly from your Android device.

### ✨ Features

#### 👥 User Management
- View list of all VPN users
- Create, edit, and delete users
- Detailed traffic statistics for each user
- View remaining subscription days
- Get configuration files and subscription links
- Search and filter users

#### 🖥 Server Management
- Monitor VPN network nodes
- View configured inbound connections
- Real-time server status and state
- Server management (reboot)

#### 💰 Payments & Billing
- PostgreSQL integration for payment tracking
- List of all payments with detailed information
- Filter payments by status and period
- Generate QR codes for tariff payments
- Revenue statistics by period

#### 📊 Statistics & Analytics
- Monitor server performance (CPU, RAM, traffic)
- Number of active users
- Total server traffic statistics
- Data visualization in a convenient format

#### 🔐 Security
- Biometric authentication (fingerprint/Face ID)
- Secure token storage in DataStore
- Secure connection via HTTPS

#### 🎨 Interface
- Modern Material Design 3
- Dark theme
- Intuitive navigation
- Video intro on startup
- Adaptive design for different screen sizes

### 📸 Screenshots

<table>
<tr>
<td><img src="screenshots/dashboard.jpg" width="240"/><br/><b>Dashboard</b></td>
<td><img src="screenshots/users.jpg" width="240"/><br/><b>Users</b></td>
<td><img src="screenshots/payments.jpg" width="240"/><br/><b>Payments</b></td>
</tr>
<tr>
<td><img src="screenshots/statistics.jpg" width="240"/><br/><b>Statistics</b></td>
<td><img src="screenshots/login.jpg" width="240"/><br/><b>Login</b></td>
</tr>
</table>

### 🚀 Download

#### Latest Release

**Version:** `v1.0.0`  
**Size:** `~68 MB`  
**Minimum Android Version:** `7.0 (API 24)`

[📥 Download APK](releases/KIZ-VPN-Panel-PUBLIC.apk)

> **Note:** Public APK requires API server configuration before use. See "Configuration" section below.

### 🛠 Technologies

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp
- **Local Storage**: DataStore Preferences
- **Biometric**: androidx.biometric
- **Video**: ExoPlayer
- **Minimum Version**: Android 7.0 (API 24)

### 📋 Requirements

- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: 17 or higher
- **Android SDK**: API Level 34
- **Internet**: required for API access

### ⚙️ Configuration

Before using the app, you need to configure:

1. **API URL**: Enter your PasarGuard API address (e.g., `https://your-server.com/api`)
2. **JWT Token**: Provide your authentication token

#### How to get JWT token:

1. Open PasarGuard web panel in browser
2. Login with administrator credentials
3. Open DevTools (F12) → Network tab
4. Perform any action in the panel
5. Find request → Headers → find `Authorization: Bearer ...`
6. Copy token after `Bearer ` (this is your JWT token)

### 📦 Installation from Source

1. Clone the repository:
```bash
git clone https://github.com/eXLu51ve-gjj/kizvpn-admin-panel.git
cd kizvpn-admin-panel
```

2. Open project in Android Studio

3. Configure API settings (see `SETUP_INSTRUCTIONS.md`)

4. Build the project:
```bash
./gradlew assembleDebug
```

APK file will be located at `app/build/outputs/apk/debug/app-debug.apk`

### 📖 Usage

#### First Launch

1. Launch the application
2. Enter your API server URL
3. Paste JWT access token
4. Click "Login"

#### Setting up Biometric Authentication

1. Go to "Settings"
2. Enable "Biometric Authentication" toggle
3. On next login, use fingerprint or Face ID

### 🏗 Project Architecture

```
app/
├── src/main/
│   ├── java/com/kizvpn/admin/
│   │   ├── data/              # Data layer
│   │   │   ├── api/           # API clients (Retrofit)
│   │   │   ├── model/         # Data models
│   │   │   └── repository/    # Repositories
│   │   ├── di/                # Dependency Injection
│   │   ├── ui/                # UI layer
│   │   │   ├── navigation/    # Navigation
│   │   │   ├── screens/       # Screens (Composable)
│   │   │   └── viewmodel/     # ViewModels
│   │   └── util/              # Utilities
│   ├── res/                   # Resources
│   └── AndroidManifest.xml
└── build.gradle.kts
```

### 🤝 Contributing

We welcome contributions to the project! Please:

1. Fork the repository
2. Create a branch for new feature (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**Made with ❤️ for VPN administrators**

[⭐ Star this repo](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel) | [📝 Report Issue](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel/issues) | [💡 Request Feature](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel/issues)

</div>
