package com.velaphi.authetication.util

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }
    
    fun isValidPassword(password: String): Boolean {
        // Password must be at least 6 characters long
        return password.length >= 6
    }
    
    fun isValidDisplayName(displayName: String): Boolean {
        // Display name must be at least 2 characters and not empty
        return displayName.trim().length >= 2
    }
    
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 8 -> PasswordStrength.MEDIUM
            password.any { it.isUpperCase() } && 
            password.any { it.isLowerCase() } && 
            password.any { it.isDigit() } -> PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
    }
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
}
