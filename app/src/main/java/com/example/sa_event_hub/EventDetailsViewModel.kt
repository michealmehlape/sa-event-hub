package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventDetailsViewModel : ViewModel() {

    val event = MutableLiveData<Event?>(null)
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)
    val isSaved = MutableLiveData(false)
    private var working = false   // stops double-taps on the Save button

    // Loads the full details of one event
    fun loadEvent(eventId: String) {
        isLoading.value = true
        ApiClient.service.getEvent(eventId).enqueue(object : Callback<Event> {
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                isLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    event.value = response.body()
                    checkIfSaved(eventId)
                } else {
                    errorMessage.value = "Could not load this event (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<Event>, t: Throwable) {
                isLoading.value = false
                Log.w("EventDetailsViewModel", "Could not reach the API", t)
                errorMessage.value = "No internet connection."
            }
        })
    }

    // Looks through the user's saved events to see if this one is there
    private fun checkIfSaved(eventId: String) {
        ApiClient.service.getFavourites().enqueue(object : Callback<FavouriteListResponse> {
            override fun onResponse(call: Call<FavouriteListResponse>, response: Response<FavouriteListResponse>) {
                val list = response.body()?.favourites ?: emptyList()
                isSaved.value = list.any { it.id == eventId }
            }

            override fun onFailure(call: Call<FavouriteListResponse>, t: Throwable) {
                Log.w("EventDetailsViewModel", "Could not check saved events", t)
                // Not a big problem: the button just starts as "not saved"
            }
        })
    }

    // Called when the user taps the Save button. Saves it, or removes it if already saved.
    fun toggleSave(eventId: String) {
        if (working) return
        working = true

        if (isSaved.value == true) {
            ApiClient.service.removeFavourite(eventId).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    working = false
                    if (response.isSuccessful) {
                        isSaved.value = false
                    } else {
                        errorMessage.value = "Could not remove this event."
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    working = false
                    errorMessage.value = "No internet connection."
                }
            })
        } else {
            ApiClient.service.addFavourite(FavouriteRequest(eventId)).enqueue(object : Callback<Event> {
                override fun onResponse(call: Call<Event>, response: Response<Event>) {
                    working = false
                    // 409 means it was already saved, which is fine too
                    if (response.isSuccessful || response.code() == 409) {
                        isSaved.value = true
                    } else {
                        errorMessage.value = "Could not save this event."
                    }
                }

                override fun onFailure(call: Call<Event>, t: Throwable) {
                    working = false
                    errorMessage.value = "No internet connection."
                }
            })
        }
    }
}