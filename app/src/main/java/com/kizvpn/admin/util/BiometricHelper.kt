package com.kizvpn.admin.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    
    /**
     * Проверяет доступность биометрической аутентификации на устройстве
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Получает тип биометрии (Fingerprint, Face, etc.)
     */
    fun getBiometricType(context: Context): String {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Доступно"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Не поддерживается"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Временно недоступно"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Не настроено"
            else -> "Неизвестно"
        }
    }
    
    /**
     * Показывает диалог биометрической аутентификации
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Биометрическая аутентификация",
        subtitle: String = "Используйте отпечаток пальца или Face ID для входа",
        negativeButtonText: String = "Отмена",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            // Пользователь отменил, ничего не делаем
                        }
                        else -> {
                            onError(errString.toString())
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )
        
        // Когда используется DEVICE_CREDENTIAL, нельзя устанавливать setNegativeButtonText
        // Система автоматически предоставит кнопку отмены
        val allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(allowedAuthenticators)
        
        // Не устанавливаем setNegativeButtonText при использовании DEVICE_CREDENTIAL
        // Система автоматически покажет кнопку отмены
        
        val promptInfo = promptInfoBuilder.build()
        
        biometricPrompt.authenticate(promptInfo)
    }
}

