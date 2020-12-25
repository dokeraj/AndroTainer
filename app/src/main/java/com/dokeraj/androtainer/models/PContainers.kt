package com.dokeraj.androtainer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PContainers(
    val containers: List<PContainer>,
) : Parcelable
