package com.dokeraj.androtainer.globalvars

import android.app.Application

class GlobalApp:Application() {
    var url: String? = null
    var user: String? = null
    var pwd: String? = null
    var jwt: String? = null
    var jwtValidUntil: Long? = null
}