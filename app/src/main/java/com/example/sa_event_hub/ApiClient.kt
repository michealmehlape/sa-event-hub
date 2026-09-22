package com.example.sa_event_hub

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Adds the user's Firebase login token to every request.
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d("AuthInterceptor", "No user is logged in, sending the request with no token.")
        } else {
            try {
                // Wait at most 10 seconds for the token, instead of forever.
                val result = Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS)
                if (result.token == null) {
                    Log.w("AuthInterceptor", "Firebase returned an empty token.")
                } else {
                    request.addHeader("Authorization", "Bearer ${result.token}")
                    Log.d("AuthInterceptor", "Token added to the request.")
                }
            } catch (e: Exception) {
                // This is the important line. It tells us WHY the token could not be fetched.
                Log.e("AuthInterceptor", "Could not get the login token: ${e.javaClass.simpleName}: ${e.message}")
            }
        }
        return chain.proceed(request.build())
    }
}

object ApiClient {

    private const val BASE_URL = "https://saeventhub.phatuditradings.co.za/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("Authorization")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}