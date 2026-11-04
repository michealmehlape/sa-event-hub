package com.example.sa_event_hub

// This file checks what the user typed.
// Each function returns a message if something is wrong, or null if everything is fine.
object Validators {

    // Checks that the email looks like a real email (has @ and a dot after it)
    fun isEmailValid(email: String): Boolean {
        val text = email.trim()
        val atPosition = text.indexOf("@")
        val dotPosition = text.lastIndexOf(".")
        return atPosition > 0 && dotPosition > atPosition + 1 && dotPosition < text.length - 1
    }

    // Password must have 8 or more characters, at least 1 letter and at least 1 number
    fun isPasswordValid(password: String): Boolean {
        val longEnough = password.length >= 8
        val hasLetter = password.any { it.isLetter() }
        val hasNumber = password.any { it.isDigit() }
        return longEnough && hasLetter && hasNumber
    }

    // Checks the login form. Returns a message to show, or null if it is OK.
    fun checkLogin(email: String, password: String): UiText? {
        if (email.isBlank()) return UiText(R.string.err_enter_email)
        if (!isEmailValid(email)) return UiText(R.string.err_email_invalid)
        if (password.isEmpty()) return UiText(R.string.err_enter_password)
        return null
    }

    // Checks the register form. Returns a message to show, or null if it is OK.
    fun checkRegister(name: String, email: String, password: String, confirm: String): UiText? {
        if (name.isBlank()) return UiText(R.string.err_enter_name)
        if (email.isBlank()) return UiText(R.string.err_enter_email)
        if (!isEmailValid(email)) return UiText(R.string.err_email_invalid)
        if (!isPasswordValid(password)) return UiText(R.string.err_password_rules)
        if (password != confirm) return UiText(R.string.err_passwords_mismatch)
        return null
    }

    // Checks the change-password form. Returns a message to show, or null if it is OK.
    fun checkPasswordChange(current: String, newPassword: String, confirm: String): UiText? {
        if (current.isEmpty()) return UiText(R.string.err_enter_current_password)
        if (!isPasswordValid(newPassword)) return UiText(R.string.err_new_password_rules)
        if (newPassword != confirm) return UiText(R.string.err_new_passwords_mismatch)
        if (newPassword == current) return UiText(R.string.err_new_password_same)
        return null
    }
}