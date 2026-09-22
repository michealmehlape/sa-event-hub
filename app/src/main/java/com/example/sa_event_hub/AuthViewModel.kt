package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

// This class does the login and register work using Firebase.
// Firebase keeps the password safe (encrypted), so we never store it ourselves.
class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()


    val isLoading = MutableLiveData(false)            // true while we wait for Firebase
    val errorMessage = MutableLiveData<String?>(null) // text to show if something goes wrong
    val isLoggedIn = MutableLiveData(false)           // becomes true when login/register works

    // LOGIN
    fun login(email: String, password: String) {
        // 1. Check what the user typed
        val problem = Validators.checkLogin(email, password)
        if (problem != null) {
            errorMessage.value = problem
            return
        }

        // 2. Ask Firebase to log the user in
        isLoading.value = true
        errorMessage.value = null
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login worked")
                    isLoggedIn.value = true
                } else {
                    Log.w("AuthViewModel", "Login failed", task.exception)
                    errorMessage.value = "Incorrect email or password."
                }
            }
    }

    // REGISTER
    fun register(name: String, email: String, password: String, confirm: String) {
        // Check what the user typed
        val problem = Validators.checkRegister(name, email, password, confirm)
        if (problem != null) {
            errorMessage.value = problem
            return
        }

        // Ask Firebase to create the account
        isLoading.value = true
        errorMessage.value = null
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Account created")
                    saveName(name)
                } else {
                    isLoading.value = false
                    Log.w("AuthViewModel", "Register failed", task.exception)
                    errorMessage.value = "Could not create the account. The email may already be used."
                }
            }
    }

    // Saves the user's name on their Firebase account
    private fun saveName(name: String) {
        val user = auth.currentUser
        val changes = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user?.updateProfile(changes)?.addOnCompleteListener {
            isLoading.value = false
            isLoggedIn.value = true // Firebase logs the new user in automatically
        }
    }
}