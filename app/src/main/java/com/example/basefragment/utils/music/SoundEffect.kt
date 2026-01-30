package com.example.basefragment.utils.music

import android.content.Context
import android.media.SoundPool
import com.example.basefragment.R
import com.example.basefragment.core.helper.SharedPreferencesManager

object SoundEffect {

    private var soundPool: SoundPool? = null
    private var clickId = 0
    private var isLoaded = false

    fun init(context: Context) {
        if (soundPool != null) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isLoaded = true
        }
        clickId = soundPool!!.load(context, R.raw.click, 1)
    }

    fun playClick(context: Context) {
        if (!SharedPreferencesManager.isSound()) return

        if (soundPool == null) init(context)
        if (!isLoaded) return
        soundPool?.play(
            clickId,
            1f, 1f,
            1,
            0,
            1f
        )
    }
    fun playClick1(context: Context) {

        if (soundPool == null) init(context)
        if (!isLoaded) return
        soundPool?.play(
            clickId,
            1f, 1f,
            1,
            0,
            1f
        )
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false

    }
}
