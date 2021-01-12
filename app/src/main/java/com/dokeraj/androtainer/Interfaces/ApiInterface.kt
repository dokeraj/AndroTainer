package com.dokeraj.androtainer.Interfaces

import com.dokeraj.androtainer.models.retrofit.Jwt
import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import com.dokeraj.androtainer.models.retrofit.UserCredentials
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @GET
    fun listDockerContainers(
        @Header("Authorization") jwt: String?,
        @Url fullPath: String,
        @Query("all") paramAll: Int,
    ): Call<PContainersResponse>

    //@Headers("Content-Type: application/json")
    @POST
    fun loginRequest(@Body userData: UserCredentials, @Url fullPath: String): Call<Jwt>

    @POST
    fun startStopContainer(@Header("Authorization") jwt: String?, @Url fullPath: String): Call<Unit>

    @DELETE
    fun removeDockerContainer(
        @Header("Authorization") jwt: String?,
        @Url fullPath: String,
        @Query("force") force: Boolean,
        @Query("v") deleteVolumes: Int,
    ): Call<Unit>

}