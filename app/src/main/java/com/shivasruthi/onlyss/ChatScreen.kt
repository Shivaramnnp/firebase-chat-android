package com.shivasruthi.onlyss

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shivasruthi.onlyss.ui.theme.OnlySSTheme
import com.shivasruthi.onlyss.ui.theme.ShivaBubbleColor
import com.shivasruthi.onlyss.ui.theme.SruthiBubbleColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.compose.material.icons.filled.Call


// Define LOG_TAG_CHAT if you haven't elsewhere
const val LOG_TAG_CHAT = "ChatScreen"
data class UserPresence(
    val isOnline: Boolean = false,
    val lastOnline: Long? = null
)

//  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
const val UID_USER_ONE_FROM_FIREBASE_CONSOLE = "7DAP8B2P2Tfxq5r46Y4JnvmK0E23"
const val UID_USER_TWO_FROM_FIREBASE_CONSOLE = "5OHYsqJ1laOS1YPF29nva0ErdPL2"
// At the top of your ChatScreen.kt or in a relevant constants file
const val UNKNOWN_USER_ID_FALLBACK_FINAL = "UNKNOWN_OTHER_USER"
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

typealias DR = DatabaseReference // Alias for DatabaseReference



@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    onSignOut: () -> Unit,
    onInitiateCall: (partnerName: String, channelName: String) -> Unit
) {
    var messageText by rememberSaveable { mutableStateOf("") }
    val messagesList = remember { mutableStateListOf<ChatMessage>() }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var messageMenuState by remember { mutableStateOf(MessageMenuState()) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var otherUserPresence by remember { mutableStateOf<UserPresence?>(UserPresence(isOnline = false)) } // Default to offline
    val localContext = LocalContext.current
    var errorStateMessage by remember { mutableStateOf<String?>(null) }

    val otherUserUid = remember(currentUserId) {
        val uid = when {
            currentUserId == UID_USER_ONE_FROM_FIREBASE_CONSOLE -> UID_USER_TWO_FROM_FIREBASE_CONSOLE
            currentUserId == UID_USER_TWO_FROM_FIREBASE_CONSOLE -> UID_USER_ONE_FROM_FIREBASE_CONSOLE
            else -> "UNKNOWN_OTHER_USER"
        }
        if (uid == "UNKNOWN_OTHER_USER") Log.e("ChatUID", "Mismatch for: $currentUserId")
        uid
    }

    var otherUserDisplayName by remember { mutableStateOf<String?>(null) }
    var chatRoomId by remember { mutableStateOf<String?>(null) }
    var initialDataLoading by remember { mutableStateOf(true) }
    var partnerIsOnline by remember { mutableStateOf(false) }
    var partnerLastSeen by remember { mutableStateOf<Long?>(null) }

    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val statusInsets = WindowInsets.statusBars
    val navInsets = WindowInsets.navigationBars

    LaunchedEffect(imeInsets) { val p = imeInsets.getBottom(density); Log.d("IME_LOG", "IME Dp: ${with(density){p.toDp()}}") }
    LaunchedEffect(statusInsets) { val p = statusInsets.getTop(density); Log.d("STATUS_LOG", "Status Dp: ${with(density){p.toDp()}}") }
    LaunchedEffect(navInsets) { val p = navInsets.getBottom(density); Log.d("NAV_LOG", "Nav Dp: ${with(density){p.toDp()}}") }


    val database = Firebase.database
    val usersRef = database.getReference("users")
    val chatsRef = database.getReference("chats")
    val statusRef = database.getReference("status")

    LaunchedEffect(key1 = otherUserUid, key2 = currentUserId) {
        initialDataLoading = true
        if (otherUserUid != "UNKNOWN_OTHER_USER") {
            usersRef.child(otherUserUid).child("displayName").get()
                .addOnSuccessListener { s -> val n=s.getValue(String::class.java); otherUserDisplayName=n?:"Partner"; if(n==null)Log.w("CSD","Null name for $otherUserUid"); chatRoomId=generateChatRoomId(currentUserId,otherUserUid); initialDataLoading=false }
                .addOnFailureListener { e -> Log.e("CSD","Fail name $otherUserUid",e); otherUserDisplayName="Partner(Err)"; chatRoomId=generateChatRoomId(currentUserId,otherUserUid); initialDataLoading=false }
        } else { otherUserDisplayName="Err: Unknown Partner"; chatRoomId=null; initialDataLoading=false }
    }
// LaunchedEffect to observe the other user's status
    // DisposableEffect to observe the other user's status
    // DisposableEffect to observe the other user's status
    DisposableEffect(key1 = otherUserUid) {
        if (otherUserUid != "UNKNOWN_OTHER_USER") {
            val otherUserStatusRef = statusRef.child(otherUserUid)
            val presenceListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val presence = snapshot.getValue(UserPresence::class.java)
                    otherUserPresence = presence // <--- otherUserPresence is set here
                    Log.d(LOG_TAG_CHAT, "Other user ($otherUserUid) presence updated: $presence")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(LOG_TAG_CHAT, "Failed to read other user ($otherUserUid) presence", error.toException())
                    otherUserPresence = UserPresence(isOnline = false) // Assumes offline on error, but NOT null
                }
            }
            otherUserStatusRef.addValueEventListener(presenceListener)

            onDispose {
                otherUserStatusRef.removeEventListener(presenceListener)
                Log.d(LOG_TAG_CHAT, "Removed presence listener for $otherUserUid")
            }
        } else {
            otherUserPresence = UserPresence(isOnline = false) // Default for unknown user, NOT null
            onDispose { /* No listener to remove */ }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = chatRoomId, key2 = lifecycleOwner) {
        if (chatRoomId == null || chatRoomId!!.contains("UNKNOWN_OTHER_USER")) {
            Log.d(LOG_TAG_CHAT, "DisposableEffect: chatRoomId is null or invalid. No listener attached.")
            onDispose { }
        } else {
            val currentValidChatRoomId = chatRoomId!!
            val messagesDbPath = chatsRef.child(currentValidChatRoomId).child("messages")

            val listener = object : ValueEventListener {
                var isDataInitiallyLoadedByThisListener by mutableStateOf(false)
                val playedSoundForMessageIdsInThisSession = mutableSetOf<String>()

                override fun onDataChange(snapshot: DataSnapshot) {
                    val newMessages = mutableListOf<ChatMessage>()
                    val toDeliver = mutableMapOf<String, DR>()
                    val toRead = mutableMapOf<String, DR>()
                    val isChatScreenResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                    var playReceivedSoundForThisSnapshot = false

                    Log.d(LOG_TAG_CHAT, "onDataChange triggered. InitialLoad: $isDataInitiallyLoadedByThisListener. Children: ${snapshot.childrenCount}")

                    for (msgSnapshot in snapshot.children) {
                        msgSnapshot.getValue(ChatMessage::class.java)?.let { msg ->
                            val msgWithId = msg.copy(id = msgSnapshot.key ?: UUID.randomUUID().toString())
                            newMessages.add(msgWithId)

                            if (msgWithId.senderId != currentUserId) {
                                if (msgWithId.status == "sent_to_server") { toDeliver[msgWithId.id] = msgSnapshot.ref }
                                if ((msgWithId.status == "sent_to_server" || msgWithId.status == "delivered") && isChatScreenResumed) { toRead[msgWithId.id] = msgSnapshot.ref }

                                if (isDataInitiallyLoadedByThisListener && !playedSoundForMessageIdsInThisSession.contains(msgWithId.id)) {
                                    playReceivedSoundForThisSnapshot = true
                                    playedSoundForMessageIdsInThisSession.add(msgWithId.id)
                                    Log.d(LOG_TAG_CHAT, "New received message (${msgWithId.id}) marked for sound.")
                                }
                            }
                        }
                    }

                    val previousMessageListSize = messagesList.size
                    messagesList.clear()
                    messagesList.addAll(newMessages.sortedBy { it.timestamp as? Long ?: 0L })

                    if (playReceivedSoundForThisSnapshot) {
                        SoundPlayer.playSoundFromAssets(localContext, "message_received.mp3")
                        Log.d(LOG_TAG_CHAT, "Played message_received.mp3")
                    }

                    if (!isDataInitiallyLoadedByThisListener && newMessages.isNotEmpty()) {
                        isDataInitiallyLoadedByThisListener = true
                        newMessages.forEach { playedSoundForMessageIdsInThisSession.add(it.id) }
                        Log.d(LOG_TAG_CHAT, "Listener's initial data load complete. ${newMessages.size} message IDs added to sound session set.")
                    }

                    if (toDeliver.isNotEmpty()) { coroutineScope.launch(Dispatchers.IO){ toDeliver.forEach { (_,r)->r.child("status").setValue("delivered") } } }
                    if (toRead.isNotEmpty()) { coroutineScope.launch(Dispatchers.IO){ toRead.forEach { (_,r)->r.child("status").setValue("read") } } }

                    if (messagesList.isNotEmpty() &&
                        (!lazyListState.isScrollInProgress && (playReceivedSoundForThisSnapshot || messagesList.size > previousMessageListSize || lazyListState.firstVisibleItemIndex < 3))
                    ) {
                        coroutineScope.launch{ lazyListState.animateScrollToItem(0) }
                    }

                    if (snapshot.exists() && errorStateMessage?.startsWith("Database error") == true) {
                        errorStateMessage = null
                    }
                }
                override fun onCancelled(e: DatabaseError){ Log.w(LOG_TAG_CHAT,"DB Error for $currentValidChatRoomId",e.toException()); errorStateMessage="Database error: ${e.message}" }
            }
            messagesDbPath.addValueEventListener(listener); Log.d(LOG_TAG_CHAT,"Attached ValueEventListener to $currentValidChatRoomId")
            onDispose { messagesDbPath.removeEventListener(listener); Log.d(LOG_TAG_CHAT,"Removed ValueEventListener from $currentValidChatRoomId") }
        }

    }

    fun sendMessageOrUpdate() {
        val currentValidChatRoomId = chatRoomId
        if(messageText.isBlank()||currentValidChatRoomId==null||currentValidChatRoomId.contains("UNKNOWN_OTHER_USER")){if(editingMessage!=null&&messageText.isBlank())Toast.makeText(localContext,"Edit empty",Toast.LENGTH_SHORT).show();return}
        val content=messageText.trim()
        if(editingMessage!=null){
            val msgToEdit = editingMessage!!
            val ref=chatsRef.child(currentValidChatRoomId).child("messages").child(msgToEdit.id);val upd=mapOf("text" to content,"isEdited" to true,"timestamp" to ServerValue.TIMESTAMP)
            ref.updateChildren(upd).addOnSuccessListener{editingMessage=null;messageText=""}.addOnFailureListener{Toast.makeText(localContext,"Edit Fail",Toast.LENGTH_SHORT).show()}
        }else{
            val newMsgRef=chatsRef.child(currentValidChatRoomId).child("messages").push();val newMsgKey=newMsgRef.key?: UUID.randomUUID().toString();val newChatMessage=ChatMessage(text=content,senderId=currentUserId,timestamp=ServerValue.TIMESTAMP,isEdited=false,status="sending")
            newMsgRef.setValue(newChatMessage)
                .addOnSuccessListener{
                    newMsgRef.child("status").setValue("sent_to_server").addOnFailureListener{e->Log.w(LOG_TAG_CHAT,"Failed to update status for $newMsgKey",e)}
                    SoundPlayer.playSoundFromAssets(localContext, "message_sent.mp3")
                    Log.d(LOG_TAG_CHAT, "Played message_sent.mp3 for $newMsgKey")
                }
                .addOnFailureListener{e->Log.e(LOG_TAG_CHAT,"Failed to send message $newMsgKey",e)}
            messageText=""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column { // Use a Column to display name and status on separate lines
                        val titleText = when {
                            // Prioritize error state for name if UID is known but fetch failed
                            otherUserUid != "UNKNOWN_OTHER_USER" && otherUserDisplayName?.contains("(Err)") == true -> "Error Loading Partner"
                            // Generic loading if display name is null AND initial data loading is true for a known user
                            initialDataLoading && otherUserDisplayName == null && otherUserUid != "UNKNOWN_OTHER_USER" -> "💕 Loading Partner... 💕"
                            // If display name is available
                            otherUserDisplayName != null -> "💕Chating With $otherUserDisplayName 💕"
                            // If otherUserUid is UNKNOWN
                            otherUserUid == "UNKNOWN_OTHER_USER" -> "💕 Unknown Partner 💕"
                            // Fallback if none of the above (should ideally be covered)
                            else -> "💕 Chat 💕"
                        }
                        Text(
                            text = titleText,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium // Or your preferred style
                        )

                        // Display Online/Offline Status SUB-TEXT
                        if (otherUserUid != "UNKNOWN_OTHER_USER" && !initialDataLoading) {
                            val presenceText = when {
                                otherUserPresence == null -> "Checking Status... " // <--- THIS IS BEING DISPLAYED
                                otherUserPresence!!.isOnline -> "Online"
                                else -> {
                                    val lastSeen = otherUserPresence!!.lastOnline?.let {
                                        "Last seen: ${formatLastSeen(it)}"
                                    } ?: "Offline"
                                    lastSeen
                                }
                            }
                            Text(
                                text = presenceText,
                                style = MaterialTheme.typography.bodySmall, // Smaller text for status
                                color = if (otherUserPresence?.isOnline == true) Color(0xFF4CAF50) /* Green */ else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Dimmed for offline
                            )
                        }
                    }
                },
                actions = {
                    if (chatRoomId != null && !chatRoomId!!.contains(UNKNOWN_USER_ID_FALLBACK_FINAL)) {
                        IconButton(onClick = {
                            onInitiateCall(otherUserDisplayName ?: "Partner", chatRoomId!!)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Start Voice Call")
                        }
                    }

                    TextButton(onClick = onSignOut) { Text("Sign Out") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // ... rest of your Scaffold
        bottomBar = {
            MessageInputRow(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendMessage = ::sendMessageOrUpdate,
                isEditing = editingMessage != null,
                onCancelEdit = { editingMessage = null; messageText = "" },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), Color(0xFFE6E6FA))))) {
            if (chatRoomId == null || (initialDataLoading && otherUserUid != "UNKNOWN_OTHER_USER")) {
                Box(Modifier.fillMaxSize(),Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){if(otherUserUid=="UNKNOWN_OTHER_USER"&&!initialDataLoading)Text("Err: Partner?",color=MaterialTheme.colorScheme.error)else{CircularProgressIndicator();Spacer(Modifier.height(8.dp));Text(if(initialDataLoading)"Loading..." else "Connecting...")}}}
            } else {
                LazyColumn(state=lazyListState,modifier=Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .imePadding(),reverseLayout=true,verticalArrangement=Arrangement.spacedBy(8.dp,Alignment.Bottom)) {
                    items(items=messagesList.asReversed(),key={it.id}){msgItem->MessageBubble(msgItem,msgItem.senderId==currentUserId,currentUserId,onLongPress={selMsg,_->if(selMsg.senderId==currentUserId)messageMenuState=MessageMenuState(true,selMsg)})}
                }
                // TODO: Display errorStateMessage somewhere in the UI if needed

                MessageOptionsMenu(
                    menuState = messageMenuState,
                    currentUserId = currentUserId,
                    onDismiss = { messageMenuState = MessageMenuState() },
                    onEditMessageClicked = { msg ->
                        editingMessage = msg
                        messageText = msg.text
                        messageMenuState = MessageMenuState()
                        Toast.makeText(localContext, "Editing...", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteMessageClicked = { msg ->
                        Toast.makeText(localContext, "Delete action for: ${msg.text}", Toast.LENGTH_SHORT).show()
                        messageMenuState = MessageMenuState()
                    },
                    onReactMessageClicked = { chatMessage, reaction ->
                        updateReactionOnFirebase(
                            chatMessage,
                            reaction,
                            currentUserId,
                            true,
                            chatRoomId
                        )
                        messageMenuState = MessageMenuState()
                    }
                )
            }
        }
    }
}
fun formatLastSeen(timestamp: Long?): String {
    if (timestamp == null) return "a while ago" // Fallback
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 1 -> SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(timestamp))
        days == 1L -> "yesterday at ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))}"
        hours > 0 -> "$hours hr${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        else -> "just now"
    }
}
fun updateReactionOnFirebase(
    chatMessage: ChatMessage,
    emoji: String,
    userId: String,
    toAdd: Boolean,
    chatRoomId: String?
) {
    if (chatRoomId == null || chatRoomId.contains("UNKNOWN_OTHER_USER")) {
        Log.e("Reaction", "Cannot update reaction, invalid chatRoomId: $chatRoomId")
        return
    }
    val messageRef = Firebase.database.getReference("chats/$chatRoomId/messages/${chatMessage.id}")

    // FIX FOR SMART CAST (using local variable)
    val reactionsFromMessage = chatMessage.reactions
    val currentReactions = reactionsFromMessage?.toMutableMap() ?: mutableMapOf()

    val usersForEmoji = currentReactions[emoji]?.toMutableList() ?: mutableListOf()

    val userAlreadyReactedWithEmoji = usersForEmoji.contains(userId)

    if (toAdd) {
        if (userAlreadyReactedWithEmoji) {
            usersForEmoji.remove(userId)
        } else {
            usersForEmoji.add(userId)
        }
    } else {
        usersForEmoji.remove(userId)
    }

    if (usersForEmoji.isEmpty()) {
        currentReactions.remove(emoji)
    } else {
        currentReactions[emoji] = usersForEmoji
    }

    messageRef.child("reactions").setValue(currentReactions.ifEmpty { null })
        .addOnSuccessListener {
            Log.d("Reaction", "Reaction updated for ${chatMessage.id} with $emoji by $userId. New state: $usersForEmoji")
        }
        .addOnFailureListener { e -> Log.e("Reaction", "Failed to update reaction", e) }
}


