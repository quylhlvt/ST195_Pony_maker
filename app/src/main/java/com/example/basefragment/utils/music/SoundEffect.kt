package com.example.basefragment.utils.music

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.basefragment.R
import com.example.basefragment.core.helper.SharedPreferencesManager

object SoundEffect {

    private var soundPool: SoundPool? = null
    private var soundCache = HashMap<Int, Int>()
    private var loadedSounds = mutableSetOf<Int>()
    private var audioManager: AudioManager? = null

    private const val DEFAULT_VOLUME = 1.0f

    fun init(context: Context) {
        if (soundPool != null) return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA) // Thử USAGE_MEDIA thay vì USAGE_GAME
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        // Tăng volume Media stream lên max khi phát sound effect
        setMediaVolumeToMax(context)
    }

    private fun setMediaVolumeToMax(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // Tăng lên 80-90% để không quá ồn
            val targetVolume = (maxVolume * 0.9).toInt()

            if (currentVolume < targetVolume) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    targetVolume,
                    0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick(
        context: Context,
        @RawRes resId: Int = R.raw.click,
        checkSettings: Boolean = true,
        volume: Float = DEFAULT_VOLUME
    ) {
        if (checkSettings && !SharedPreferencesManager.isSound()) return

        if (soundPool == null) init(context)

        var soundId = soundCache[resId]

        if (soundId == null) {
            soundId = soundPool?.load(context, resId, 1) ?: return
            soundCache[resId] = soundId
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == soundId) {
                    loadedSounds.add(sampleId)
                    soundPool?.play(sampleId, volume, volume, 1, 0, 1f)
                }
            }
        } else {
            if (loadedSounds.contains(soundId)) {
                soundPool?.play(soundId, volume, volume, 1, 0, 1f)
            }
        }
    }

    fun playSound(
        context: Context,
        @RawRes resId: Int,
        volume: Float = DEFAULT_VOLUME,
        loop: Int = 0,
        rate: Float = 1f,
        checkSettings: Boolean = true
    ) {
        if (checkSettings && !SharedPreferencesManager.isSound()) return

        if (soundPool == null) init(context)

        var soundId = soundCache[resId]

        if (soundId == null) {
            soundId = soundPool?.load(context, resId, 1) ?: return
            soundCache[resId] = soundId
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == soundId) {
                    loadedSounds.add(sampleId)
                    soundPool?.play(sampleId, volume, volume, 1, loop, rate)
                }
            }
        } else {
            if (loadedSounds.contains(soundId)) {
                soundPool?.play(soundId, volume, volume, 1, loop, rate)
            }
        }
    }

    fun playClick1(context: Context) {
        playClick(context, R.raw.click, checkSettings = false, volume = DEFAULT_VOLUME)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundCache.clear()
        loadedSounds.clear()
        audioManager = null
    }
}