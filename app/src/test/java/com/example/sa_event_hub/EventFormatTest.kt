package com.example.sa_event_hub

import org.junit.Assert.assertEquals
import org.junit.Test

class EventFormatTest {

    @Test
    fun dateAndTime_areFormattedNicely() {
        assertEquals("15 Dec 2026 at 20:00", EventFormat.dateAndTime("2026-12-15", "20:00:00"))
    }

    @Test
    fun dateWithoutTime_showsOnlyTheDate() {
        assertEquals("15 Dec 2026", EventFormat.dateAndTime("2026-12-15", null))
    }

    @Test
    fun missingDate_saysToBeAnnounced() {
        assertEquals("Date to be announced", EventFormat.dateAndTime(null, null))
    }
}