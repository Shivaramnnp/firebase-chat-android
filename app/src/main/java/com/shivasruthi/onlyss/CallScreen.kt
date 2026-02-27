package com.shivasruthi.onlyss

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallScreen(
    partnerName: String, // To display "Call with Shiva" or "Call with Sruthi"
    onEndCallClicked: () -> Unit
) {
    var isMicMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mute/Unmute Button
                IconButton(onClick = { isMicMuted = !isMicMuted }) {
                    Icon(
                        imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // End Call Button
                FloatingActionButton(
                    onClick = onEndCallClicked,
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(36.dp))
                }

                // Speakerphone Button
                IconButton(onClick = { isSpeakerOn = !isSpeakerOn }) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Toggle Speakerphone",
                        tint = if (isSpeakerOn) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "In call with",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = partnerName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                // TODO: Later, we will show call duration timer here
                // TODO: And the video streams will be displayed in this Box area
            }
        }
    }
}