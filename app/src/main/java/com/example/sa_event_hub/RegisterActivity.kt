package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sa_event_hub.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // WATCH the ViewModel

        viewModel.isLoading.observe(this) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
            binding.btnRegister.isEnabled = !loading
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message == null) {
                binding.tvError.visibility = View.GONE
            } else {
                binding.tvError.text = message
                binding.tvError.visibility = View.VISIBLE
            }
        }

        // When the account is created, go to the main screen
        viewModel.isLoggedIn.observe(this) { loggedIn ->
            if (loggedIn) {
                goToMain()
            }
        }

        // BUTTONS
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            viewModel.register(name, email, password, confirm)
        }

        // Go back to the Login screen
        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}