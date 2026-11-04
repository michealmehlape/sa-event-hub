package com.example.sa_event_hub

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Runs in the background when there is internet.
// 1. Sends every change made offline to the API.
// 2. Then fetches the server's list, so events saved on another phone appear here.
// The server is the source of truth once a change has been sent.
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.d(TAG, "Nobody is logged in, nothing to sync")
            return@withContext Result.success()
        }

        val dao = AppDatabase.get(applicationContext).savedEventDao()
        val api = ApiClient.service
        var allDone = true

        try {
            // STEP 1: push the offline changes
            for (item in dao.getPending()) {
                when (item.syncState) {
                    SyncState.PENDING_ADD -> {
                        val r = api.addFavourite(FavouriteRequest(item.id)).execute()
                        when {
                            // 201 = saved, 409 = the server already had it. Both mean done.
                            r.isSuccessful || r.code() == 409 -> {
                                dao.setState(item.id, SyncState.SYNCED)
                                Log.d(TAG, "Pushed save for ${item.id}")
                            }
                            // The event no longer exists, so stop trying
                            r.code() == 404 -> {
                                dao.delete(item.id)
                                Log.w(TAG, "Event ${item.id} not found on the server, dropped")
                            }
                            else -> {
                                allDone = false
                                Log.w(TAG, "Save for ${item.id} failed: ${r.code()}")
                            }
                        }
                    }
                    SyncState.PENDING_DELETE -> {
                        val r = api.removeFavourite(item.id).execute()
                        // 200 = removed, 404 = it was already gone. Both mean done.
                        if (r.isSuccessful || r.code() == 404) {
                            dao.delete(item.id)
                            Log.d(TAG, "Pushed removal for ${item.id}")
                        } else {
                            allDone = false
                            Log.w(TAG, "Removal for ${item.id} failed: ${r.code()}")
                        }
                    }
                    SyncState.SYNCED -> { }
                }
            }

            // STEP 2: pull the server's list
            val response = api.getFavourites().execute()
            val remote = response.body()?.favourites
            if (response.isSuccessful && remote != null) {
                val remoteIds = remote.map { it.id }.toSet()
                val local = dao.getAll().associateBy { it.id }
                val now = System.currentTimeMillis()

                // Saved on another device: add it here (keeps the server's order)
                remote.forEachIndexed { index, event ->
                    if (local[event.id] == null) {
                        dao.upsert(event.toEntity(SyncState.SYNCED, now - index))
                        Log.d(TAG, "Pulled ${event.id} from the server")
                    }
                }
                // Removed on another device: remove it here. Pending local changes are never touched.
                local.values
                    .filter { it.syncState == SyncState.SYNCED && it.id !in remoteIds }
                    .forEach {
                        dao.delete(it.id)
                        Log.d(TAG, "Removed ${it.id}, it was deleted elsewhere")
                    }
            } else {
                allDone = false
                Log.w(TAG, "Could not pull saved events: ${response.code()}")
            }
        } catch (e: IOException) {
            Log.w(TAG, "Network problem, will retry", e)
            return@withContext Result.retry()
        }

        when {
            allDone -> Result.success()
            runAttemptCount < 5 -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}
