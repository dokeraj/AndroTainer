package com.dokeraj.androtainer.globalvars

import android.app.Application
import com.dokeraj.androtainer.models.Credential
import com.dokeraj.androtainer.models.LogSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalApp : Application() {
    var credentials: MutableMap<String, Credential> = mutableMapOf()
    var currentUser: Credential? = null
    var logSettings: LogSettings? = null
}