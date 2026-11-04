package com.example.sa_event_hub

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// The questions we can ask the local database.
@Dao
interface SavedEventDao {

    // The Saved tab. Events waiting to be deleted are hidden straight away.
    @Query("SELECT * FROM saved_events WHERE syncState != 'PENDING_DELETE' ORDER BY savedAt DESC")
    fun observeSaved(): Flow<List<SavedEventEntity>>

    // Is this event saved? (drives the Save button)
    @Query("SELECT EXISTS(SELECT 1 FROM saved_events WHERE id = :id AND syncState != 'PENDING_DELETE')")
    fun observeIsSaved(id: String): Flow<Boolean>

    @Query("SELECT * FROM saved_events WHERE id = :id")
    suspend fun get(id: String): SavedEventEntity?

    @Query("SELECT * FROM saved_events")
    suspend fun getAll(): List<SavedEventEntity>

    // Everything that still has to be sent to the server
    @Query("SELECT * FROM saved_events WHERE syncState != 'SYNCED'")
    suspend fun getPending(): List<SavedEventEntity>

    @Upsert
    suspend fun upsert(event: SavedEventEntity)

    @Query("UPDATE saved_events SET syncState = :state WHERE id = :id")
    suspend fun setState(id: String, state: SyncState)

    @Query("DELETE FROM saved_events WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM saved_events")
    suspend fun clearAll()
}
