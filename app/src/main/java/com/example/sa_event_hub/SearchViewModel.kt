package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads events for the Search tab, using whatever filters are chosen.
class SearchViewModel : ViewModel() {

    val results = MutableLiveData<List<Event>>(emptyList())
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    // The filters currently chosen. null means "no filter".
    var keyword: String = ""
        private set
    var city: String? = null
        private set
    var category: String? = null
        private set

    fun setKeyword(text: String) {
        keyword = text.trim()
        search()
    }

    fun setCity(newCity: String?) {
        city = newCity
        search()
    }

    fun setCategory(newCategory: String?) {
        category = newCategory
        search()
    }

    // Asks the API for events using the current filters
    fun search() {
        isLoading.value = true
        errorMessage.value = null

        val call = if (keyword.isEmpty()) {
            // No search text: just filter by city/category
            ApiClient.service.getEvents(city, category, 0, 30)
        } else {
            ApiClient.service.searchEvents(keyword, city, category, 0, 30)
        }

        call.enqueue(object : Callback<EventListResponse> {
            override fun onResponse(call: Call<EventListResponse>, response: Response<EventListResponse>) {
                isLoading.value = false
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    results.value = body.events
                    errorMessage.value = if (body.events.isEmpty()) "No events found. Try different filters." else null
                } else {
                    errorMessage.value = "Could not search events (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<EventListResponse>, t: Throwable) {
                isLoading.value = false
                Log.w("SearchViewModel", "Could not reach the API", t)
                errorMessage.value = "No internet connection."
            }
        })
    }
}