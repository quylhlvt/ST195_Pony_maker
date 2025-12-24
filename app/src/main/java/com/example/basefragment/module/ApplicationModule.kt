package com.example.basefragment.module

import android.content.Context
import com.example.basefragment.data.callapi.ApiHelper
import com.example.basefragment.data.repository.ApiRepository
import com.example.basefragment.data.repository.RoomRepository
import com.example.basefragment.utils.SharedPreferenceUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {
    @Singleton
    @Provides
    fun providerSharedPreference(@ApplicationContext appContext: Context): SharedPreferenceUtils {
        return SharedPreferenceUtils.Companion.getInstance(appContext)
    }
    @Singleton
    @Provides
    fun providerApi(@ApplicationContext context: Context): ApiHelper {
        return ApiHelper(context)
    }
    @Singleton
    @Provides
    fun providerApiRepository(apiHelper: ApiHelper): ApiRepository {
        return ApiRepository(apiHelper)
    }
    @Singleton
    @Provides
    fun providerRepository(@ApplicationContext context: Context): RoomRepository {
        return RoomRepository(context)
    }
}