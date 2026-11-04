package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sa_event_hub.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    // The Android photo picker. It needs no storage permission.
    // Reference: Android Developers, "Photo picker".
    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadPhoto(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "binding" lets us reach the views in fragment_profile.xml
        binding = FragmentProfileBinding.bind(view)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Open the Settings screen
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // Choose a new profile picture
        val choosePhoto = View.OnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.ivAvatar.setOnClickListener(choosePhoto)
        binding.btnChangePhoto.setOnClickListener(choosePhoto)

        // Log out, then go back to the Welcome screen
        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                FavouritesRepository.get(requireContext()).clearLocal()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        // WATCH the ViewModel
        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            if (url != null) {
                Glide.with(this)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.ivAvatar)
            }
        }

        viewModel.isUploading.observe(viewLifecycleOwner) { uploading ->
            binding.btnChangePhoto.isEnabled = !uploading
            binding.ivAvatar.alpha = if (uploading) 0.5f else 1f
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message.asString(requireContext()), Toast.LENGTH_SHORT).show()
                viewModel.message.value = null   // show it only once
            }
        }

        viewModel.loadProfile()
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
            binding.tvName.text = getString(R.string.user_default_name)
        } else {
            binding.tvName.text = name
        }
        binding.tvEmail.text = user?.email
    }
}
