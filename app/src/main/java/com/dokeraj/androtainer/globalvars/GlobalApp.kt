package com.dokeraj.androtainer.globalvars

import android.app.Application
import com.dokeraj.androtainer.models.Credential

class GlobalApp : Application() {
    var credentials: MutableMap<String, Credential> = mutableMapOf()
    var currentUser: Credential? = null
}