package com.dokeraj.androtainer.interfaces

import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import com.dokeraj.androtainer.models.retrofit.PEndpointsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface MasterInter {
    @Headers("Content-Type: application/json")
    @GET
    abstract fun getEnpointId(fullPath: String, auth: String, limit: Int, start: Int): Call<PEndpointsResponse>

    @Headers("Content-Type: application/octet-stream")
    @GET
    abstract fun getLog(
        fullPath: String,
        auth: String,
        since: Int,
        stderr: Int,
        stdout: Int,
        tail: Int,
        timestamps: Int,
    ): Call<ResponseBody>
}