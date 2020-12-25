package com.dokeraj.androtainer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PContainer(var id: String, var name: String, var status: String, var state: String) : Parcelable // todo:: add ID
