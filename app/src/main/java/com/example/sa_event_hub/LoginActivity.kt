package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sa_event_hub.databinding.ActivityLoginBinding
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope

import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Wthches the ViewModel. When a value changes, update the screen.

        // Show the spinner and turn off the button while loading
        viewModel.isLoading.observe(this) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
            binding.btnLogin.isEnabled = !loading
            binding.btnGoogle.isEnabled = !loading
        }

        // Show the error message (or hide it if there is none)
        viewModel.errorMessage.observe(this) { message ->
            if (message == null) {
                binding.tvError.visibility = View.GONE
            } else {
                binding.tvError.text = message.asString(this)
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // When the login works, go to the main screen
        viewModel.isLoggedIn.observe(this) { loggedIn ->
            if (loggedIn) {
                goToMain()
            }
        }

        // BUTTONS
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
    }

    // Opens the main screen and clears the old screens so "Back" does not return here
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // Opens the Google account picker and passes the result to the ViewModel
    private fun signInWithGoogle() {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // show all accounts on the phone
            .setServerClientId(getString(R.string.default_web_client_id)) // generated from google-services.json
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@LoginActivity)
                    .getCredential(this@LoginActivity, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val google = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.signInWithGoogle(google.idToken)
                } else {
                    Log.w("LoginActivity", "Unexpected credential type")
                    viewModel.showError(UiText(R.string.google_sign_in_failed))
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("LoginActivity", "User closed the Google picker")
            } catch (e: GetCredentialException) {
                Log.w("LoginActivity", "Google sign-in error", e)
                viewModel.showError(UiText(R.string.google_sign_in_failed))
            }
        }
    }
}