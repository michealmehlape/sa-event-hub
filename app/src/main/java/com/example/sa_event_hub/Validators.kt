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

    // Checks the login form. Returns an error message, or null if it is OK.
    fun checkLogin(email: String, password: String): String? {
        if (email.isBlank()) return "Please enter your email."
        if (!isEmailValid(email)) return "That email does not look right."
        if (password.isEmpty()) return "Please enter your password."
        return null
    }

    // Checks the register form. Returns an error message, or null if it is OK.
    fun checkRegister(name: String, email: String, password: String, confirm: String): String? {
        if (name.isBlank()) return "Please enter your full name."
        if (email.isBlank()) return "Please enter your email."
        if (!isEmailValid(email)) return "That email does not look right."
        if (!isPasswordValid(password)) return "Password needs 8+ characters, a letter and a number."
        if (password != confirm) return "The passwords do not match."
        return null
    }

    // Checks the change-password form. Returns an error message, or null if it is OK.
    fun checkPasswordChange(current: String, newPassword: String, confirm: String): String? {
        if (current.isEmpty()) return "Please enter your current password."
        if (!isPasswordValid(newPassword)) return "New password needs 8+ characters, a letter and a number."
        if (newPassword != confirm) return "The new passwords do not match."
        if (newPassword == current) return "The new password must be different."
        return null
    }
}