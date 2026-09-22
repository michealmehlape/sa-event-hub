package com.example.sa_event_hub

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Turns the date and time from the API into text people can read.
object EventFormat {

    // "2026-12-15" and "20:00:00"  becomes  "15 Dec 2026 at 20:00"
    fun dateAndTime(date: String?, time: String?): String {
        if (date.isNullOrBlank()) return "Date to be announced"

        return try {
            val day = LocalDate.parse(date)
            val dayText = day.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
            if (time.isNullOrBlank()) dayText else "$dayText at ${time.take(5)}"
        } catch (e: Exception) {
            date   // if the date looks strange, just show it as it is
        }
    }
}