package com.dokeraj.androtainer

import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.globalvars.PermaVals.APP_SETTINGS
import com.dokeraj.androtainer.globalvars.PermaVals.LOG_SETTINGS
import com.dokeraj.androtainer.globalvars.PermaVals.SP_DB
import com.dokeraj.androtainer.globalvars.PermaVals.USERS_CREDENTIALS
import com.dokeraj.androtainer.models.Credential
import com.dokeraj.androtainer.models.CredentialDeserializer
import com.dokeraj.androtainer.models.KontainerFilterPref
import com.dokeraj.androtainer.models.LogSettings
import com.dokeraj.androtainer.models.retrofit.AppSettings
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

@AndroidEntryPoint
class MainActiviy : AppCompatActivity() {

    private var logoutMsg: String? = null
    fun getLogoutMsg() = logoutMsg
    fun setLogoutMsg(msg: String?) {
        logoutMsg = msg
    }

    private var loginToDockerLister: Boolean = true
    fun getIsLoginToDockerLister() = loginToDockerLister
    fun setIsLoginToDockerLister(msg: Boolean) {
        loginToDockerLister = msg
    }

    fun showGenericSnack(
        context: Context,
        view: View,
        snackbarText: String,
        textColor: Int,
        snackBckColor: Int,
        duration: Int = 3000,
    ) {

        val markwon = Markwon.create(context);
        val mardownFormattedText: Spanned = markwon.toMarkdown(snackbarText)

        val snackbar = Snackbar.make(
            view,
            mardownFormattedText,
            duration
        )

        val snackbarView: View = snackbar.view
        val snackbarTextId: Int = R.id.snackbar_text

        val textView = snackbarView.findViewById<View>(snackbarTextId) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setTextColor(ContextCompat.getColor(context, textColor))

        snackbarView.setBackgroundColor(ContextCompat.getColor(context, snackBckColor))

        snackbar.show()
    }

    fun setGlobalAppSettings(
        inputKontainerFilter: KontainerFilterPref? = null,
        searchTermVisibility: Boolean? = null,
    ) {
        val global = (this.application as GlobalApp)

        val kontainerFilterToSave: KontainerFilterPref =
            inputKontainerFilter ?: global.appSettings!!.kontainerFilter
        val searchTermVisibilityToSave: Boolean =
            searchTermVisibility ?: global.appSettings!!.searchTermVisibility

        val appSettings = AppSettings(kontainerFilterToSave, searchTermVisibilityToSave)
        // set app settings to global var
        global.appSettings = appSettings

        // save the app settings to the perma val
        saveAppSettingsToPerma(appSettings)
    }

    fun saveAppSettingsToPerma(appSettings: AppSettings) {
        val sharedPrefs = this.getSharedPreferences(SP_DB, MODE_PRIVATE)
        val editor = sharedPrefs?.edit()

        editor?.putString(APP_SETTINGS, Gson().toJson(appSettings))

        editor?.apply()
    }

    fun setGlobalLoggingSettings(
        autoRefresh: Boolean,
        timestamp: Boolean,
        linesCount: Int?,
        autoRefreshInterval: Long?,
    ) {
        val global = (this.application as GlobalApp)

        val logLinesCount: Int = linesCount ?: global.logSettings?.let {
            it.linesCount
        } ?: 1000

        val arInterval: Long = autoRefreshInterval ?: global.logSettings?.let {
            it.autoRefreshInterval
        } ?: 6000L

        val logSettings = LogSettings(autoRefresh, timestamp, logLinesCount, arInterval)
        // set logging settings to global var
        global.logSettings = logSettings

        // save the logging settings to the perma val
        saveLoggingSettingsToPerma(logSettings)
    }

    fun saveLoggingSettingsToPerma(
        logSettings: LogSettings,
    ) {
        val sharedPrefs = this.getSharedPreferences(SP_DB, MODE_PRIVATE)
        val editor = sharedPrefs?.edit()

        editor?.putString(LOG_SETTINGS, Gson().toJson(logSettings))

        editor?.apply()
    }

    fun saveCredentialsPermaData(
        users: Map<String, Credential>,
    ) {
        val creds: List<Credential> = users.map { (_, v) -> v }

        val jsArrayCreds: String = Gson().toJson(creds)

        val sharedPrefs = this.getSharedPreferences(SP_DB, MODE_PRIVATE)
        val editor = sharedPrefs?.edit()

        editor?.putString(USERS_CREDENTIALS, jsArrayCreds)

        editor?.apply()
    }

    fun setGlobalCredentials(user: Credential, isFromLogin: Boolean = false) {
        val global = (this.application as GlobalApp)

        val currentUser: Credential = if (isFromLogin) {
            // create new user cred
            val lastActivity: Long = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
            user.copy(lastActivity = lastActivity)
        } else
            user

        // set this user as current user
        global.currentUser = currentUser

        // add the user to the mutable map (it will be updated or new)
        global.credentials["${currentUser.serverUrl}.${currentUser.username}"] = currentUser

        // save the mutable map to perma
        saveCredentialsPermaData(global.credentials)
    }

