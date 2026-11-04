package com.example.sa_event_hub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.flow.map

// The Saved tab. It reads from the phone's database, so it works offline.
class SavedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavouritesRepository.get(application)

    val favourites: LiveData<List<Event>> =
        repository.saved.map { list -> list.map { it.toEvent() } }.asLiveData()

    val isLoading = MutableLiveData(false)

    // Shows "no saved events" when the list is empty
    val errorMessage: LiveData<UiText?> = favourites.map { list ->
        if (list.isEmpty()) UiText(R.string.no_saved_events) else null
    }

    // Called when the tab is shown: syncs with the server when online
    fun loadFavourites() {
        repository.syncNow()
    }
}
