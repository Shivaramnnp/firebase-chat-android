package com.shivasruthi.onlyss // <<<< YOUR ACTUAL PACKAGE NAME

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth // For getting current user
import com.google.firebase.auth.ktx.auth     // KTX for auth
import com.google.firebase.database.DatabaseReference // For RTDB reference
import com.google.firebase.database.ktx.database // KTX for database
import com.google.firebase.ktx.Firebase           // For Firebase app services
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFMService" // Tag for logging
        private const val CHANNEL_ID = "OnlySSChannel"
    }

    /**
     * Called when a new FCM registration token is generated for the app instance.
     * This method is called when the token is initially generated and whenever it's refreshed.
     *
     * @param token The new FCM registration token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "------------------------------------------")
        Log.d(TAG, "NEW FCM TOKEN GENERATED/REFRESHED: $token")
        Log.d(TAG, "------------------------------------------")

        // If you need to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server (Firebase RTDB in our case).
        sendRegistrationToServer(token)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        if (token == null) {
            Log.e(TAG, "sendRegistrationToServer: FCM token is null. Cannot update.")
            return
        }

        // Get the current authenticated user's UID
        val firebaseAuth: FirebaseAuth = Firebase.auth
        val currentUserId: String? = firebaseAuth.currentUser?.uid

        if (currentUserId != null) {
            // User is logged in, update their token in the Realtime Database
            val userTokenDatabasePath = "users/$currentUserId/fcmToken"
            val userTokenRef: DatabaseReference = Firebase.database.getReference(userTokenDatabasePath)

            Log.d(TAG, "Attempting to update FCM token in RTDB for user: $currentUserId at path: $userTokenDatabasePath")
            userTokenRef.setValue(token)
                .addOnSuccessListener {
                    Log.i(TAG, "Refreshed FCM Token ($token) successfully updated in RTDB for user $currentUserId.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update refreshed FCM Token in RTDB for user $currentUserId. Error: ${e.message}", e)
                    // TODO: Consider implementing retry logic or more robust error handling if this fails.
                    // For a chat app, if token update fails, user might not get notifications.
                }
        } else {
            // User is not logged in.
            // You might choose to:
            // 1. Store the token locally and try to upload it once the user logs in.
            // 2. Do nothing if your app logic assumes tokens are only relevant for logged-in users.
            // For "OnlySS", where the app is specific to two users who will be logged in to use it,
            // we primarily care about updating the token when they *are* logged in.
            // If a token refreshes while they are logged out, it will be updated upon their next login anyway.
            Log.w(TAG, "User not logged in. Refreshed FCM token ($token) not sent to server (RTDB) at this time.")
        }
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message Received - From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        // This is important if your Cloud Function sends custom data.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Example: If you send 'title' and 'body' in the data payload from your Cloud Function
            val title = remoteMessage.data["title"] // Or whatever keys you use
            val body = remoteMessage.data["body"]
            val senderName = remoteMessage.data["senderName"] // Custom field

            if (title != null && body != null) {
                val notificationBody = if (senderName != null) "$senderName: $body" else body
                sendNotification(title, notificationBody)
            } else {
                Log.w(TAG, "Data payload received but missing 'title' or 'body'. Using default notification.")
                // Fallback to notification payload if available or a generic message
                remoteMessage.notification?.let {
                    sendNotification(it.title ?: "New Message", it.body ?: "You have a new message.")
                } ?: sendNotification("New Message", "You have received a new message.")
            }
        }
        // Check if message contains a notification payload (Firebase console often sends this type).
        // This block is primarily for when the app is in the FOREGROUND and receives a 'notification' payload.
        // If the app is in background/killed and receives a 'notification' payload, FCM system tray handles it.
        else if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: ${remoteMessage.notification!!.body}")
            sendNotification(
                remoteMessage.notification!!.title ?: "New Message",
                remoteMessage.notification!!.body ?: "You have a new message."
            )
        } else {
            Log.w(TAG, "Received FCM message without data or notification payload.")
        }
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java) // Intent to open MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // <<<< YOUR APP'S LAUNCHER ICON
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up display on newer Android

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OnlySS Chat Messages", // User-visible name of the channel
                NotificationManager.IMPORTANCE_HIGH // Importance for heads-up, sound, etc.
            )
            channel.description = "Notifications for new messages in OnlySS"
            // You can also configure lights, vibration, etc. on the channel
            // channel.enableLights(true)
            // channel.lightColor = Color.RED
            // channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_ID' created or already exists.")
        } else {
            Log.d(TAG, "Pre-Oreo, no notification channel needed.")
        }

        val notificationId = System.currentTimeMillis().toInt() // Unique ID for each notification
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification sent with ID: $notificationId, Title: '$messageTitle'")
    }
}