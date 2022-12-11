package com.dokeraj.androtainer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Kontainer(
    val id: String,
    val name: String,
    val status: String,
    val state: ContainerStateType,
    val created: Long,
    val pulledImage: String,
    val maintainerInfo: MaintainerInfo,
    val hostConfig: HostConfig,
    val mounts: List<Mount>,
    val ports: List<Port>,
): Parcelable


enum class ContainerStateType {
    RUNNING,
    EXITED,
    TRANSITIONING,
    ERRORED,
    CREATED,
    RESTARTING
}

enum class ContainerActionType {
    START, STOP
}

@Parcelize
data class HostConfig(val networkMode: String) : Parcelable

@Parcelize
class Mount(
    val source: String,
    val destination: String,
    val type: String,
) : Parcelable


@Parcelize
class Port(
    var privatePort: String,
    var publicPort: String?,
    var type: String,
) : Parcelable

@Parcelize
class MaintainerInfo(
    var maintainer: String?,
    var url: String?,
) : Parcelable