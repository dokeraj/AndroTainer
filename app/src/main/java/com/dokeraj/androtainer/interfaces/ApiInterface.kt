package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.Jwt
import com.dokeraj.androtainer.models.retrofit.UserCredentials
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    //@Headers("Content-Type: application/json")
    @POST
    fun loginRequest(@Body userData: UserCredentials, @Url fullPath: String): Call<Jwt>

}