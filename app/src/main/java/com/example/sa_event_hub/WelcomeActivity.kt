package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sa_event_hub.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

// The first screen the user sees.
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the user is already logged in, skip this screen and go to the main screen
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // "binding" lets us reach the views in activity_welcome.xml
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When the button is tapped, open the Login screen
        binding.btnGetStarted.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}