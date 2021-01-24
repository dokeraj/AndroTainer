package com.dokeraj.androtainer.models.retrofit

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import kotlinx.android.parcel.Parcelize

data class PContainerResponse(
    @SerializedName("Id") val id: String,
    @SerializedName("Names") val names: List<String>,
    @SerializedName("Status") val status: String,
    @SerializedName("State") val state: String,
    @SerializedName("Created") val created: Long,
    @SerializedName("Image") val pulledImage: String,
    @SerializedName("Labels") val maintainerInfo: MaintainerInfo,
    @SerializedName("HostConfig") val hostConfig: HostConfig,
    @SerializedName("Mounts") val mounts: List<Mount>,
    @SerializedName("Ports") val ports: List<Port>,
)

data class HostConfig(@SerializedName("NetworkMode") var networkMode: String)

class Mount(
    @SerializedName("Source") var source: String,
    @SerializedName("Destination") var destination: String,
    @SerializedName("Type") var type: String,
)

class Port(
    @SerializedName("PrivatePort") var privatePort: String,
    @SerializedName("PublicPort") var publicPort: String?,
    @SerializedName("Type") var type: String,
)

class MaintainerInfo(
    @SerializedName("maintainer") var maintainer: String?,
    @SerializedName("url") var url: String?,
)