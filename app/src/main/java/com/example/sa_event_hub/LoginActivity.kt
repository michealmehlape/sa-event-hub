package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sa_event_hub.databinding.ActivityLoginBinding

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
        }

        // Show the error message (or hide it if there is none)
        viewModel.errorMessage.observe(this) { message ->
            if (message == null) {
                binding.tvError.visibility = View.GONE
            } else {
                binding.tvError.text = message
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
    }

    // Opens the main screen and clears the old screens so "Back" does not return here
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}