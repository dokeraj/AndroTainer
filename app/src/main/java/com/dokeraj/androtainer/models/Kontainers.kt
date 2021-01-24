package com.dokeraj.androtainer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Kontainers(
    val containers: List<Kontainer>,
) : Parcelable
