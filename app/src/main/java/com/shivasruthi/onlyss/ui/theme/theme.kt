package com.shivasruthi.onlyss.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


object ChatColors {
    // Primary colors (Consider if these are distinct from MaterialTheme roles or if they should map to them)
    val CHAT_PRIMARY = Color(0xFF6750A4)
    val CHAT_PRIMARY_VARIANT = Color(0xFF7C4DFF) // Often maps to primaryContainer
    val CHAT_SECONDARY = Color(0xFF625B71)
    val CHAT_TERTIARY = Color(0xFF7D5260)

    // Chat-specific bubble colors
    val USER_BUBBLE_BACKGROUND = Color(0xFF6750A4) // Renamed for clarity
    val ON_USER_BUBBLE_CONTENT = Color.White      // Text/icon color for user bubble

    val OTHER_BUBBLE_BACKGROUND = Color(0xFFE7E0EC) // Renamed for clarity
    val ON_OTHER_BUBBLE_CONTENT = Color(0xFF1C1B1F) // Text/icon color for other bubble

    // Indicators
    val ONLINE_INDICATOR = Color(0xFF4CAF50)
    val TYPING_INDICATOR = Color(0xFF2196F3)

    // Message Status colors
    val MESSAGE_SENT = Color(0xFF9E9E9E)
    val MESSAGE_DELIVERED = Color(0xFF4CAF50)
    val MESSAGE_READ = Color(0xFF2196F3)
    val MESSAGE_FAILED = Color(0xFFF44336)

    // Background gradients (Consider if these are for full screen backgrounds or specific components)
    val CHAT_BACKGROUND_LIGHT_START = Color(0xFFF8F9FA)
    val CHAT_BACKGROUND_LIGHT_END = Color(0xFFE8F5E8)
    val CHAT_BACKGROUND_DARK_START = Color(0xFF121212)
    val CHAT_BACKGROUND_DARK_END = Color(0xFF1E1E1E)
}

// Enhanced dark color scheme for modern chat
private val DarkChatColorScheme = darkColorScheme(
    primary = ChatColors.CHAT_PRIMARY, // Or a more theme-aligned primary
    onPrimary = ChatColors.ON_USER_BUBBLE_CONTENT, // If CHAT_PRIMARY is used for user bubbles
    primaryContainer = ChatColors.CHAT_PRIMARY_VARIANT, // Typically a toned-down version of primary
    // onPrimaryContainer: Color, // Define if needed

    secondary = ChatColors.CHAT_SECONDARY,
    onSecondary = Color.White, // Or a specific "onSecondary"
    // secondaryContainer: Color,
    // onSecondaryContainer: Color,

    tertiary = ChatColors.CHAT_TERTIARY,
    onTertiary = Color.White, // Or a specific "onTertiary"
    // tertiaryContainer: Color,
    // onTertiaryContainer: Color,

    background = Color(0xFF121212), // Or ChatColors.CHAT_BACKGROUND_DARK_START for consistency
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E), // Or ChatColors.CHAT_BACKGROUND_DARK_END
    onSurface = Color(0xFFE6E1E5),

    surfaceVariant = ChatColors.OTHER_BUBBLE_BACKGROUND, // Using this for other user's bubble background
    onSurfaceVariant = ChatColors.ON_OTHER_BUBBLE_CONTENT, // Text on other user's bubble

    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB)
)

// Enhanced light color scheme for modern chat
private val LightChatColorScheme = lightColorScheme(
    primary = ChatColors.CHAT_PRIMARY,
    onPrimary = ChatColors.ON_USER_BUBBLE_CONTENT,
    primaryContainer = Color(0xFFEADDFF), // Consider if CHAT_PRIMARY_VARIANT should be used here
    onPrimaryContainer = Color(0xFF21005D),

    secondary = ChatColors.CHAT_SECONDARY,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),

    tertiary = ChatColors.CHAT_TERTIARY,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    background = Color(0xFFFFFFFF), // Or ChatColors.CHAT_BACKGROUND_LIGHT_START
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF8F9FA), // Or ChatColors.CHAT_BACKGROUND_LIGHT_END
    onSurface = Color(0xFF1C1B1F),

    surfaceVariant = ChatColors.OTHER_BUBBLE_BACKGROUND,
    onSurfaceVariant = ChatColors.ON_OTHER_BUBBLE_CONTENT,

    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerHighest = Color(0xFFE6E0E9),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000),

    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun OnlySSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Consider making this false by default if you prefer your custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkChatColorScheme
        else -> LightChatColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let { // Use 'let' for safer nullable handling
                Log.d("OnlySSTheme", "Applying window settings for edge-to-edge.")

                WindowCompat.setDecorFitsSystemWindows(it, false) // Enable edge-to-edge

                // Make status and navigation bars transparent to allow content to draw behind them
                it.statusBarColor = Color.Transparent.toArgb()
                it.navigationBarColor = Color.Transparent.toArgb()

                // Control system bar icon colors (light/dark)
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController?.isAppearanceLightStatusBars = !darkTheme
                insetsController?.isAppearanceLightNavigationBars = !darkTheme

                Log.d("OnlySSTheme", "Window settings applied successfully.")
            } ?: run {
                Log.e("OnlySSTheme", "Window not available. Cannot apply window settings.")
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Ensure Typography is defined and imported
        content = content
    )
}

/**
 * Provides a pair of (backgroundColor, contentColor) for chat bubbles.
 *
 * @param isFromUser True if the bubble is for the current user, false otherwise.
 * @return Pair of (Color for bubble background, Color for text/icons on the bubble).
 */
@Composable
fun ChatBubbleColors(isFromUser: Boolean): Pair<Color, Color> {
    return if (isFromUser) {
        // Using explicitly defined user bubble colors from ChatColors
        ChatColors.USER_BUBBLE_BACKGROUND to ChatColors.ON_USER_BUBBLE_CONTENT
    } else {
        // Using explicitly defined other bubble colors from ChatColors
        // Or, you could use MaterialTheme roles if they are set up for this:
        // MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        ChatColors.OTHER_BUBBLE_BACKGROUND to ChatColors.ON_OTHER_BUBBLE_CONTENT
    }
}

/**
 * Provides the color for the online presence indicator.
 */
@Composable
fun OnlineIndicatorColor(): Color = ChatColors.ONLINE_INDICATOR

/**
 * Provides the color for the typing indicator.
 */
@Composable
fun TypingIndicatorColor(): Color = ChatColors.TYPING_INDICATOR


@Composable
fun MessageStatusColor(status: String): Color {
    return when (status.lowercase()) { // Using lowercase for case-insensitive matching
        "sent" -> ChatColors.MESSAGE_SENT
        "delivered" -> ChatColors.MESSAGE_DELIVERED
        "read" -> ChatColors.MESSAGE_READ
        "failed" -> ChatColors.MESSAGE_FAILED
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Fallback for unknown status
    }
}