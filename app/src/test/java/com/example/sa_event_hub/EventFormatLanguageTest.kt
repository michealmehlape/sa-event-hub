package com.example.sa_event_hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Locale

// Tests for the date text in other languages and for the extra options of EventFormat.
class EventFormatLanguageTest {

    @Test
    fun customJoinerAndText_areUsed() {
        // This is how the app passes in the translated words
        val text = EventFormat.dateAndTime(
            "2026-12-15", "20:00:00",
            joiner = { day, time -> "$day om $time" }
        )
        assertEquals("15 Dec 2026 om 20:00", text)
    }

    @Test
    fun missingDate_usesTheTranslatedText() {
        val text = EventFormat.dateAndTime(null, null, tbdText = "Datum word nog aangekondig")
        assertEquals("Datum word nog aangekondig", text)
    }

    @Test
    fun blankDate_countsAsMissing() {
        assertEquals("Date to be announced", EventFormat.dateAndTime("", "20:00:00"))
    }

    @Test
    fun timeIsShortenedToHoursAndMinutes() {
        assertEquals("15 Dec 2026 at 09:30", EventFormat.dateAndTime("2026-12-15", "09:30:45"))
    }

    @Test
    fun strangeDate_isShownAsItIs() {
        assertEquals("next Friday", EventFormat.dateAndTime("next Friday", "20:00:00"))
    }

    @Test
    fun afrikaansLocale_usesAfrikaansMonthNames() {
        val afrikaans = Locale.forLanguageTag("af")
        // Some Java builds do not include Afrikaans data. Skip the test there instead of failing.
        assumeTrue(Locale.getAvailableLocales().any { it.language == "af" })

        val text = EventFormat.dateAndTime("2026-12-15", "20:00:00", locale = afrikaans)
        assertTrue("Expected the Afrikaans month in: $text", text.contains("Des"))
    }
}