fun generateChatRoomId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
}

@Composable
fun MessageInputRow(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEditing: Boolean,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(tonalElevation = 3.dp, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editing message...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    TextButton(onClick = onCancelEdit) { Text("Cancel", style = MaterialTheme.typography.labelMedium) }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 8.dp,
                        top = if (isEditing) 2.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    placeholder = { Text(if (isEditing) "Save your edit..." else "Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSendMessage, enabled = messageText.isNotBlank()) {
                    Icon(
                        imageVector = if (isEditing) Icons.Filled.Done else Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isEditing) "Save Edit" else "Send Message",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUserSender: Boolean,
    currentUserId: String,
    onLongPress: (message: ChatMessage, pressPosition: Offset) -> Unit
) {
    val bubbleColorToUse: Color = if (isCurrentUserSender) {
        if (currentUserId == UID_USER_ONE_FROM_FIREBASE_CONSOLE) ShivaBubbleColor else SruthiBubbleColor
    } else {
        if (message.senderId == UID_USER_ONE_FROM_FIREBASE_CONSOLE) ShivaBubbleColor else SruthiBubbleColor
    }

    val horizontalBubbleAlignment: Alignment.Horizontal = if (isCurrentUserSender) Alignment.End else Alignment.Start
    val columnOuterPadding: PaddingValues = if (isCurrentUserSender) PaddingValues(start = 56.dp) else PaddingValues(end = 56.dp)
    val bubbleShapeTarget: Shape = if (isCurrentUserSender) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    // FIX FOR SMART CAST (using local variable)
    val currentReactions = message.reactions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(columnOuterPadding),
        horizontalAlignment = horizontalBubbleAlignment
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShapeTarget)
                .background(color = bubbleColorToUse)
                .pointerInput(message) {
                    detectTapGestures(onLongPress = { offset ->
                        onLongPress(
                            message,
                            offset
                        )
                    })
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(message.text, color = Color.Black, style = MaterialTheme.typography.bodyLarge)

                if (!currentReactions.isNullOrEmpty()) { // Use the local variable
                    Spacer(Modifier.height(4.dp))
                    ReactionsDisplay(reactions = currentReactions) // Use the local variable
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (message.isEdited) Text("(edited) ", fontSize = 10.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                    Text(formatTimestamp(message.timestamp), fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.padding(end = 4.dp))
                    if (isCurrentUserSender) {
                        when (message.status) {
                            "sending" -> Text("⏳", fontSize = 10.sp)
                            "sent_to_server" -> Text("🤍", fontSize = 10.sp)
                            "delivered" -> Text("💕", fontSize = 10.sp)
                            "read" -> Text("❤️", fontSize = 10.sp)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionsDisplay(reactions: Map<String, List<String>>) {
    Row {
        reactions.forEach { (emoji, users) ->
            if (users.isNotEmpty()) {
                Text(
                    text = emoji,
                    modifier = Modifier.padding(end = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Any?): String {
    if (timestamp == null) return ""
    val longTs = when (timestamp) { is Long -> timestamp else -> return "..." }
    if (longTs <= 0) return "..."
    return try { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(longTs)) }
    catch (e: Exception) { Log.e("FormatTime", "Err formatting timestamp: $longTs", e); "..." }
}

@Preview(showBackground = true, name = "ChatScreen P1")
@Composable
fun PreviewCS1() { // Removed the immediate return with =
    OnlySSTheme {
        ChatScreen(
            currentUserId = UID_USER_ONE_FROM_FIREBASE_CONSOLE,
            onSignOut = {
                // This is a dummy function for the preview.
                // You can add a Log statement if you want to verify it's callable.
                Log.d("PreviewCS1", "onSignOut called in preview")
            },
            onInitiateCall = { partnerName, channelName ->
                // This is also a dummy function for the preview.
                Log.d("PreviewCS1", "onInitiateCall with $partnerName and $channelName in preview")
            }
        )
    }
}

@Preview(showBackground = true, name = "Bubble U1 P1")
@Composable
fun PreviewMB1() = OnlySSTheme {
    MessageBubble(
        ChatMessage(text = "Test from User 1 (Shiva)", senderId = UID_USER_ONE_FROM_FIREBASE_CONSOLE, timestamp = System.currentTimeMillis(), status = "read", isEdited = true, reactions = mapOf("👍" to listOf("user1"), "😂" to listOf("user1", "user2"))),
        true,
        UID_USER_ONE_FROM_FIREBASE_CONSOLE,
        onLongPress = { _, _ -> }
    )
}

@Preview(showBackground = true, name = "Bubble U2 P1")
@Composable
fun PreviewMB2() = OnlySSTheme {
    MessageBubble(
        ChatMessage(text = "Test from User 2 (Sruthi)", senderId = UID_USER_TWO_FROM_FIREBASE_CONSOLE, timestamp = System.currentTimeMillis(), status = "delivered"),
        false,
        UID_USER_ONE_FROM_FIREBASE_CONSOLE,
        onLongPress = { _, _ -> }
    )
}



