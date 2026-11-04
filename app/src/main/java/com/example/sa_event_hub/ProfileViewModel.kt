package com.example.sa_event_hub

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads the profile picture and uploads a new one.
// The picture goes to our API, which stores it in Supabase Storage.
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    val photoUrl = MutableLiveData<String?>(null)
    val isUploading = MutableLiveData(false)
    val message = MutableLiveData<UiText?>(null)   // a short message to show in a Toast

    // Gets the saved picture address from the API
    fun loadProfile() {
        ApiClient.service.getProfile().enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    photoUrl.value = response.body()?.photoUrl?.takeIf { it.isNotBlank() }
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.w("ProfileViewModel", "Could not load the profile", t)
            }
        })
    }

    // Shrinks the chosen picture and sends it to the API
    fun uploadPhoto(uri: Uri) {
        if (isUploading.value == true) return
        isUploading.value = true

        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) { ImageUtils.toJpegBytes(getApplication(), uri) }
            if (bytes == null) {
                isUploading.value = false
                message.value = UiText(R.string.err_photo_read)
                return@launch
            }

            val body = bytes.toRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("photo", "avatar.jpg", body)

            ApiClient.service.uploadPhoto(part).enqueue(object : Callback<PhotoResponse> {
                override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
                    isUploading.value = false
                    val result = response.body()
                    if (response.isSuccessful && result != null) {
                        Log.d("ProfileViewModel", "Photo uploaded: ${result.photoUrl}")
                        photoUrl.value = result.photoUrl
                        message.value = UiText(R.string.toast_photo_updated)
                    } else {
                        Log.w("ProfileViewModel", "Upload failed: ${response.code()}")
                        message.value = UiText(R.string.err_upload_photo)
                    }
                }

                override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                    isUploading.value = false
                    Log.w("ProfileViewModel", "Could not reach the API", t)
                    message.value = UiText(R.string.err_no_internet)
                }
            })
        }
    }
}
