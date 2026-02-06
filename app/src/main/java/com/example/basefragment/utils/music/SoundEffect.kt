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
    private var onSoundCompleteListener: ((Int) -> Unit)? = null

    private val activeStreams = mutableMapOf<Int, Int>()
    private val streamHandlers = mutableMapOf<Int, Runnable>()

    private const val DEFAULT_VOLUME = 1.0f

    // ... existing init() and setMediaVolumeToMax() ...
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
    }
    fun playClick(
        context: Context,
        @RawRes resId: Int = R.raw.click,
        checkSettings: Boolean = true,
        volume: Float = DEFAULT_VOLUME,
        onComplete: (() -> Unit)? = null
    ) {
        if (checkSettings && !SharedPreferencesManager.isSound()){
            onComplete?.invoke()
            return
        }

        if (soundPool == null) init(context)

        var soundId = soundCache[resId]

        if (soundId == null) {
            soundId = soundPool?.load(context, resId, 1) ?: return
            soundCache[resId] = soundId
            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == soundId) {
                    loadedSounds.add(sampleId)
                    val streamId = soundPool?.play(sampleId, volume, volume, 1, 0, 1f)
                    streamId?.let {
                        activeStreams[resId] = it
                        calculateSoundDuration(context, resId, onComplete)
                    }
                }
            }
        } else {
            if (loadedSounds.contains(soundId)) {
                val streamId = soundPool?.play(soundId, volume, volume, 1, 0, 1f)

                streamId?.let {
                    activeStreams[resId] = it
                    calculateSoundDuration(context, resId, onComplete)
                }
            }
        }
    }

    private fun calculateSoundDuration(
        context: Context,
        @RawRes resId: Int,
        onComplete: (() -> Unit)?
    ) {
        try {
            streamHandlers[resId]?.let {
                android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it)
            }

            val mediaPlayer = android.media.MediaPlayer.create(context, resId)
            val duration = mediaPlayer?.duration?.toLong() ?: 0L
            mediaPlayer?.release()

            if (duration > 0 && onComplete != null) {
                val runnable = Runnable {
                    onComplete.invoke()
                    activeStreams.remove(resId)
                    streamHandlers.remove(resId)
                }

                streamHandlers[resId] = runnable

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                    runnable,
                    duration
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete?.invoke()
            activeStreams.remove(resId)
            streamHandlers.remove(resId)
        }
    }

    // ✅ Stop và XÓA HẲN sound cụ thể
    fun removeSound(@RawRes resId: Int) {
        // Stop stream nếu đang chạy
        activeStreams[resId]?.let { streamId ->
            soundPool?.stop(streamId)
        }

        // Hủy handler callback
        streamHandlers[resId]?.let { runnable ->
            android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(runnable)
        }

        // ✅ Unload sound khỏi SoundPool
        soundCache[resId]?.let { soundId ->
            soundPool?.unload(soundId)
            loadedSounds.remove(soundId)
        }

        // ✅ Xóa khỏi cache
        soundCache.remove(resId)
        activeStreams.remove(resId)
        streamHandlers.remove(resId)
    }

    // ✅ Stop sound nhưng GIỮ trong cache (dùng lại được)
    fun stopSound(@RawRes resId: Int) {
        activeStreams[resId]?.let { streamId ->
            soundPool?.stop(streamId)
        }

        streamHandlers[resId]?.let { runnable ->
            android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(runnable)
        }

        activeStreams.remove(resId)
        streamHandlers.remove(resId)
    }

    // ✅ Stop tất cả nhưng GIỮ cache
    fun stopAllSounds() {
        activeStreams.forEach { (_, streamId) ->
            soundPool?.stop(streamId)
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        streamHandlers.forEach { (_, runnable) ->
            handler.removeCallbacks(runnable)
        }

        activeStreams.clear()
        streamHandlers.clear()
    }

    // ✅ Xóa HẲN tất cả sounds
    fun removeAllSounds() {
        stopAllSounds()

        // Unload tất cả sounds
        soundCache.values.forEach { soundId ->
            soundPool?.unload(soundId)
        }

        soundCache.clear()
        loadedSounds.clear()
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
                    val streamId = soundPool?.play(sampleId, volume, volume, 1, loop, rate)
                    streamId?.let {
                        activeStreams[resId] = it
                    }
                }
            }
        } else {
            if (loadedSounds.contains(soundId)) {
                val streamId = soundPool?.play(soundId, volume, volume, 1, loop, rate)
                streamId?.let {
                    activeStreams[resId] = it
                }
            }
        }
    }

    fun playClick1(context: Context) {
        playClick(context, R.raw.click, checkSettings = false, volume = DEFAULT_VOLUME)
    }

    fun release() {
        removeAllSounds() // ✅ Xóa hết trước khi release
        soundPool?.release()
        soundPool = null
        audioManager = null
    }
}