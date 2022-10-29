package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import retrofit2.Response
import retrofit2.http.*

interface KontainerRetrofit:MasterInter {
    @GET
    override suspend fun listDockerContainers(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
        @Query("all") paramAll: Int,
    ): PContainersResponse

    @POST
    override suspend fun startStopContainer(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
    ): Response<Unit>

    @DELETE
    override suspend fun deleteDockerContainer(
        @Header("Authorization") auth: String?,
        @Url fullPath: String,
        @Query("force") force: Boolean,
        @Query("v") deleteVolumes: Int,
    ): Response<Unit>

}