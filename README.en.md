<div align="center">

<img src="logo.png" width="300"/>

# KIZ VPN Admin Panel

**Modern mobile VPN server control panel**

[![Android](https://img.shields.io/badge/Android-7.0%2B-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-orange.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

📖 **Languages:** [🇷🇺 Русский](README.md) | [🇬🇧 English](README.en.md)

---

</div>

---

## 📱 Description

**KIZ VPN Admin Panel** is a modern Android application for managing VPN servers based on PasarGuard Panel. Provides full functionality for administering VPN infrastructure directly from your Android device.

## ✨ Features

### 👥 User Management
- View list of all VPN users
- Create, edit, and delete users
- Detailed traffic statistics for each user
- View remaining subscription days
- Get configuration files and subscription links
- Search and filter users

### 🖥 Server Management
- Monitor VPN network nodes
- View configured inbound connections
- Real-time server status and state
- Server management (reboot)

### 💰 Payments & Billing
- PostgreSQL integration for payment tracking
- List of all payments with detailed information
- Filter payments by status and period
- Generate QR codes for tariff payments
- Revenue statistics by period

### 📊 Statistics & Analytics
- Monitor server performance (CPU, RAM, traffic)
- Number of active users
- Total server traffic statistics
- Data visualization in a convenient format

### 🔐 Security
- Biometric authentication (fingerprint/Face ID)
- Secure token storage in DataStore
- Secure connection via HTTPS

### 🎨 Interface
- Modern Material Design 3
- Dark theme
- Intuitive navigation
- Video intro on startup
- Adaptive design for different screen sizes

## 📸 Screenshots

<table>
<tr>
<td><img src="screenshots/dashboard.jpg" width="250"/><br/><b>Dashboard</b></td>
<td><img src="screenshots/users.jpg" width="250"/><br/><b>Users</b></td>
<td><img src="screenshots/payments.jpg" width="250"/><br/><b>Payments</b></td>
</tr>
<tr>
<td><img src="screenshots/statistics.jpg" width="250"/><br/><b>Statistics</b></td>
<td><img src="screenshots/login.jpg" width="250"/><br/><b>Login</b></td>
</tr>
</table>

## 🚀 Download

### Latest Release

**Version:** `v1.0.0`  
**Size:** `~68 MB`  
**Minimum Android Version:** `7.0 (API 24)`

[📥 Download APK](releases/KIZ-VPN-Panel-PUBLIC.apk)

> **Note:** Public APK requires API server configuration before use. See "Configuration" section below.

## 🛠 Technologies

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp
- **Local Storage**: DataStore Preferences
- **Biometric**: androidx.biometric
- **Video**: ExoPlayer
- **Minimum Version**: Android 7.0 (API 24)

## 📋 Requirements

- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: 17 or higher
- **Android SDK**: API Level 34
- **Internet**: required for API access

## ⚙️ Configuration

Before using the app, you need to configure:

1. **API URL**: Enter your PasarGuard API address (e.g., `https://your-server.com/api`)
2. **JWT Token**: Provide your authentication token

### How to get JWT token:

1. Open PasarGuard web panel in browser
2. Login with administrator credentials
3. Open DevTools (F12) → Network tab
4. Perform any action in the panel
5. Find request → Headers → find `Authorization: Bearer ...`
6. Copy token after `Bearer ` (this is your JWT token)

## 📦 Installation from Source

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

## 📖 Usage

### First Launch

1. Launch the application
2. Enter your API server URL
3. Paste JWT access token
4. Click "Login"

### Setting up Biometric Authentication

1. Go to "Settings"
2. Enable "Biometric Authentication" toggle
3. On next login, use fingerprint or Face ID

## 🏗 Project Architecture

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

## 🤝 Contributing

We welcome contributions to the project! Please:

1. Fork the repository
2. Create a branch for new feature (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**Made with ❤️ for VPN administrators**

[⭐ Star this repo](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel) | [📝 Report Issue](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel/issues) | [💡 Request Feature](https://github.com/eXLu51ve-gjj/kizvpn-admin-panel/issues)

</div>