    fun deleteUser(credToDel: Credential) {
        val global = (this.application as GlobalApp)

        val keyToDelete = "${credToDel.serverUrl}.${credToDel.username}"

        // remove the credential from the list of saved credentials
        global.credentials.remove(keyToDelete)

        // check to see if the current user is chosen to be deleted and if so set the currentUser to the latest other user that had activity
        if ("${global.currentUser?.serverUrl}.${global.currentUser?.username}" == keyToDelete) {
            global.currentUser = getLatestActivityUser(global.credentials.map { (_, v) -> v })
        }

        // save the mutable map to perma
        saveCredentialsPermaData(global.credentials)
    }

    fun isUserCurrentlyLoggedIn(credToCheck: Credential): Boolean {
        val global = (this.application as GlobalApp)

        val keyToCheck = "${credToCheck.serverUrl}.${credToCheck.username}"
        return "${global.currentUser?.serverUrl}.${global.currentUser?.username}" == keyToCheck
    }

    fun invalidateJwt() {
        val global = (this.application as GlobalApp)

        // validUntil hack to not keep in endless login loop when using Api Key
        val jwtValidUntil = if (global.currentUser!!.isUsingApiKey) -1L else 0L

        // jwt should not be invalidated if user is using Api Key; another hack
        val jwt = if (global.currentUser!!.isUsingApiKey) global.currentUser!!.jwt else null


        // create new cred that has user's JWT to null and validUntil to 0 and updated lastActivity
        val lastActivity: Long = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
        val curUser: Credential =
            global.currentUser!!.copy(jwt = jwt,
                jwtValidUntil = jwtValidUntil,
                lastActivity = lastActivity)

        // add that user to globals current cred
        global.currentUser = curUser

        // return that user into the mutable map
        global.credentials["${curUser.serverUrl}.${curUser.username}"] = curUser

        // save the mutable map to perma
        saveCredentialsPermaData(global.credentials)
    }

    fun isJwtValid(): Boolean {
        val global = (this.application as GlobalApp)

        // hack to not keep you in the login loop when using an API Key login
        val jwtIsValid: Boolean? = if (global.currentUser!!.isUsingApiKey)
            global.currentUser?.jwtValidUntil?.let {
                it == 0L
            }
        else global.currentUser?.jwtValidUntil?.let {
            val validUntilInstant: Instant = Instant.ofEpochMilli(it)
            val instantNow = Instant.now()
            instantNow.isBefore(validUntilInstant)
        }


        return jwtIsValid == true
    }

    fun hasJwt(): Boolean {
        val global = (this.application as GlobalApp)
        return global.currentUser?.jwt != null
    }

    fun isUserUsingApiKey(): Boolean {
        val global = (this.application as GlobalApp)
        return global.currentUser?.isUsingApiKey == true
    }

    private fun initializeGlobalVar(): Unit {
        val sharedPrefs = this.getSharedPreferences(SP_DB, AppCompatActivity.MODE_PRIVATE)
        val usersStr: String? = sharedPrefs?.getString(USERS_CREDENTIALS, null)
        val credentials: List<Credential> = if (usersStr != null) {
            val credentialGSon =
                GsonBuilder().registerTypeAdapter(Credential::class.java, CredentialDeserializer())
                    .create()
            credentialGSon.fromJson(usersStr, Array<Credential>::class.java).toList()
        } else listOf()

        val collectionCreds: MutableMap<String, Credential> = credentials.map {
            "${it.serverUrl}.${it.username}" to it
        }.toMap().toMutableMap()

        val logSettingsStr: String? = sharedPrefs?.getString(LOG_SETTINGS, null)
        val logSettings: LogSettings? = if (logSettingsStr != null)
            GsonBuilder().create().fromJson(logSettingsStr, LogSettings::class.java)
        else null

        val appSettingsStr: String? = sharedPrefs?.getString(APP_SETTINGS, null)
        val appSettings: AppSettings? = if (appSettingsStr != null)
            GsonBuilder().create().fromJson(appSettingsStr, AppSettings::class.java)
        else AppSettings(KontainerFilterPref.RUNNING, false)

        val lastUsedCred: Credential? = getLatestActivityUser(credentials)

        val global = (this.application as GlobalApp)
        global.credentials = collectionCreds
        global.currentUser = lastUsedCred
        global.logSettings = logSettings
        global.appSettings = appSettings
    }

    private fun getLatestActivityUser(allCredentials: List<Credential>): Credential? {
        val lastActivities: List<Credential> =
            allCredentials.filter { it.lastActivity != null }
        return lastActivities.maxByOrNull { it.lastActivity!! }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Load saved data from storage to a global var */
        initializeGlobalVar()

        /** Lay out app in full screen */
        // WindowCompat.setDecorFitsSystemWindows(window, false) // needs work

    }


}