package com.example.basefragment.utils.music

import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.edit
import com.example.basefragment.R
import com.example.basefragment.core.helper.SharedPreferencesManager
import com.example.basefragment.core.helper.SharedPreferencesManager.isMusic
import com.example.basefragment.core.helper.SharedPreferencesManager.setMusic

object MusicLocal {
    private var music: MediaPlayer? = null
    var isInSplashOrTutorial = false
    var home = false


    // 2. Lưu trạng thái

    // 3. Chạy nhạc
    fun play(context: Context) {
        val savedStatus = isMusic()

        // Chỉ phát nếu user đã bật và đã vào MainActivity
        if (!savedStatus || !home) return

        if (music == null) {
            try {
                music = MediaPlayer.create(context.applicationContext, R.raw.theme)?.apply {
                    isLooping = true
                }
            } catch (e: Exception) {
                music = null
            }
        }

        try {
            if (music?.isPlaying == false) {
                music?.start()
            }
        } catch (e: Exception) {
        }
    }

    // 4. Tạm dừng nhạc
    fun pause() {
        try {
            if (music?.isPlaying == true) {
                music?.pause()
            }
        } catch (e: Exception) {
        }
    }

    // 5. Bật/tắt nhạc + lưu trạng thái
    fun toggle(context: Context, enable: Boolean) {
        setMusic( enable) // lưu ngay

        if (enable) play(context) else pause()
    }



    // Giải phóng khi cần
    fun release() {
        try {
            music?.apply {
                if (isPlaying) stop()
                release()
            }
            music = null
        } catch (e: Exception) {
        }
    }

}