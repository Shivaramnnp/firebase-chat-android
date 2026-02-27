package com.shivasruthi.onlyss // Your package name

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

object SoundPlayer { // Using an object for a simple singleton-like utility

    private var mediaPlayer: MediaPlayer? = null

    fun playSoundFromAssets(context: Context, fileName: String) {
        try {
            // Consider if you always want to release here.
            // If another sound is playing, calling this will stop it.
            // For one-shot sounds triggered by distinct actions, this might be fine.
            release() // Release any previous instance

            mediaPlayer = MediaPlayer()
            val assetFileDescriptor = context.assets.openFd(fileName)
            mediaPlayer?.apply {
                setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
                assetFileDescriptor.close()
                prepareAsync() // Prepare asynchronously to not block UI thread
                setOnPreparedListener {
                    Log.d("SoundPlayer", "$fileName prepared, starting playback.")
                    start()
                }
                setOnCompletionListener {
                    Log.d("SoundPlayer", "$fileName playback completed.")
                    release() // Release after completion
                }
                // ... inside playSoundFromAssets function
                setOnErrorListener { _, what, extra -> // <-- Renamed 'mp' to '_'
                    Log.e("SoundPlayer", "MediaPlayer Error on $fileName: what: $what, extra: $extra")
                    release()
                    true // True if the error has been handled
                }
// ...
            }
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing sound $fileName from assets", e)
            release()
        }
    }

    // Changed from 'private fun releaseMediaPlayer()' to 'fun release()'
    fun release() { // Make this public by removing 'private' (and rename if desired)
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset() // Reset for reuse or before release
            it.release() // This is the actual MediaPlayer.release()
            Log.d("SoundPlayer", "MediaPlayer released via SoundPlayer.release().")
        }
        mediaPlayer = null
    }
}