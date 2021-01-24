package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import retrofit2.Response
import retrofit2.http.*

interface KontainerRetrofit {
    @GET
    suspend fun listDockerContainers(
        @Header("Authorization") jwt: String?,
        @Url fullPath: String,
        @Query("all") paramAll: Int,
    ): PContainersResponse

    @POST
    suspend fun startStopContainer(
        @Header("Authorization") jwt: String?,
        @Url fullPath: String,
    ): Response<Unit>

    @DELETE
    suspend fun deleteDockerContainer(
        @Header("Authorization") jwt: String?,
        @Url fullPath: String,
        @Query("force") force: Boolean,
        @Query("v") deleteVolumes: Int,
    ): Response<Unit>
}