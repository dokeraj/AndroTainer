package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.Jwt
import com.dokeraj.androtainer.models.retrofit.PEndpointsResponse
import com.dokeraj.androtainer.models.retrofit.UserCredentials
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterfaceApiKey:MasterInter {
    @Headers("Content-Type: application/octet-stream")
    @GET
    fun getStatus(@Url fullPath: String,@Header("X-API-Key") auth: String): Call<ResponseBody>

    @Headers("Content-Type: application/octet-stream")
    @GET
    override fun getLog(
        @Url fullPath: String,
        @Header("X-API-Key") auth: String,
        @Query("since") since: Int,
        @Query("stderr") stderr: Int,
        @Query("stdout") stdout: Int,
        @Query("tail") tail: Int,
        @Query("timestamps") timestamps: Int,
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @GET
    override fun getEnpointId(
        @Url fullPath: String,
        @Header("X-API-Key") auth: String,
        @Query("limit") limit: Int,
        @Query("start") start: Int,
    ): Call<PEndpointsResponse>

}