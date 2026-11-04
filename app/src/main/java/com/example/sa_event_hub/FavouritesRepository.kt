package com.example.sa_event_hub

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

// The screens talk to this class, never straight to the network, for saved events.
// Every change is written to the phone first (so it works offline) and a background
// job (SyncWorker) sends it to the server as soon as there is internet.
class FavouritesRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val dao = AppDatabase.get(appContext).savedEventDao()

    val saved: Flow<List<SavedEventEntity>> = dao.observeSaved()

    fun isSaved(eventId: String): Flow<Boolean> = dao.observeIsSaved(eventId)

    suspend fun getLocal(eventId: String): SavedEventEntity? = dao.get(eventId)

    // Save an event. Works with no internet.
    suspend fun save(event: Event) {
        Log.d(TAG, "Saving ${event.id} on the phone, sync pending")
        dao.upsert(event.toEntity(SyncState.PENDING_ADD))
        syncNow()
    }

    // Remove an event. It disappears from the screen straight away.
    suspend fun remove(eventId: String) {
        if (dao.get(eventId) == null) return
        Log.d(TAG, "Removing $eventId on the phone, sync pending")
        dao.setState(eventId, SyncState.PENDING_DELETE)
        syncNow()
    }

    // Asks WorkManager to run the sync once the phone is online.
    // If it is offline, WorkManager waits and runs it when the connection returns.
    // Reference: Android Developers, "WorkManager: define work requests".
    fun syncNow() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    // Called on log out, so the next person to log in does not see this user's events.
    suspend fun clearLocal() {
        WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME)
        dao.clearAll()
        Log.d(TAG, "Local saved events cleared")
    }

    companion object {
        private const val TAG = "FavouritesRepository"
        private const val WORK_NAME = "sync_favourites"

        @Volatile private var instance: FavouritesRepository? = null

        fun get(context: Context): FavouritesRepository =
            instance ?: synchronized(this) {
                instance ?: FavouritesRepository(context).also { instance = it }
            }
    }
}
