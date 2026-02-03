package com.example.basefragment.core.helper

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Singleton


@Singleton
object SharedPreferencesManager {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: Editor
    lateinit var bundle: Bundle
    private const val SPLASH_SCREEN = "splash_screen"
    private const val LANGUAGE_SCREEN = "language_screen"
    private const val LANGUAGE_KEY = "language_key"
    private const val PERMISSION_SCREEN = "permission_screen"
    private const val PERMISSION_NOTIFICATION = "NOTIFICATION_KEY"
    private const val PERMISSION_STORAGE = "STORAGE_KEY"
    private const val PERMISSION_CAMERA = "CAMERA_KEY"
    private const val COUNT_BACK_KEY = "COUNT_BACK_KEY"
    private const val RATE_KEY = "RATE_KEY"
    private const val SOUND_KEY = "SOUND_KEY"
    private const val MUSIC_KEY = "MUSIC_KEY"
    private const val SCREEN_ROTATE_KEY = "SCREEN_ROTATE_KEY"
    private const val RATE_COUNT_KEY = "RATE_COUNT_KEY"
    fun isSplashScreen(): Boolean = getBooleanDataByKey(SPLASH_SCREEN)
    fun setSplashScreen(isSkipped: Boolean) {
        saveBooleanDataByKey(SPLASH_SCREEN, isSkipped)
    }
    fun isMusic(): Boolean = sharedPreferences.getBoolean(MUSIC_KEY, true)

    fun setMusic(isMusicOn: Boolean) {
        saveBooleanDataByKey(MUSIC_KEY, isMusicOn)
    }
    fun isLanuageScreen(): Boolean = getBooleanDataByKey(LANGUAGE_SCREEN)
    fun setLanuageScreen(isSkipped: Boolean=true) {
        saveBooleanDataByKey(LANGUAGE_SCREEN, isSkipped)
    }
    fun isSound(): Boolean = sharedPreferences.getBoolean(SOUND_KEY, true)

    fun setSound(isSoundStatus: Boolean) {
        saveBooleanDataByKey(SOUND_KEY, isSoundStatus)
    }
fun isRotate(): Boolean = getBooleanDataByKey(SCREEN_ROTATE_KEY)
    fun setRotate(isRotateStatus: Boolean) {
        saveBooleanDataByKey(SCREEN_ROTATE_KEY, isRotateStatus)
    }

    fun isPermissionScreen(): Boolean = getBooleanDataByKey(PERMISSION_SCREEN)
    fun setPermissionScreen(isSkipped: Boolean) {
        saveBooleanDataByKey(PERMISSION_SCREEN, isSkipped)
    }

    fun isPermissionNotiRequest(): Int = getIntDataByKey(PERMISSION_NOTIFICATION)
    fun setPermissionNotiRequest(isSkipped: Int) {
        saveIntDataByKey(PERMISSION_NOTIFICATION, isSkipped)
    }
    fun isPermissionStorRequest(): Int = getIntDataByKey(PERMISSION_STORAGE)
    fun setPermissionStorRequest(isSkipped: Int) {
        saveIntDataByKey(PERMISSION_STORAGE, isSkipped)
    }
    fun isPermissionCamRequest(): Int = getIntDataByKey(PERMISSION_CAMERA)
    fun setPermissionCamRequest(isSkipped: Int) {
        saveIntDataByKey(PERMISSION_CAMERA, isSkipped)
    }

    fun isRateRequest(): Boolean = getBooleanDataByKey(RATE_KEY)
    fun setRateRequest(isSkipped: Boolean) {
        saveBooleanDataByKey(RATE_KEY, isSkipped)
    }
 fun isRateCountRequest(): Int = getIntDataByKey(RATE_COUNT_KEY)
    fun setRateCountRequest(isSkipped: Int) {
        saveIntDataByKey(RATE_COUNT_KEY, isSkipped)
    }

    fun isBackRequest(): Int = getIntDataByKey(COUNT_BACK_KEY)
    fun setBackRequest(isSkipped: Int) {
        saveIntDataByKey(COUNT_BACK_KEY, isSkipped)
    }

    fun isLanguageKey(): String=getStringDataByKey(LANGUAGE_KEY)

    fun setLanguageKey(isSkipped: String) {
        saveStringDataByKey(LANGUAGE_KEY, isSkipped)
    }


    private fun getBooleanDataByKey(key: String?): Boolean =
        sharedPreferences.getBoolean(key, false)

    private fun saveBooleanDataByKey(key: String?, data: Boolean) {
        editor.putBoolean(key, data).apply()
    }

    private fun saveStringDataByKey(key: String?, data: String?) {
        editor.putString(key, data).apply()
    }

    private fun getStringDataByKey(key: String?): String =
        sharedPreferences.getString(key, "") ?: ""

    private fun saveIntDataByKey(key: String?, data: Int) {
        editor.putInt(key, data).apply()
    }

    private fun getIntDataByKey(key: String?): Int = sharedPreferences.getInt(key, 0)

}