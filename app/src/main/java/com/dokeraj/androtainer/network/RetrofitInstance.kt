package com.dokeraj.androtainer.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private var retrofit: Retrofit? = null

    private val okClient = OkHttpClient().newBuilder().readTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .build()

    val retrofitInstance: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://dummywebsite.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okClient)
                    .build()
            }
            return retrofit
        }
}

object RetrofitBinaryInstance {
    private var retrofit: Retrofit? = null

    private val okClient = OkHttpClient().newBuilder().readTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    val retrofitInstance: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://dummywebsite.com")
                    .client(okClient)
                    .build()
            }
            return retrofit
        }
}