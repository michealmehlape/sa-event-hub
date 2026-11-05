package com.example.sa_event_hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Tests for the code that converts between an Event (from the API)
// and a SavedEventEntity (stored on the phone with Room).
class SavedEventEntityTest {

    private val event = Event(
        id = "Z698xZ05Z16v8Va8Qp",
        title = "The Kehlani World Tour",
        description = "An evening of R&B.",
        date = "2026-12-15",
        time = "20:00:00",
        venue = "Sun Arena",
        city = "Pretoria",
        category = "Music",
        imageUrl = "https://example.com/kehlani.jpg",
        ticketUrl = "https://example.com/tickets"
    )

    @Test
    fun eventToEntityAndBack_keepsEveryField() {
        val entity = event.toEntity(SyncState.SYNCED)
        assertEquals(event, entity.toEvent())
    }

    @Test
    fun toEntity_setsTheSyncStateItWasGiven() {
        assertEquals(SyncState.PENDING_ADD, event.toEntity(SyncState.PENDING_ADD).syncState)
        assertEquals(SyncState.PENDING_DELETE, event.toEntity(SyncState.PENDING_DELETE).syncState)
        assertEquals(SyncState.SYNCED, event.toEntity(SyncState.SYNCED).syncState)
    }

    @Test
    fun toEntity_usesTheSavedTimeWeGive() {
        val entity = event.toEntity(SyncState.SYNCED, savedAt = 12345L)
        assertEquals(12345L, entity.savedAt)
    }

    @Test
    fun emptyOptionalFields_stayEmpty() {
        val bare = Event(
            id = "abc", title = "Mystery event", description = null, date = null, time = null,
            venue = null, city = null, category = null, imageUrl = null, ticketUrl = null
        )
        val back = bare.toEntity(SyncState.PENDING_ADD).toEvent()
        assertEquals("abc", back.id)
        assertEquals("Mystery event", back.title)
        assertNull(back.date)
        assertNull(back.imageUrl)
        assertNull(back.ticketUrl)
    }
}
