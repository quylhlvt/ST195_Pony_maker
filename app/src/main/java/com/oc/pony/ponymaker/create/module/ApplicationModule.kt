package com.oc.pony.ponymaker.create.module

import android.content.Context
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
    fun providerSharedPreference(@ApplicationContext appContext: Context): com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils {
        return _root_ide_package_.com.oc.pony.ponymaker.create.utils.SharedPreferenceUtils.Companion.getInstance(appContext)
    }
    @Singleton
    @Provides
    fun providerApi(@ApplicationContext context: Context): com.oc.pony.ponymaker.create.data.callapi.ApiHelper {
        return _root_ide_package_.com.oc.pony.ponymaker.create.data.callapi.ApiHelper(context)
    }
    @Singleton
    @Provides
    fun providerApiRepository(apiHelper: com.oc.pony.ponymaker.create.data.callapi.ApiHelper): com.oc.pony.ponymaker.create.data.repository.ApiRepository {
        return _root_ide_package_.com.oc.pony.ponymaker.create.data.repository.ApiRepository(apiHelper)
    }
    @Singleton
    @Provides
    fun providerRepository(@ApplicationContext context: Context): com.oc.pony.ponymaker.create.data.repository.RoomRepository {
        return _root_ide_package_.com.oc.pony.ponymaker.create.data.repository.RoomRepository(context)
    }
}