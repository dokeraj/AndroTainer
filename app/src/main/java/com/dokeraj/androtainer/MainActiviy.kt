package com.dokeraj.androtainer

import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.globalvars.PermaVals.LOG_SETTINGS
import com.dokeraj.androtainer.globalvars.PermaVals.SP_DB
import com.dokeraj.androtainer.globalvars.PermaVals.USERS_CREDENTIALS
import com.dokeraj.androtainer.models.Credential
import com.dokeraj.androtainer.models.CredentialDeserializer
import com.dokeraj.androtainer.models.LogSettings
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
    ) {

        val markwon = Markwon.create(context);
        val mardownFormattedText: Spanned = markwon.toMarkdown(snackbarText)

        val snackbar = Snackbar.make(
            view,
            mardownFormattedText,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView: View = snackbar.view
        val snackbarTextId: Int = R.id.snackbar_text

        val textView = snackbarView.findViewById<View>(snackbarTextId) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setTextColor(ContextCompat.getColor(context, textColor))

        snackbarView.setBackgroundColor(ContextCompat.getColor(context, snackBckColor))

        snackbar.show()
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

        // create new cred that has user's JWT to null and validUntil to 0 and updated lastActivity
        val lastActivity: Long = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
        val curUser: Credential =
            global.currentUser!!.copy(jwt = null, jwtValidUntil = 0L, lastActivity = lastActivity)

        // add that user to globals current cred
        global.currentUser = curUser

        // return that user into the mutable map
        global.credentials["${curUser.serverUrl}.${curUser.username}"] = curUser

        // save the mutable map to perma
        saveCredentialsPermaData(global.credentials)
    }

    fun isJwtValid(): Boolean {
        val global = (this.application as GlobalApp)
        val jwtIsValid: Boolean? = global.currentUser?.jwtValidUntil?.let {
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

        val lastUsedCred: Credential? = getLatestActivityUser(credentials)

        val global = (this.application as GlobalApp)
        global.credentials = collectionCreds
        global.currentUser = lastUsedCred
        global.logSettings = logSettings
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
    }


}