package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads the list of events for the Home screen.
class HomeViewModel : ViewModel() {

    // The Home screen watches these three values
    val events = MutableLiveData<List<Event>>(emptyList())
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    fun loadEvents() {
        isLoading.value = true
        errorMessage.value = null

        // Ask our API for the first 30 events (no city or category filter)
        ApiClient.service.getEvents(null, null, 0, 30)
            .enqueue(object : Callback<EventListResponse> {

                // The API answered
                override fun onResponse(call: Call<EventListResponse>, response: Response<EventListResponse>) {
                    isLoading.value = false
                    val body = response.body()

                    if (response.isSuccessful && body != null) {
                        Log.d("HomeViewModel", "Got ${body.events.size} events")
                        events.value = body.events
                        if (body.events.isEmpty()) {
                            errorMessage.value = "No events found right now."
                        }
                    } else {
                        Log.w("HomeViewModel", "API error: ${response.code()}")
                        errorMessage.value = "Could not load events (error ${response.code()})."
                    }
                }

                // We could not reach the API (for example, no internet)
                override fun onFailure(call: Call<EventListResponse>, t: Throwable) {
                    isLoading.value = false
                    Log.w("HomeViewModel", "Could not reach the API", t)
                    errorMessage.value = "No internet connection. Please try again."
                }
            })
    }
}