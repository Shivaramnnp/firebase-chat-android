package com.shivasruthi.onlyss

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue

data class ChatMessage(
    @get:Exclude var id: String = "",
    var text: String = "",
    val senderId: String = "",
    val timestamp: Any = ServerValue.TIMESTAMP,
    var status: String = "sending",
    var isEdited: Boolean = false,
    val deletedFor: Map<String, Boolean>? = null,
    // MODIFIED HERE:
    var reactions: Map<String, List<String>>? = null // Emoji -> List of UserIDs who reacted
) {
    // Make sure your secondary constructor aligns with any new default values or types
    constructor() : this(
        id = "",
        text = "",
        senderId = "",
        timestamp = 0L, // Default to Long for placeholder in constructor
        status = "sending",
        isEdited = false,
        deletedFor = null, // Add deletedFor
        reactions = null
    )
}