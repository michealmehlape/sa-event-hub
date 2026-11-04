package com.example.sa_event_hub

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Turns the date and time from the API into text people can read.
object EventFormat {

    // Used by the screens: formats the date in the language the user chose.
    fun dateAndTime(context: Context, date: String?, time: String?): String {
        val locale = context.resources.configuration.locales[0]
        return dateAndTime(
            date, time, locale,
            joiner = { d, t -> context.getString(R.string.date_at_time, d, t) },
            tbdText = context.getString(R.string.date_tbc)
        )
    }

    // "2026-12-15" and "20:00:00"  becomes  "15 Dec 2026 at 20:00"
    // The defaults are English, so the unit tests need no Android context.
    fun dateAndTime(
        date: String?,
        time: String?,
        locale: Locale = Locale.ENGLISH,
        joiner: (String, String) -> String = { d, t -> "$d at $t" },
        tbdText: String = "Date to be announced"
    ): String {
        if (date.isNullOrBlank()) return tbdText

        return try {
            val day = LocalDate.parse(date)
            val dayText = day.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
            if (time.isNullOrBlank()) dayText else joiner(dayText, time.take(5))
        } catch (e: Exception) {
            date   // if the date looks strange, just show it as it is
        }
    }
}