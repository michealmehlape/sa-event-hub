package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads the events the user has saved.
class SavedViewModel : ViewModel() {

    val favourites = MutableLiveData<List<Event>>(emptyList())
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    fun loadFavourites() {
        isLoading.value = true
        errorMessage.value = null

        ApiClient.service.getFavourites().enqueue(object : Callback<FavouriteListResponse> {
            override fun onResponse(call: Call<FavouriteListResponse>, response: Response<FavouriteListResponse>) {
                isLoading.value = false
                val list = response.body()?.favourites

                if (response.isSuccessful && list != null) {
                    favourites.value = list
                    errorMessage.value = if (list.isEmpty()) "You have not saved any events yet." else null
                } else {
                    errorMessage.value = "Could not load your saved events (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<FavouriteListResponse>, t: Throwable) {
                isLoading.value = false
                Log.w("SavedViewModel", "Could not reach the API", t)
                errorMessage.value = "No internet connection."
            }
        })
    }
}