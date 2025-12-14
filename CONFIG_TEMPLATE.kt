/**
 * ШАБЛОН КОНФИГУРАЦИИ
 * 
 * Этот файл содержит шаблоны конфигурации для публичной версии.
 * Замените значения по умолчанию на ваши реальные данные.
 */

object ConfigTemplate {
    // Замените на ваш API URL
    const val DEFAULT_API_URL = "YOUR_API_URL_HERE" // например: "https://your-domain.com/api"
    
    // Замените на IP адрес Bot API сервера
    const val DEFAULT_BOT_API_URL = "YOUR_BOT_API_URL_HERE" // например: "http://192.168.1.100:8080"
    
    // Замените на базовый URL для subscription ссылок
    const val DEFAULT_SUBSCRIPTION_BASE_URL = "YOUR_SUBSCRIPTION_BASE_URL_HERE" // например: "https://your-domain.com"
    
    // IP адреса серверов (для функционала перезагрузки)
    const val VPN_SERVER_IP = "YOUR_VPN_SERVER_IP_HERE" // например: "192.168.1.110"
    const val BOT_SERVER_IP = "YOUR_BOT_SERVER_IP_HERE" // например: "192.168.1.120"
}

/**
 * ИНСТРУКЦИЯ ПО ЗАМЕНЕ:
 * 
 * 1. Найдите все места в коде, где используются хардкодные значения:
 *    - "https://host.kizvpn.ru"
 *    - "10.10.10.110"
 *    - "10.10.10.120"
 *    - "http://10.10.10.120:8080"
 * 
 * 2. Замените их на значения из этого шаблона или на ваши собственные константы.
 * 
 * 3. Можно создать отдельный файл конфигурации или использовать BuildConfig:
 *    - buildConfigField "String", "API_URL", "\"YOUR_API_URL\""
 *    - buildConfigField "String", "BOT_API_URL", "\"YOUR_BOT_API_URL\""
 */

