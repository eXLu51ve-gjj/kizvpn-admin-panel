package com.kizvpn.admin.data.model

import com.google.gson.annotations.SerializedName

// PasarGuard API Models

data class SystemInfo(
    val version: String? = null,
    @SerializedName("build_time") val buildTime: String? = null,
    // Системные метрики (могут приходить из /api/system)
    @SerializedName("cpu_usage") val cpuUsage: Double? = null,
    @SerializedName("cpu_percent") val cpuPercent: Double? = null,
    @SerializedName("cpu_cores") val cpuCores: Int? = null,
    @SerializedName("ram_usage") val ramUsage: Long? = null,
    @SerializedName("ram_total") val ramTotal: Long? = null,
    @SerializedName("ram_percent") val ramPercent: Double? = null,
    @SerializedName("memory_usage") val memoryUsage: Long? = null,
    @SerializedName("memory_total") val memoryTotal: Long? = null,
    @SerializedName("mem_usage") val memUsage: Long? = null,
    @SerializedName("mem_used") val memUsed: Long? = null, // API возвращает mem_used, а не mem_usage
    @SerializedName("mem_total") val memTotal: Long? = null,
    @SerializedName("online_users") val onlineUsers: Int? = null, // Онлайн пользователи из /api/system
    @SerializedName("incoming_bandwidth") val incomingBandwidth: Long? = null, // Входящий трафик сервера
    @SerializedName("outgoing_bandwidth") val outgoingBandwidth: Long? = null, // Исходящий трафик сервера
    // Дополнительные возможные поля из API
    @SerializedName("mem") val mem: Map<String, Any>? = null, // Может быть объект с usage/total
    @SerializedName("memory") val memory: Map<String, Any>? = null, // Может быть объект
    @SerializedName("system") val system: Map<String, Any>? = null // Может быть объект с метриками
) {
    fun getCpuUsage(): Double {
        return cpuUsage ?: cpuPercent ?: 0.0
    }
    
    fun getCpuCores(): Int {
        return cpuCores ?: 0
    }
    
    fun getRamUsage(): Long {
        // Сначала пробуем прямые значения (mem_used имеет приоритет, так как это то, что возвращает API)
        val directUsage = ramUsage ?: memoryUsage ?: memUsed ?: memUsage
        if (directUsage != null && directUsage > 0) {
            return directUsage
        }
        
        // Пробуем из вложенных объектов mem/memory/system
        mem?.let { memMap ->
            val memUsageVal = memMap["usage"] ?: memMap["used"] ?: memMap["mem_usage"]
            when (memUsageVal) {
                is Number -> return memUsageVal.toLong()
                is String -> return memUsageVal.toLongOrNull() ?: 0L
                else -> {}
            }
        }
        
        memory?.let { memMap ->
            val memUsageVal = memMap["usage"] ?: memMap["used"] ?: memMap["memory_usage"]
            when (memUsageVal) {
                is Number -> return memUsageVal.toLong()
                is String -> return memUsageVal.toLongOrNull() ?: 0L
                else -> {}
            }
        }
        
        system?.let { sysMap ->
            val sysUsage = sysMap["mem_usage"] ?: sysMap["memory_usage"] ?: sysMap["ram_usage"]
            when (sysUsage) {
                is Number -> return sysUsage.toLong()
                is String -> return sysUsage.toLongOrNull() ?: 0L
                else -> {}
            }
        }
        
        // Если нет прямых значений, но есть ramPercent и ramTotal, вычисляем
        val total = getRamTotal()
        if (ramPercent != null && total > 0) {
            val computed = (ramPercent!! * total / 100.0).toLong()
            android.util.Log.d("SystemInfo", "Вычисляем RAM usage из ramPercent: ${ramPercent}% * $total = $computed байт")
            return computed
        }
        
        return 0
    }
    
    fun getRamTotal(): Long {
        return ramTotal ?: memoryTotal ?: memTotal ?: 0
    }
    
    fun getRamUsagePercent(): Double {
        val used = getRamUsage()
        val total = getRamTotal()
        return if (total > 0) (used.toDouble() / total.toDouble()) * 100.0 else 0.0
    }
}

