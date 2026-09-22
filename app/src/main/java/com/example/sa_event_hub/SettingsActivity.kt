package com.example.sa_event_hub

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sa_event_hub.databinding.ActivitySettingsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

// The Settings screen. The user can change their name, password, notification choice and event interests.
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences   // a small file on the phone for simple settings
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Shows the saved settings on the screen
        loadSettings()

        // BUTTONS
        binding.tvBack.setOnClickListener { finish() }
        binding.btnSaveName.setOnClickListener { saveName() }
        binding.btnSaveInterests.setOnClickListener { saveInterests() }
        binding.btnChangePassword.setOnClickListener { changePassword() }

        // The notifications switch saves as soon as it is changed
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            Log.d("SettingsActivity", "Notifications turned on = $isChecked")
        }
    }

    // Fills the screen with the saved values
    private fun loadSettings() {
        // Name
        binding.etName.setText(auth.currentUser?.displayName ?: "")

        // Notifications (the default is ON)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications", true)

        // Interests
        val interests = prefs.getStringSet("interests", emptySet()) ?: emptySet()
        binding.cbMusic.isChecked = interests.contains("Music")
        binding.cbSports.isChecked = interests.contains("Sports")
        binding.cbArts.isChecked = interests.contains("Arts & Culture")
        binding.cbComedy.isChecked = interests.contains("Comedy")
        binding.cbBusiness.isChecked = interests.contains("Business")
    }

    // CHANGE NAME
    private fun saveName() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show()
            return
        }

        val changes = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.currentUser?.updateProfile(changes)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("SettingsActivity", "Name changed")
                Toast.makeText(this, "Name saved.", Toast.LENGTH_SHORT).show()
            } else {
                Log.w("SettingsActivity", "Name change failed", task.exception)
                Toast.makeText(this, "Could not save your name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SAVE INTERESTS
    private fun saveInterests() {
        val interests = mutableSetOf<String>()
        if (binding.cbMusic.isChecked) interests.add("Music")
        if (binding.cbSports.isChecked) interests.add("Sports")
        if (binding.cbArts.isChecked) interests.add("Arts & Culture")
        if (binding.cbComedy.isChecked) interests.add("Comedy")
        if (binding.cbBusiness.isChecked) interests.add("Business")

        prefs.edit().putStringSet("interests", interests).apply()
        Log.d("SettingsActivity", "Interests saved: $interests")
        Toast.makeText(this, "Interests saved.", Toast.LENGTH_SHORT).show()
    }

    // CHANGE PASSWORD
    private fun changePassword() {
        val current = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmNewPassword.text.toString()

        // 1. Check what the user typed
        val problem = Validators.checkPasswordChange(current, newPassword, confirm)
        if (problem != null) {
            Toast.makeText(this, problem, Toast.LENGTH_LONG).show()
            return
        }

        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Firebase needs the old password first, to make sure it is really the user
        val credential = EmailAuthProvider.getCredential(email, current)
        user.reauthenticate(credential).addOnCompleteListener { checkTask ->
            if (!checkTask.isSuccessful) {
                Toast.makeText(this, "Your current password is wrong.", Toast.LENGTH_LONG).show()
                return@addOnCompleteListener
            }

            // 3. The old password was right, so set the new one
            user.updatePassword(newPassword).addOnCompleteListener { changeTask ->
                if (changeTask.isSuccessful) {
                    Log.d("SettingsActivity", "Password changed")
                    Toast.makeText(this, "Password changed.", Toast.LENGTH_SHORT).show()
                    binding.etCurrentPassword.text?.clear()
                    binding.etNewPassword.text?.clear()
                    binding.etConfirmNewPassword.text?.clear()
                } else {
                    Log.w("SettingsActivity", "Password change failed", changeTask.exception)
                    Toast.makeText(this, "Could not change the password.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}