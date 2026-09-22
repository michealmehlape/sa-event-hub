package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.sa_event_hub.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "binding" lets us reach the views in fragment_profile.xml
        binding = FragmentProfileBinding.bind(view)

        // Open the Settings screen
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // Log out, then go back to the Welcome screen
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // onResume runs every time this screen comes back into view

    override fun onResume() {
        super.onResume()
        showUserInfo()
    }

    private fun showUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName
        if (name.isNullOrBlank()) {
            binding.tvName.text = "SA Event Hub user"
        } else {
            binding.tvName.text = name
        }
        binding.tvEmail.text = user?.email
    }
}