data class User(
    val id: Int,
    val username: String? = null,
    val email: String? = null,
    val protocol: String? = null,
    val status: String? = null,
    @SerializedName("expiry") val expiryDate: String? = null,
    @SerializedName("expire") val expire: String? = null, // Альтернативное поле для даты
    @SerializedName("traffic_limit") val trafficLimit: Long? = null,
    @SerializedName("data_limit") val dataLimit: Long? = null, // Альтернативное поле
    @SerializedName("traffic_used") val trafficUsed: Long? = null,
    @SerializedName("used_traffic") val usedTraffic: Long? = null, // Альтернативное поле
    @SerializedName("lifetime_used_traffic") val lifetimeUsedTraffic: Long? = null, // Общий трафик за всё время
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("online_at") val onlineAt: String? = null, // Время последнего подключения
    @SerializedName("subscription_url") val subscriptionUrl: String? = null,
    @SerializedName("subscription") val subscription: String? = null // Альтернативное поле
) {
    // Вычисляемые свойства для удобства
    fun getDisplayName(): String {
        return username ?: email?.split("@")?.get(0) ?: "Пользователь #$id"
    }
    
    fun getTrafficLimit(): Long {
        return trafficLimit ?: dataLimit ?: 0
    }
    
    fun getTrafficUsed(): Long {
        // Используем lifetime_used_traffic если есть (более точный показатель), иначе used_traffic
        return lifetimeUsedTraffic ?: trafficUsed ?: usedTraffic ?: 0
    }
    
    fun isOnline(): Boolean {
        if (onlineAt == null) return false
        return try {
            val onlineTime = java.time.OffsetDateTime.parse(onlineAt)
            val fiveMinutesAgo = java.time.OffsetDateTime.now().minusMinutes(5)
            onlineTime.isAfter(fiveMinutesAgo)
        } catch (e: Exception) {
            android.util.Log.e("User", "Ошибка парсинга online_at: $onlineAt", e)
            false
        }
    }
    
    fun getExpiryDateString(): String? {
        return expiryDate ?: expire
    }
    
    fun getSubscriptionUrl(baseUrl: String = "https://host.kizvpn.ru"): String? {
        // Сначала пробуем subscription_url или subscription из ответа API
        val url = subscriptionUrl ?: subscription
        
        if (url != null && url.isNotBlank()) {
            // Если URL полный (начинается с http), возвращаем как есть
            if (url.startsWith("http")) {
                return url
            }
            // Если относительный, добавляем базовый URL
            val base = baseUrl.trimEnd('/')
            return if (url.startsWith("/")) {
                "$base$url"
            } else {
                "$base/$url"
            }
        }
        
        // Если нет в ответе, строим из username (как делается в ботах)
        if (username != null && username.isNotBlank()) {
            val base = baseUrl.trimEnd('/')
            return "$base/sub/$username"
        }
        
        return null
    }
}

data class CreateUserRequest(
    val username: String,
    val email: String? = null,
    val protocol: String = "vless",
    @SerializedName("inbound_id") val inboundId: Int,
    val expiry: String,
    @SerializedName("traffic_limit") val trafficLimit: Long
)

data class UpdateUserRequest(
    val email: String? = null,
    val expiry: String? = null,
    @SerializedName("traffic_limit") val trafficLimit: Long? = null,
    val status: String? = null
)

data class Inbound(
    val id: Int,
    val name: String,
    val port: Int,
    val protocol: String,
    val listen: String?,
    val settings: String? = null
)

