package com.dokeraj.androtainer.models.retrofit

import com.google.gson.annotations.SerializedName

data class PortainerEndpoint(
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("URL") val url: String,
    @SerializedName("Type") val type: Int,
)
