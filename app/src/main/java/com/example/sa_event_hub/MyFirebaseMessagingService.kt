package com.example.sa_event_hub

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Locale

// Receives push notifications from Firebase Cloud Messaging (FCM).
// Our API sends "data" messages, so this function runs whether the app is open or closed,
// and we build the notification here in the language the user chose.
// Reference: Firebase documentation, "Receive messages in an Android app".
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Firebase gives the phone a new address (token). Tell our API about it.
    override fun onNewToken(token: String) {
        Log.d(TAG, "New push token received")
        PushTokens.upload(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Push received: ${message.data}")

        // Respect the notifications switch in Settings
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        if (!prefs.getBoolean("notifications", true)) {
            Log.d(TAG, "Notifications are switched off, ignoring")
            return
        }

        if (message.data["type"] == "comment") {
            showCommentNotification(message.data)
        }
    }

    private fun showCommentNotification(data: Map<String, String>) {
        // On Android 13 and newer the user must allow notifications
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Notification permission not granted")
            return
        }

        val text = localizedContext()
        createChannel(text)

        // Tapping the notification opens the post
        val intent = Intent(this, PostDetailsActivity::class.java).apply {
            putExtra(PostDetailsActivity.EXTRA_POST_ID, data["postId"])
            putExtra(PostDetailsActivity.EXTRA_POST_AUTHOR, data["postAuthor"])
            putExtra(PostDetailsActivity.EXTRA_POST_BODY, data["postBody"])
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = (data["postId"] ?: "").hashCode()
        val pending = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(text.getString(R.string.notif_comment_title))
            .setContentText(text.getString(R.string.notif_comment_body, data["commenterName"] ?: ""))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(this).notify(requestCode, notification)
    }

    private fun createChannel(text: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                text.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = text.getString(R.string.notif_channel_description) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    // A service does not get the app language automatically on older Android versions,
    // so we use the language the user chose in Settings (saved there).
    private fun localizedContext(): Context {
        val tag = getSharedPreferences("settings", MODE_PRIVATE).getString("language", null)
            ?: return this
        val config = Configuration(resources.configuration)
        config.setLocale(Locale.forLanguageTag(tag))
        return createConfigurationContext(config)
    }

    companion object {
        private const val TAG = "PushService"
        const val CHANNEL_ID = "community_updates"
    }
}
