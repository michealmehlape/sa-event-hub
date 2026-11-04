package com.example.sa_event_hub

import android.content.Context
import androidx.annotation.StringRes

// Holds a message as a string resource ID (plus any values to insert), not as plain text.
// The screen turns it into text with its own context, so it always shows in the language
// the user chose in Settings. (Reference: Android Developers, "Localize your app".)
class UiText(@StringRes val resId: Int, vararg val args: Any) {
    fun asString(context: Context): String = context.getString(resId, *args)
}
