package com.example.sa_event_hub

import androidx.room.Entity
import androidx.room.PrimaryKey

// Where a saved event is in its journey to the online database.
enum class SyncState {
    SYNCED,          // the server already knows about it
    PENDING_ADD,     // saved on the phone, still has to be sent to the server
    PENDING_DELETE   // removed on the phone, the server still has to be told
}

// One saved event stored on the phone (Room table). The fields match our Event class,
// plus syncState and savedAt.
// Reference: Android Developers, "Save data in a local database using Room".
@Entity(tableName = "saved_events")
data class SavedEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val date: String?,
    val time: String?,
    val venue: String?,
    val city: String?,
    val category: String?,
    val imageUrl: String?,
    val ticketUrl: String?,
    val syncState: SyncState,
    val savedAt: Long
)

// Room row -> the Event class the screens already use
fun SavedEventEntity.toEvent() = Event(
    id = id, title = title, description = description, date = date, time = time,
    venue = venue, city = city, category = category, imageUrl = imageUrl, ticketUrl = ticketUrl
)

// Event -> Room row
fun Event.toEntity(state: SyncState, savedAt: Long = System.currentTimeMillis()) = SavedEventEntity(
    id = id, title = title, description = description, date = date, time = time,
    venue = venue, city = city, category = category, imageUrl = imageUrl, ticketUrl = ticketUrl,
    syncState = state, savedAt = savedAt
)
