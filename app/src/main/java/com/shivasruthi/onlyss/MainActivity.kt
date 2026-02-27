package com.shivasruthi.onlyss

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shivasruthi.onlyss.ui.theme.OnlySSTheme
import com.shivasruthi.onlyss.utils.generateChannelName // Example import

// Define sealed class for navigation states
sealed class Screen {
    object Login : Screen()
    object Chat : Screen()
    data class Call(val partnerName: String, val channelName: String) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var presenceReference: DatabaseReference? = null
    private var userPresenceListener: ValueEventListener? = null

    private var onPermissionGranted: (() -> Unit)? = null

    // Prepare the permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permissions", "Microphone permission GRANTED")
                onPermissionGranted?.invoke()
            } else {
                Log.w("Permissions", "Microphone permission DENIED")
                // TODO: Show a message to the user that permission is needed for calls
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database

        setContent {
            OnlySSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthAndNavGate()
                }
            }
        }
    }

    // Manages online presence setup when a user is confirmed to be logged in
    private fun setupOnlinePresence(userId: String) {
        if (userPresenceListener != null) {
            Log.d("Presence", "Presence listener already active for $userId. Skipping setup.")
            return
        }
        Log.d("Presence", "Setting up online presence for user: $userId")
        presenceReference = database.getReference("/presence/$userId")
        val connectedRef = database.getReference(".info/connected")

        userPresenceListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("Presence", "Firebase connected. Setting user online and onDisconnect.")
                    val onlineStatus = mapOf(
                        "isOnline" to true,
                        "lastSeen" to ServerValue.TIMESTAMP
                    )
                    val offlineStatus = mapOf(
                        "isOnline" to false,
                        "lastSeen" to ServerValue.TIMESTAMP
                    )

                    presenceReference?.setValue(onlineStatus)
                        ?.addOnSuccessListener { Log.d("Presence", "User $userId set to online.") }
                        ?.addOnFailureListener { e -> Log.e("Presence", "Failed to set user $userId online.", e) }

                    presenceReference?.onDisconnect()?.setValue(offlineStatus)
                        ?.addOnSuccessListener { Log.d("Presence", "onDisconnect for $userId set to offline.") }
                        ?.addOnFailureListener { e -> Log.e("Presence", "Failed to set onDisconnect for $userId.", e) }
                } else {
                    Log.d("Presence", "Firebase disconnected. Status likely updated by onDisconnect if it was set.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Presence", "Listener for .info/connected was cancelled", error.toException())
            }
        }
        connectedRef.addValueEventListener(userPresenceListener!!)
    }

    // Handles tearing down presence listeners and explicitly setting offline if needed
    private fun goOffline() {
        val userId = auth.currentUser?.uid
        Log.d("Presence", "goOffline called. Current user: $userId")

        userPresenceListener?.let {
            database.getReference(".info/connected").removeEventListener(it)
            userPresenceListener = null
            Log.d("Presence", ".info/connected listener removed.")
        }

        userId?.let { uid ->
            val userSpecificPresenceRef = database.getReference("/presence/$uid")
            val offlineStatus = mapOf(
                "isOnline" to false,
                "lastSeen" to ServerValue.TIMESTAMP
            )
            userSpecificPresenceRef.setValue(offlineStatus)
                ?.addOnSuccessListener { Log.d("Presence", "User $uid explicitly set to offline.") }
                ?.addOnFailureListener { e -> Log.e("Presence", "Failed to set user $uid offline explicitly.", e) }

            userSpecificPresenceRef.onDisconnect()?.cancel()
                ?.addOnSuccessListener { Log.d("Presence", "Pending onDisconnect operations cancelled for $uid.") }
                ?.addOnFailureListener { e -> Log.e("Presence", "Failed to cancel onDisconnect for $uid.", e) }
        }
    }

    private fun askForMicrophonePermission(onGranted: () -> Unit) {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                onGranted()
            }
            else -> {
                // Permission is not granted, launch the request
                onPermissionGranted = onGranted
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    @Composable
    fun AuthAndNavGate() {
        var currentUserId by rememberSaveable { mutableStateOf(auth.currentUser?.uid) }
        var isLoadingLogin by rememberSaveable { mutableStateOf(false) }
        var loginError by rememberSaveable { mutableStateOf<String?>(null) }
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

        // Handle auth state changes and presence management
        LaunchedEffect(key1 = auth.currentUser) {
            val firebaseUser = auth.currentUser
            val oldUserId = currentUserId
            currentUserId = firebaseUser?.uid
            Log.d("AuthGate_State", "Auth state checked. New UID: $currentUserId (Prev: $oldUserId)")

            if (currentUserId != null && currentUserId != oldUserId) {
                Log.d("AuthGate_State", "User $currentUserId logged IN or changed. Setting up presence.")
                setupOnlinePresence(currentUserId!!)
                currentScreen = Screen.Chat
            } else if (currentUserId == null && oldUserId != null) {
                Log.d("AuthGate_State", "User $oldUserId logged OUT.")
                currentScreen = Screen.Login
            } else if (currentUserId != null && currentUserId == oldUserId) {
                if (userPresenceListener == null) {
                    Log.d("AuthGate_State", "User $currentUserId already logged in but presence listener was null. Setting up.")
                    setupOnlinePresence(currentUserId!!)
                }
                currentScreen = Screen.Chat
            } else {
                currentScreen = Screen.Login
            }
        }

        when (val screen = currentScreen) {
            is Screen.Login -> {
                LoginScreen(
                    onLoginSuccess = { email, password ->
                        if (email.isBlank() || password.isBlank()) {
                            loginError = "Email and Password cannot be empty."
                            return@LoginScreen
                        }
                        isLoadingLogin = true
                        loginError = null
                        Log.d("AuthGate_Login", "Attempting login for email: $email")

                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@MainActivity) { task ->
                                isLoadingLogin = false
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    Log.d("AuthGate_Login", "Login SUCCESS. Firebase User UID: ${user?.uid}")
                                    // currentUserId will update via LaunchedEffect observing auth.currentUser
                                } else {
                                    Log.w("AuthGate_Login", "Login FAILURE.", task.exception)
                                    val exception = task.exception
                                    loginError = when (exception) {
                                        is FirebaseAuthException -> when (exception.errorCode.lowercase()) {
                                            "error_invalid_credential", "error_user_not_found", "error_wrong_password" -> "Invalid email or password."
                                            "error_invalid_email" -> "The email address is badly formatted."
                                            "error_user_disabled" -> "This user account has been disabled."
                                            else -> exception.localizedMessage ?: "Authentication failed."
                                        }
                                        else -> exception?.localizedMessage ?: "An unknown error occurred."
                                    }
                                    Log.e("AuthGate_Login", "Login error message set to: $loginError")
                                }
                            }
                    },
                    isLoading = isLoadingLogin,
                    loginErrorMessage = loginError
                )
            }

            is Screen.Chat -> {
                ChatScreen(
                    currentUserId = currentUserId!!,
                    onSignOut = {
                        Log.d("AuthGate_SignOut", "Sign Out clicked for user $currentUserId.")
                        goOffline()
                        auth.signOut()
                    },
                    // ****** ADD THE onInitiateCall PARAMETER HERE ******
                    onInitiateCall = { partnerId, partnerName -> // Assuming ChatScreen provides these
                        Log.d("AuthGate_Call", "Initiating call with $partnerName (ID: $partnerId)")
                        // You'll likely need a channel name. For simplicity, let's derive one.
                        // In a real app, this might come from a shared backend or be agreed upon.
                        val channelName = generateChannelName(currentUserId!!, partnerId)

                        askForMicrophonePermission {
                            // This block is executed if permission is granted (or already granted)
                            Log.d("AuthGate_Call", "Microphone permission granted. Navigating to CallScreen.")
                            currentScreen = Screen.Call(partnerName = partnerName, channelName = channelName)
                        }
                    }
                )
            }


            is Screen.Call -> {
                CallScreen(
                    partnerName = screen.partnerName,
                    onEndCallClicked = {
                        // TODO: Add Agora leaveChannel logic here
                        currentScreen = Screen.Chat
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity_Lifecycle", "onStart CALLED")
        auth.currentUser?.uid?.let { userId ->
            if (userPresenceListener == null) {
                Log.d("MainActivity_Lifecycle", "User $userId is signed in onStart. Setting up online presence.")
                setupOnlinePresence(userId)
            } else {
                Log.d("MainActivity_Lifecycle", "User $userId is signed in onStart. Presence listener already active.")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity_Lifecycle", "onStop CALLED")
        if (!isChangingConfigurations) {
            Log.d("MainActivity_Lifecycle", "App stopped. Relying on onDisconnect or explicit sign-out for presence.")
        } else {
            Log.d("MainActivity_Lifecycle", "App stopped due to configuration change. Presence handling continues.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity_Lifecycle", "onDestroy CALLED")

        // Release sound player resources
        SoundPlayer.release()
        // TODO: Later, we will add AgoraEngine.destroy() here

        if (isFinishing) {
            auth.currentUser?.uid?.let {
                Log.d("MainActivity_Lifecycle", "Activity is finishing. Calling goOffline for $it.")
                goOffline()
            }
        }
    }
}