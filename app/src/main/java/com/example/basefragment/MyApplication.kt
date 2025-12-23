package com.example.basefragment

import android.app.Application
import android.content.SharedPreferences
import com.example.basefragment.core.helper.SharedPreferencesManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp                     // QUAN TRỌNG NHẤT – KHÔNG ĐƯỢC THIẾU
class MyApplication : Application(){
}