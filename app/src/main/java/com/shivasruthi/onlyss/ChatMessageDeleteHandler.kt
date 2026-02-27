package com.shivasruthi.onlyss

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.google.firebase.database.DatabaseReference

private const val LOG_TAG_DELETE_HANDLER = "MsgDeleteHandler" // Local log tag

// Firebase update logic for "Delete for Me"
fun executeDeleteForMeInFirebase(
    messageToDelete: ChatMessage, // Assumes ChatMessage is defined in com.shivasruthi.onlyss
    chatRoomId: String,
    currentUserId: String,
    chatsRefGlobal: DatabaseReference, // Pass the root 'chats' reference
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (chatRoomId.isBlank() || messageToDelete.id.isBlank() || currentUserId.isBlank()) {
        Log.e(LOG_TAG_DELETE_HANDLER, "Cannot delete: Invalid params. ChatID:'$chatRoomId', MsgID:'${messageToDelete.id}'")
        Toast.makeText(context, "Error deleting message: Invalid data.", Toast.LENGTH_LONG).show()
        onFailure(IllegalStateException("Invalid parameters for deletion operation."))
        return
    }

    Log.d(LOG_TAG_DELETE_HANDLER, "Marking msg '${messageToDelete.id}' as deletedFor '$currentUserId' in chat '$chatRoomId'")
    chatsRefGlobal.child(chatRoomId) // Use the passed root chatsRef
        .child("messages")
        .child(messageToDelete.id)
        .child("deletedFor")
        .child(currentUserId)
        .setValue(true)
        .addOnSuccessListener {
            Log.d(LOG_TAG_DELETE_HANDLER, "Msg '${messageToDelete.id}' marked deleted for $currentUserId.")
            Toast.makeText(context, "Message deleted for you.", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e(LOG_TAG_DELETE_HANDLER, "Failed to mark msg '${messageToDelete.id}' deleted for $currentUserId.", exception)
            Toast.makeText(context, "Failed to delete message: ${exception.message}", Toast.LENGTH_LONG).show()
            onFailure(exception)
        }
}

// Confirmation Dialog Composable
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
    // onDeleteMessageClicked: (ChatMessage) -> Unit // Removed
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Message") },
        text = { Text("Are you sure you want to delete this message for yourself? Others will still see it.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete for Me") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}