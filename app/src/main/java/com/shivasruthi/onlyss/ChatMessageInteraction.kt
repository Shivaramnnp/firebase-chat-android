package com.shivasruthi.onlyss

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
 import androidx.compose.material.icons.outlined.FavoriteBorder // Not used in this version, can remove if not needed elsewhere
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
// import androidx.compose.ui.graphics.Color // Import if you add Color tint
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

// Assuming ChatMessage is defined in another file in the same package or imported
// e.g., data class ChatMessage(
//    val id: String = "",
//    val text: String = "",
//    val senderId: String = "",
//    val timestamp: Any? = null,
//    val status: String? = null,
//    val isEdited: Boolean? = false,
//    var reactions: Map<String, List<String>>? = null // Emoji -> List of UserIDs
// )

data class MessageMenuState(
    val showMenu: Boolean = false,
    val selectedMessage: ChatMessage? = null,
    val pressPosition: Offset = Offset.Zero // For positioning the menu if needed
)

val predefinedReactions = listOf("❤️", "👍", "😂", "😢", "😮", "🤔", "kk")

@Composable
fun MessageOptionsMenu(
    menuState: MessageMenuState,
    currentUserId: String,
    onDismiss: () -> Unit,
    onEditMessageClicked: (ChatMessage) -> Unit,
    onDeleteMessageClicked: (ChatMessage) -> Unit,
    onReactMessageClicked: (message: ChatMessage, reaction: String) -> Unit
) {
    // This state determines if we are showing the list of emojis to pick from.
    // It's reset if the selected message changes or the menu is dismissed.
    var showReactionPicker by remember(menuState.selectedMessage, menuState.showMenu) { mutableStateOf(false) }

    if (menuState.showMenu && menuState.selectedMessage != null) {
        val message = menuState.selectedMessage
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current

        DropdownMenu(
            expanded = menuState.showMenu,
            onDismissRequest = {
                showReactionPicker = false // Reset picker mode on dismiss
                onDismiss()
            }
        ) {
            // Section 1: Reaction Picker Mode
            if (showReactionPicker) {
                predefinedReactions.forEach { reactionEmoji ->
                    val currentUserHasReactedWithThisEmoji = message.reactions?.get(reactionEmoji)?.contains(currentUserId) == true

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Optional: Display an icon for specific reactions like "❤️"
                                if (reactionEmoji == "❤️") {
                                    Icon(
                                        imageVector = if (currentUserHasReactedWithThisEmoji) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, // Show filled if reacted
                                        contentDescription = reactionEmoji,
                                        tint = if (currentUserHasReactedWithThisEmoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(reactionEmoji)
                                if (currentUserHasReactedWithThisEmoji) {
                                    Spacer(Modifier.width(4.dp))
                                    Text("(remove)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        },
                        onClick = {
                            onReactMessageClicked(message, reactionEmoji)
                            showReactionPicker = false // Close picker after reaction
                            onDismiss() // Close the entire menu
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Cancel") }, // Cancel from reaction picker
                    onClick = {
                        showReactionPicker = false // Go back to main options
                        // Do not call onDismiss() here if you want to return to the main menu
                    }
                )
            }
            // Section 2: Main Menu Items (not in reaction picker mode)
            else {
                // Copy Text (available for all messages)
                DropdownMenuItem(
                    text = { Text("Copy Text") },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(message.text))
                        Toast.makeText(context, "Text Copied!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                // Edit and Delete (only for messages sent by the current user)
                if (message.senderId == currentUserId) {
                    DropdownMenuItem(
                        text = { Text("Edit Message") },
                        onClick = {
                            onEditMessageClicked(message)
                            // onDismiss() // Dismissal is usually handled by the calling screen after navigation or action
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete for Me") },
                        onClick = {
                            onDeleteMessageClicked(message)
                            // onDismiss() // Dismissal handled by calling screen
                        }
                    )
                }

                // React Option (ONLY for messages RECEIVED by the current user)
                if (message.senderId != currentUserId) {
                    val currentUserPrimaryReaction = message.reactions?.entries?.find { entry -> entry.value.contains(currentUserId) }?.key

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentUserPrimaryReaction != null) {
                                    Text(currentUserPrimaryReaction) // Show the emoji they reacted with
                                    Spacer(Modifier.width(4.dp))
                                    Text("(Change Reaction)")
                                } else {
                                    Text("React...")
                                }
                            }
                        },
                        onClick = { showReactionPicker = true } // Switch to reaction picker mode
                    )
                }
            }
        }
    }
}

// Ensure you have a ChatMessage data class defined, for example:
// Make sure this matches the structure used in your ChatScreen.kt and Firebase.
// data class ChatMessage(
//     val id: String = "",
//     val text: String = "",
//     val senderId: String = "",
//     val recipientId: String? = null, // If you have specific recipients
//     val timestamp: Any? = null, // Using Long or ServerValue.TIMESTAMP for Firebase
//     val status: String? = null, // e.g., "sent_to_server", "delivered", "read"
//     val isEdited: Boolean? = false,
//     var reactions: Map<String, List<String>>? = null // Key: Emoji (e.g., "❤️"), Value: List of User IDs who reacted with it
//     // Add any other fields your app uses, like mediaUrl, messageType, etc.
// )