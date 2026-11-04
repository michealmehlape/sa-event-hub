package com.example.sa_event_hub

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Keeps our API up to date with this phone's push address (token) and the notifications choice.
object PushTokens {

    private const val TAG = "PushTokens"

    // Gets the phone's current token and sends it to the API. Call this after login.
    fun register() {
        if (FirebaseAuth.getInstance().currentUser == null) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                upload(task.result)
            } else {
                Log.w(TAG, "Could not get the push token", task.exception)
            }
        }
    }

    // Saves the token on the user's profile (PUT /api/profile)
    fun upload(token: String) {
        send(ProfileUpdate(fcmToken = token), "token")
    }

    // Saves the notifications switch on the user's profile, so the server knows
    fun saveNotificationsEnabled(enabled: Boolean) {
        send(ProfileUpdate(notificationsEnabled = enabled), "notifications switch")
    }

    private fun send(update: ProfileUpdate, what: String) {
        if (FirebaseAuth.getInstance().currentUser == null) return
        ApiClient.service.updateProfile(update).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                Log.d(TAG, "Saved $what, HTTP ${response.code()}")
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.w(TAG, "Could not save $what", t)
            }
        })
    }
}
