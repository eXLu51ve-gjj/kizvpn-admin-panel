package com.kizvpn.admin.util

/**
 * Форматирует байты в читаемый вид (B, KB, MB, GB, TB)
 */
fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "%.2f %s".format(size, units[unitIndex])
}

/**
 * Форматирует байты RAM в GB (всегда показывает в GB, даже если меньше 1 GB)
 * Пример: 828300000 байт -> "0.8 GB"
 */
fun formatRamInGB(bytes: Long): String {
    val gb = bytes / 1024.0 / 1024.0 / 1024.0
    return "%.1f GB".format(gb)
}

