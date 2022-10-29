package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import retrofit2.Response
import retrofit2.http.*

interface KontainerRetrofit {
    @GET
    suspend fun listDockerContainers(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
        @Query("all") paramAll: Int,
    ): PContainersResponse

    @GET
    suspend fun listDockerContainersApiKey(
        @Header("X-API-Key") auth: String?,
        @Url fullPath: String,
        @Query("all") paramAll: Int,
    ): PContainersResponse

    @POST
    suspend fun startStopContainer(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
    ): Response<Unit>

    @POST
    suspend fun startStopContainerApiKey(
        @Header("X-API-Key") auth: String?,
        @Url fullPath: String,
    ): Response<Unit>



    @DELETE
    suspend fun deleteDockerContainer(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
        @Query("force") force: Boolean,
        @Query("v") deleteVolumes: Int,
    ): Response<Unit>

    @DELETE
    suspend fun deleteDockerContainerApiKey(
        @Header("X-API-Key") auth: String?,
        @Url fullPath: String,
        @Query("force") force: Boolean,
        @Query("v") deleteVolumes: Int,
    ): Response<Unit>

}