package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sa_event_hub.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

// The main screen. It holds the bottom menu and shows one tab (Fragment) at a time.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if user is not logged in, go back to the Welcome screen
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When the user taps a menu item, show the matching screen
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment())
                R.id.nav_search -> showFragment(SearchFragment())
                R.id.nav_saved -> showFragment(SavedFragment())
                R.id.nav_community -> showFragment(CommunityFragment())
                R.id.nav_profile -> showFragment(ProfileFragment())
            }
            true
        }

        // Show the Home tab when the app first opens
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_home
        }
    }

    // Replaces the content in the container with the new screen
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}