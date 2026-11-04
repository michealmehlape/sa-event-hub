package com.example.sa_event_hub

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// One event's details. Saving works offline through the local database.
class EventDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavouritesRepository.get(application)

    val event = MutableLiveData<Event?>(null)
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<UiText?>(null)

    // Follows the local database, so the Save button is always right, even offline
    private val currentId = MutableLiveData<String>()
    val isSaved: LiveData<Boolean> = currentId.switchMap { id ->
        repository.isSaved(id).asLiveData()
    }

    // Loads the full details of one event
    fun loadEvent(eventId: String) {
        currentId.value = eventId
        isLoading.value = true
        ApiClient.service.getEvent(eventId).enqueue(object : Callback<Event> {
            override fun onResponse(call: Call<Event>, response: Response<Event>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    isLoading.value = false
                    event.value = body
                } else {
                    useSavedCopy(eventId, UiText(R.string.err_load_event, response.code()))
                }
            }

            override fun onFailure(call: Call<Event>, t: Throwable) {
                Log.w("EventDetailsViewModel", "Could not reach the API", t)
                useSavedCopy(eventId, UiText(R.string.err_no_internet))
            }
        })
    }

    // If the API cannot be reached, show the copy saved on the phone (if there is one)
    private fun useSavedCopy(eventId: String, error: UiText) {
        viewModelScope.launch {
            val local = repository.getLocal(eventId)
            isLoading.value = false
            if (local != null) {
                Log.d("EventDetailsViewModel", "Showing the saved copy of $eventId")
                event.value = local.toEvent()
            } else {
                errorMessage.value = error
            }
        }
    }

    // Save button: saves the event, or removes it if it is already saved
    fun toggleSave(eventId: String) {
        val current = event.value ?: return
        viewModelScope.launch {
            try {
                if (isSaved.value == true) repository.remove(eventId) else repository.save(current)
            } catch (e: Exception) {
                Log.e("EventDetailsViewModel", "Could not update the saved event", e)
                errorMessage.value = UiText(
                    if (isSaved.value == true) R.string.err_remove_event else R.string.err_save_event
                )
            }
        }
    }
}
