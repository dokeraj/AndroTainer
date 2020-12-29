package com.dokeraj.androtainer

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.globalvars.PermaVals.JWT_NAME
import com.dokeraj.androtainer.globalvars.PermaVals.JWT_VALID_UNTIL
import com.dokeraj.androtainer.globalvars.PermaVals.PWD_NAME
import com.dokeraj.androtainer.globalvars.PermaVals.SP_NAME
import com.dokeraj.androtainer.globalvars.PermaVals.URL_NAME
import com.dokeraj.androtainer.globalvars.PermaVals.USR_NAME
import com.google.android.material.snackbar.Snackbar
import java.time.Instant


class MainActiviy : AppCompatActivity() {

    private var logoutMsg: String? = null
    fun getLogoutMsg() = logoutMsg
    fun setLogoutMsg(msg: String?) {
        logoutMsg = msg
    }

    fun showGenericSnack(
        context: Context,
        view: View,
        snackbarText: String,
        textColor: Int,
        snackBckColor: Int,
    ) {
        val snackbar = Snackbar.make(
            view,
            snackbarText,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView: View = snackbar.view
        val snackbarTextId: Int = R.id.snackbar_text

        val textView = snackbarView.findViewById<View>(snackbarTextId) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        //textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(ContextCompat.getColor(context, textColor))

        snackbarView.setBackgroundColor(ContextCompat.getColor(context, snackBckColor))

        snackbar.show()
    }


    private fun savePermaData(
        url: String,
        usr: String,
        pwd: String,
        jwt: String?,
        validUntil: Long?,
    ) {
        val sharedPrefs = this.getSharedPreferences(SP_NAME, MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        println("U PERMA SME SEGAxxxxxx")
        editor?.putString(URL_NAME, url)
        editor?.putString(USR_NAME, usr)
        editor?.putString(PWD_NAME, pwd)
        editor?.putString(JWT_NAME, jwt)
        if (validUntil != null) {
            editor?.putLong(JWT_VALID_UNTIL, validUntil)
        } else {
            editor?.putLong(JWT_VALID_UNTIL, 0L)
        }

        editor?.apply()
    }

    fun getPermaStringVal(paramName: String): String? {
        val sharedPrefs =
            this.getSharedPreferences(SP_NAME, AppCompatActivity.MODE_PRIVATE)
        return sharedPrefs?.getString(paramName, null)
    }

    private fun getPermaLongVal(paramName: String): Long? {
        val sharedPrefs =
            this.getSharedPreferences(SP_NAME, AppCompatActivity.MODE_PRIVATE)
        return sharedPrefs?.getLong(paramName, 0L)
    }


    private fun setGlobalVars(
        url: String?,
        usr: String?,
        pwd: String?,
        jwt: String?,
        validUntil: Long?,
    ) {
        val global = (this.application as GlobalApp)
        global.url = url
        global.user = usr
        global.pwd = pwd
        global.jwt = jwt
        global.jwtValidUntil = validUntil
    }

    fun setAllMasterVals(url: String, usr: String, pwd: String, jwt: String?, validUntil: Long?) {
        savePermaData(url, usr, pwd, jwt, validUntil)
        setGlobalVars(url, usr, pwd, jwt, validUntil)
    }

    fun invalidateJwt() {
        val sharedPrefs = this.getSharedPreferences(SP_NAME, MODE_PRIVATE)
        val editor = sharedPrefs?.edit()
        editor?.putString(JWT_NAME, null)
        editor?.putLong(JWT_VALID_UNTIL, 0L)
        editor?.apply()

        val global = (this.application as GlobalApp)
        global.jwt = null
        global.jwtValidUntil = 0L
    }

    fun isJwtValid(): Boolean {
        val global = (this.application as GlobalApp)
        val jwtIsValid: Boolean? = global.jwtValidUntil?.let {
            val validUntilInstant: Instant = Instant.ofEpochMilli(it)
            val instantNow = Instant.now()
            instantNow.isBefore(validUntilInstant)
        }

        return jwtIsValid == true
    }

    fun fromPermaToGlobal(): Unit {
        setGlobalVars(getPermaStringVal(URL_NAME), getPermaStringVal(USR_NAME), getPermaStringVal(
            PWD_NAME), getPermaStringVal(JWT_NAME), getPermaLongVal(JWT_VALID_UNTIL))
    }

    fun hasJwt(): Boolean {
        val global = (this.application as GlobalApp)
        return global.jwt != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Load saved data from storage to a global var */
        fromPermaToGlobal()
    }


}