data class Node(
    val id: Int,
    val name: String,
    val address: String,
    val port: Int,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class Stats(
    @SerializedName("total_users") val totalUsers: Int? = null,
    @SerializedName("active_users") val activeUsers: Int? = null,
    @SerializedName("total_traffic") val totalTraffic: Long? = null,
    @SerializedName("total_up") val totalUp: Long? = null,
    @SerializedName("total_down") val totalDown: Long? = null,
    // Системные метрики (CPU, RAM)
    @SerializedName("cpu_usage") val cpuUsage: Double? = null,
    @SerializedName("cpu_percent") val cpuPercent: Double? = null,
    @SerializedName("cpu_cores") val cpuCores: Int? = null,
    @SerializedName("ram_usage") val ramUsage: Long? = null,
    @SerializedName("ram_total") val ramTotal: Long? = null,
    @SerializedName("ram_percent") val ramPercent: Double? = null,
    @SerializedName("memory_usage") val memoryUsage: Long? = null,
    @SerializedName("memory_total") val memoryTotal: Long? = null,
    @SerializedName("online_users") val onlineUsers: Int? = null,
    @SerializedName("system") val system: Map<String, Any>? = null, // Для дополнительных полей из system
    // Альтернативные названия полей, которые могут приходить из API
    @SerializedName("cpu") val cpu: Map<String, Any>? = null,
    @SerializedName("memory") val memory: Map<String, Any>? = null,
    @SerializedName("mem") val mem: Map<String, Any>? = null,
    @SerializedName("traffic") val traffic: Map<String, Any>? = null
) {
    fun getCpuUsage(): Double {
        // Пробуем получить CPU из system, если не задано напрямую
        if (cpuUsage == null && cpuPercent == null) {
            // Из system
            system?.let { sys ->
                val systemCpu = sys["cpu_usage"] ?: sys["cpu_percent"]
                when (systemCpu) {
                    is Number -> return systemCpu.toDouble()
                    is String -> return systemCpu.toDoubleOrNull() ?: 0.0
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            // Из cpu
            cpu?.let { cpuMap ->
                val cpuUsageVal = cpuMap["usage"] ?: cpuMap["percent"] ?: cpuMap["cpu_usage"]
                when (cpuUsageVal) {
                    is Number -> return cpuUsageVal.toDouble()
                    is String -> return cpuUsageVal.toDoubleOrNull() ?: 0.0
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return cpuUsage ?: cpuPercent ?: 0.0
    }
    
    fun getCpuCores(): Int {
        // Пробуем получить CPU cores из system или cpu, если не задано напрямую
        if (cpuCores == null) {
            system?.let { sys ->
                val systemCores = sys["cpu_cores"] ?: sys["cores"]
                when (systemCores) {
                    is Number -> return systemCores.toInt()
                    is String -> return systemCores.toIntOrNull() ?: 0
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            cpu?.let { cpuMap ->
                val cores = cpuMap["cores"] ?: cpuMap["cpu_cores"]
                when (cores) {
                    is Number -> return cores.toInt()
                    is String -> return cores.toIntOrNull() ?: 0
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return cpuCores ?: 0
    }
    
    fun getRamUsage(): Long {
        // Пробуем получить RAM из system, memory, mem, если не задано напрямую
        if (ramUsage == null && memoryUsage == null) {
            // Из system
            system?.let { sys ->
                val systemRamUsage = sys["ram_usage"] ?: sys["memory_usage"] ?: sys["mem_usage"]
                when (systemRamUsage) {
                    is Number -> return systemRamUsage.toLong()
                    is String -> return systemRamUsage.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            // Из memory
            memory?.let { memMap ->
                val memUsageVal = memMap["usage"] ?: memMap["used"] ?: memMap["memory_usage"]
                when (memUsageVal) {
                    is Number -> return memUsageVal.toLong()
                    is String -> return memUsageVal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            // Из mem
            mem?.let { memMap ->
                val memUsageVal = memMap["usage"] ?: memMap["used"] ?: memMap["mem_usage"]
                when (memUsageVal) {
                    is Number -> return memUsageVal.toLong()
                    is String -> return memUsageVal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return ramUsage ?: memoryUsage ?: 0
    }
    
    fun getRamTotal(): Long {
        // Пробуем получить RAM total из system, memory, mem, если не задано напрямую
        if (ramTotal == null && memoryTotal == null) {
            // Из system
            system?.let { sys ->
                val systemRamTotal = sys["ram_total"] ?: sys["memory_total"] ?: sys["mem_total"]
                when (systemRamTotal) {
                    is Number -> return systemRamTotal.toLong()
                    is String -> return systemRamTotal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            // Из memory
            memory?.let { memMap ->
                val memTotalVal = memMap["total"] ?: memMap["memory_total"]
                when (memTotalVal) {
                    is Number -> return memTotalVal.toLong()
                    is String -> return memTotalVal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            // Из mem
            mem?.let { memMap ->
                val memTotalVal = memMap["total"] ?: memMap["mem_total"]
                when (memTotalVal) {
                    is Number -> return memTotalVal.toLong()
                    is String -> return memTotalVal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return ramTotal ?: memoryTotal ?: 0
    }
    
    fun getRamUsagePercent(): Double {
        val used = getRamUsage()
        val total = getRamTotal()
        return if (total > 0) (used.toDouble() / total.toDouble()) * 100.0 else 0.0
    }
    
    fun getTotalTraffic(): Long {
        // Пробуем получить total_traffic из traffic или system, если не задано напрямую
        if (totalTraffic == null) {
            traffic?.let { trafficMap ->
                val trafficTotal = trafficMap["total"] ?: trafficMap["total_traffic"]
                when (trafficTotal) {
                    is Number -> return trafficTotal.toLong()
                    is String -> return trafficTotal.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            system?.let { sys ->
                val sysTraffic = sys["total_traffic"] ?: sys["traffic"]
                when (sysTraffic) {
                    is Number -> return sysTraffic.toLong()
                    is String -> return sysTraffic.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return totalTraffic ?: 0
    }
    
    fun getTotalUpload(): Long {
        // Пробуем получить total_up из traffic или system, если не задано напрямую
        if (totalUp == null) {
            traffic?.let { trafficMap ->
                val up = trafficMap["up"] ?: trafficMap["upload"] ?: trafficMap["total_up"]
                when (up) {
                    is Number -> return up.toLong()
                    is String -> return up.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            system?.let { sys ->
                val sysUp = sys["total_up"] ?: sys["upload"]
                when (sysUp) {
                    is Number -> return sysUp.toLong()
                    is String -> return sysUp.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return totalUp ?: 0
    }
    
    fun getTotalDownload(): Long {
        // Пробуем получить total_down из traffic или system, если не задано напрямую
        if (totalDown == null) {
            traffic?.let { trafficMap ->
                val down = trafficMap["down"] ?: trafficMap["download"] ?: trafficMap["total_down"]
                when (down) {
                    is Number -> return down.toLong()
                    is String -> return down.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
            system?.let { sys ->
                val sysDown = sys["total_down"] ?: sys["download"]
                when (sysDown) {
                    is Number -> return sysDown.toLong()
                    is String -> return sysDown.toLongOrNull() ?: 0L
                    else -> { /* Игнорируем другие типы */ }
                }
            }
        }
        return totalDown ?: 0
    }
    
    fun getOnlineUsersCount(): Int {
        // Пробуем получить online_users из system, если не задано напрямую
        if (onlineUsers == null && system != null) {
            val systemOnlineUsers = system["online_users"]
            when (systemOnlineUsers) {
                is Number -> return systemOnlineUsers.toInt()
                is String -> return systemOnlineUsers.toIntOrNull() ?: 0
                else -> { /* Игнорируем другие типы */ }
            }
        }
        return onlineUsers ?: activeUsers ?: 0
    }
}

data class UserStats(
    @SerializedName("traffic_used") val trafficUsed: Long? = null,
    @SerializedName("used_traffic") val usedTraffic: Long? = null,
    @SerializedName("traffic_limit") val trafficLimit: Long? = null,
    @SerializedName("data_limit") val dataLimit: Long? = null,
    @SerializedName("last_seen") val lastSeen: String? = null
) {
    fun getTrafficUsed(): Long {
        return trafficUsed ?: usedTraffic ?: 0
    }
    
    fun getTrafficLimit(): Long {
        return trafficLimit ?: dataLimit ?: 0
    }
}

// Payment/Billing Models (из PostgreSQL БД)

data class Payment(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("tariff_id") val tariffId: Int,
    val amount: Double,
    val currency: String = "RUB",
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_id") val paymentId: String?,
    val status: String,
    @SerializedName("confirmed_at") val confirmedAt: String?,
    @SerializedName("created_at") val createdAt: String
)

data class Tariff(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("traffic_limit_gb") val trafficLimitGb: Int,
    val price: Double,
    val protocol: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class Subscription(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("vpn_email") val vpnEmail: String?,
    @SerializedName("vpn_uuid") val vpnUuid: String?,
    @SerializedName("vpn_user_id") val vpnUserId: Int?,
    val protocol: String,
    val status: String,
    @SerializedName("traffic_limit") val trafficLimit: Long,
    @SerializedName("traffic_used") val trafficUsed: Long,
    @SerializedName("expiry_date") val expiryDate: String?
)

// QR Code для оплаты
data class QRPaymentData(
    val cardNumber: String,
    val amount: Double,
    val tariffName: String
)

// Response от Bot API для subscription URL
data class SubscriptionUrlResponse(
    @SerializedName("subscription_url") val subscriptionUrl: String? = null,
    @SerializedName("subscription_id") val subscriptionId: Int? = null,
    val error: String? = null
)

// Response от Bot API для перезагрузки сервера
data class ServerRebootResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("server_ip") val serverIp: String,
    val error: String? = null
)


