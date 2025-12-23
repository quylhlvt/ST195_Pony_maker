package com.example.basefragment.data.datalocal.di

import com.example.basefragment.data.datalocal.api.DataCustomApi
import com.example.basefragment.data.datalocal.manager.AppDataManager
import com.example.basefragment.utils.BASE_URL
import com.example.basefragment.utils.SET_TIME_OUT_API
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataRemoveModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(SET_TIME_OUT_API, TimeUnit.SECONDS)
                    .writeTimeout(SET_TIME_OUT_API, TimeUnit.SECONDS)
                    .readTimeout(SET_TIME_OUT_API, TimeUnit.SECONDS)
                    .connectionPool(ConnectionPool(0, 5, TimeUnit.MINUTES))
                    .addInterceptor(httpLoggingInterceptor)
                    .build()
            )
            .build()

    @Provides
    @Singleton
    fun provideCatalogueApi(retrofit: Retrofit): DataCustomApi =
        retrofit.create(DataCustomApi::class.java)

}