package com.kizvpn.admin.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.kizvpn.admin.data.model.QRPaymentData

object QRCodeGenerator {
    
    /**
     * Генерирует QR-код для оплаты через Сбербанк
     * Формат: sberbank://transfer?card=НОМЕР_КАРТЫ&amount=СУММА_В_КОПЕЙКАХ
     * Альтернативный: https://www.sberbank.com/sms/pbpn?requisiteNumber=НОМЕР&sum=СУММА_В_КОПЕЙКАХ
     */
    fun generateSberbankQR(data: QRPaymentData, size: Int = 512): Bitmap {
        val amountInKopecks = (data.amount * 100).toInt()
        
        // Используем URL схему для открытия приложения Сбербанка
        val qrData = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Пробуем deep link для приложения
            "sberbank://transfer?card=${data.cardNumber}&amount=$amountInKopecks"
        } else {
            // Fallback на веб-версию
            "https://www.sberbank.com/sms/pbpn?requisiteNumber=${data.cardNumber}&sum=$amountInKopecks"
        }
        
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }
        
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, size, size, hints)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    }
    
    /**
     * Генерирует QR-код из произвольной строки
     */
    fun generateQR(text: String, size: Int = 512): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }
        
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    }
}


