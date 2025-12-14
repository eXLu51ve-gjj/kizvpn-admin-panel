# KIZ VPN Admin Panel

[English](#english) | [Русский](#русский)

---

## English

### 📱 Mobile Admin Panel for VPN Server Management

**KIZ VPN Admin Panel** is a modern Android application for managing VPN servers, users, and monitoring system resources. Built with Kotlin and Jetpack Compose.

### ✨ Features

- **User Management**: Create, edit, and delete VPN users. View detailed user information, traffic statistics, and subscription links.
- **Server Management**: Monitor VPN nodes, view inbounds, and manage server infrastructure.
- **Payments & Billing**: Track payments, generate QR codes for different tariffs, and view revenue statistics.
- **Statistics & Analytics**: Monitor server performance (CPU, RAM, traffic), view active users, and track resource usage.
- **Security**: Biometric authentication support for secure access.
- **Modern UI**: Beautiful Material Design 3 interface with intuitive navigation.

### 🚀 Getting Started

#### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9.0+

#### Installation

1. Clone the repository:
```bash
git clone https://github.com/eXLu51ve-gjj/kizvpn-admin-panel.git
cd kizvpn-admin-panel
```

2. Open the project in Android Studio

3. Build and run:
```bash
./gradlew assembleDebug
```

#### Configuration

Before using the app, you need to configure:

1. **API URL**: Enter your PasarGuard API endpoint (e.g., `https://your-server.com/api`)
2. **JWT Token**: Provide your authentication token

### 📸 Screenshots

_Screenshots will be available in the `screenshots/` directory_

### 🏗️ Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Manual DI with Factory pattern
- **Networking**: Retrofit + OkHttp
- **Local Storage**: DataStore
- **Navigation**: Navigation Component

### 📦 Dependencies

- Jetpack Compose
- Material Design 3
- Retrofit
- Gson
- DataStore
- Biometric
- ExoPlayer (for video intro)

### 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### 📄 License

This project is licensed under the MIT License.

---

## Русский

### 📱 Мобильная панель управления VPN сервером

**KIZ VPN Admin Panel** — современное Android-приложение для управления VPN серверами, пользователями и мониторинга системных ресурсов. Построено на Kotlin и Jetpack Compose.

### ✨ Возможности

- **Управление пользователями**: Создание, редактирование и удаление VPN пользователей. Просмотр детальной информации, статистики трафика и ссылок на подписки.
- **Управление серверами**: Мониторинг VPN узлов, просмотр входящих подключений и управление серверной инфраструктурой.
- **Платежи и биллинг**: Отслеживание платежей, генерация QR-кодов для различных тарифов и просмотр статистики доходов.
- **Статистика и аналитика**: Мониторинг производительности сервера (CPU, RAM, трафик), просмотр активных пользователей и отслеживание использования ресурсов.
- **Безопасность**: Поддержка биометрической аутентификации для безопасного доступа.
- **Современный UI**: Красивый интерфейс Material Design 3 с интуитивной навигацией.

### 🚀 Начало работы

#### Требования

- Android Studio Hedgehog | 2023.1.1 или новее
- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9.0+

#### Установка

1. Клонируйте репозиторий:
```bash
git clone https://github.com/eXLu51ve-gjj/kizvpn-admin-panel.git
cd kizvpn-admin-panel
```

2. Откройте проект в Android Studio

3. Соберите и запустите:
```bash
./gradlew assembleDebug
```

#### Настройка

Перед использованием приложения необходимо настроить:

1. **API URL**: Введите адрес вашего PasarGuard API (например, `https://your-server.com/api`)
2. **JWT Token**: Укажите токен аутентификации

### 📸 Скриншоты

_Скриншоты будут доступны в директории `screenshots/`_

### 🏗️ Архитектура

- **Язык**: Kotlin
- **UI Framework**: Jetpack Compose
- **Архитектура**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Ручная DI с Factory паттерном
- **Сеть**: Retrofit + OkHttp
- **Локальное хранилище**: DataStore
- **Навигация**: Navigation Component

### 📦 Зависимости

- Jetpack Compose
- Material Design 3
- Retrofit
- Gson
- DataStore
- Biometric
- ExoPlayer (для видео-интро)

### 🤝 Участие в разработке

Приветствуются любые вклады! Пожалуйста, не стесняйтесь отправлять Pull Request.

### 📄 Лицензия

Этот проект распространяется под лицензией MIT.
