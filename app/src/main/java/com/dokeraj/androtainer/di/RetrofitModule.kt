package com.dokeraj.androtainer.di

import com.dokeraj.androtainer.Interfaces.KontainerRetrofit
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.models.retrofit.NetworkMapper
import com.dokeraj.androtainer.models.retrofit.PContainerResponse
import com.dokeraj.androtainer.network.RetrofitInstance
import com.dokeraj.androtainer.util.EntityMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Singleton
    @Provides
    fun provideGsonBuilder(): Gson {
        return GsonBuilder().create()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient():OkHttpClient {
        return OkHttpClient().newBuilder().readTimeout(45, TimeUnit.SECONDS)
            .connectTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder().baseUrl("http://dummywebsite.com")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
    }

    @Singleton
    @Provides
    fun provideKontainerService(retrofit: Retrofit.Builder): KontainerRetrofit {
        return retrofit.build().create(KontainerRetrofit::class.java)
    }

}