package com.dokeraj.androtainer.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PContainer(
    @SerializedName("Id") var id: String,
    @SerializedName("Names") var names: List<String>,
    var name: String = "",
    @SerializedName("Status") var status: String,
    @SerializedName("State") var state: ContainerStateType,
    @SerializedName("Created") var created: Long,
    @SerializedName("Image") var pulledImage: String,
    @SerializedName("Labels") val maintainerInfo: MaintainerInfo,
    @SerializedName("HostConfig") val hostConfig: HostConfig,
    @SerializedName("Mounts") val mounts: ArrayList<Mount>,
    @SerializedName("Ports") val ports: ArrayList<Port>,
) : Parcelable

object PContainerHelper {
    fun toListPContainer(pcResponse: PContainersResponse): List<PContainer> {
        return pcResponse.map { pcr ->
            pcr.copy(name = pcr.names[0].drop(1).trim().capitalize())
        }
    }
}

enum class ContainerStateType {
    @SerializedName("running")
    RUNNING,

    @SerializedName("exited")
    EXITED,
    TRANSITIONING,
    ERRORED
}

enum class ContainerActionType {
    START, STOP
}

@Parcelize
data class HostConfig(@SerializedName("NetworkMode") var networkMode: String) : Parcelable

@Parcelize
class Mount(
    @SerializedName("Source") var source: String,
    @SerializedName("Destination") var destination: String,
    @SerializedName("Type") var type: String,
) : Parcelable


@Parcelize
class Port(
    @SerializedName("PrivatePort") var privatePort: String,
    @SerializedName("PublicPort") var publicPort: String,
    @SerializedName("Type") var type: String,
) : Parcelable

@Parcelize
class MaintainerInfo(
    @SerializedName("maintainer") var maintainer: String?,
    @SerializedName("url") var url: String?,
) : Parcelable