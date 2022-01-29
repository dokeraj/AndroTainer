package com.dokeraj.androtainer.globalvars

import android.app.Application
import com.dokeraj.androtainer.models.Credential
import com.dokeraj.androtainer.models.LogSettings
import com.dokeraj.androtainer.models.retrofit.AppSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GlobalApp : Application() {
    var credentials: MutableMap<String, Credential> = mutableMapOf()
    var currentUser: Credential? = null
    var logSettings: LogSettings? = null
    var appSettings: AppSettings? = null
}