package com.example.basefragment

import android.app.Application
//import com.lvt.ads.util.AdsApplication
//import com.lvt.ads.util.AppOpenManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@HiltAndroidApp                     // QUAN TRỌNG NHẤT – KHÔNG ĐƯỢC THIẾU
class MyApplication : Application()   {
    override fun onCreate() {
        super.onCreate()
//        AppOpenManager.getInstance().disableAppResumeWithActivity(MyApplication::class.java)

    }
//    override fun enableAdsResume(): Boolean {
//        return true
//    }
//
//    override fun getListTestDeviceId(): MutableList<String>? {
//        return null
//    }
//
//    override fun getResumeAdId(): String {
//        return getString(R.string.open_resume)
//    }
//
//    override fun buildDebug(): Boolean {
//        return true
//    }
}