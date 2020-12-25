package com.dokeraj.androtainer

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dokeraj.androtainer.Interfaces.ApiInterface
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.network.RetrofitInstance
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.ZoneOffset
import java.time.ZonedDateTime

class HomeFragment : Fragment(R.layout.fragment_home) {

    /*val SP_NAME = "SHARED_PREFS"
    val URL_NAME = "URL"
    val USR_NAME = "USR"
    val PWD_NAME = "PWD"
    val JWT_NAME = "JWT"

    fun savePermaData(url: String, usr: String, pwd: String, jwt: String?) {
        val sharedPrefs =
            this.activity?.getSharedPreferences(SP_NAME, AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPrefs?.edit()

        editor?.putString(URL_NAME, url)
        editor?.putString(USR_NAME, usr)
        editor?.putString(PWD_NAME, pwd)
        editor?.putString(JWT_NAME, jwt)

        editor?.apply()
    }

    fun getPermaVal(paramName: String): String? {
        val sharedPrefs =
            this.activity?.getSharedPreferences(SP_NAME, AppCompatActivity.MODE_PRIVATE)
        return sharedPrefs?.getString(paramName, null)
    }

    fun isPermaJWTEmpty(): Boolean {
        val sharedPrefs =
            this.activity?.getSharedPreferences(SP_NAME, AppCompatActivity.MODE_PRIVATE)
        val permaJwt: String? = sharedPrefs?.getString(JWT_NAME, null)
        return permaJwt == null
    }

    fun setGlobalVars(url: String, usr: String, pwd: String, jwt: String?) {
        val global = GlobalApp()
        global.url = url
        global.user = usr
        global.pwd = pwd
        global.jwt = jwt
    }*/


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.dis4)

        etUrl.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {

                when (Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                    true -> {
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.web_link_logo,
                            0,
                            0,
                            0)
                        val background = etUrl.background
                        background.mutate()
                        background.colorFilter =
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_main),
                                PorterDuff.Mode.SRC_ATOP)
                        etUrl.background = background
                    }
                    else -> {
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.warning_logo,
                            0,
                            0,
                            0)
                        val background = etUrl.background
                        background.mutate()
                        background.colorFilter =
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow_warning),
                                PorterDuff.Mode.SRC_ATOP)
                        etUrl.background = background
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            changeBtnState(btnLogin, false)
            if (Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                authenticate(etUrl.text.toString(),
                    etUser.text.toString(),
                    etPass.text.toString(), btnLogin, true)
            } else {
                changeBtnState(btnLogin, true)
                println("crush onhyou e ${activity?.applicationContext}") // todo:: add the generic snack
                //val aktivnost: FragmentActivity? = activity
                //Toast.makeText(aktivnost!!.baseContext, "Invalid URL", Toast.LENGTH_SHORT).show()

            }
        }

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars = (globActivity.application as GlobalApp)

        globalVars.url?.let { etUrl.setText(it) }
        globalVars.user?.let { etUser.setText(it) }
        globalVars.pwd?.let { etPass.setText(it) }

        if(globActivity.hasJwt() && globActivity.isJwtValid()) {
            println("JWT IS VALID")
            getPortainerContainers(globalVars.url!!, globalVars.jwt!!)
        } else if (globActivity.hasJwt() && !globActivity.isJwtValid()){
            println("JWT NEEDS REFRESHING")
            authenticate(etUrl.text.toString(),
                etUser.text.toString(),
                etPass.text.toString(),
                null,
                true)
        } else {
            println("First Time user / or logged out")
        }

    }


    private fun authenticate(
        url: String,
        usr: String,
        pwd: String,
        btn: Button?,
        login: Boolean,
    ) {
        val cred = UserCredentials(usr, pwd)

        val fixedBaseUrl: String = url.removeSuffix("/")
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.loginRequest(cred, "$fixedBaseUrl/api/auth")
            .enqueue(object : retrofit2.Callback<Jwt?> {
                override fun onResponse(
                    call: retrofit2.Call<Jwt?>,
                    response: retrofit2.Response<Jwt?>,
                ) {
                    val jwtResponse: String? = response.body()?.jwt
                    showResponseSnack(response.code().toString(), btn)

                    jwtResponse?.let {
                        val jwtValidUntil: Long =
                            ZonedDateTime.now(ZoneOffset.UTC).plusHours(7).plusMinutes(59)
                                .toInstant()
                                .toEpochMilli()

                        val globActivity: MainActiviy = (activity as MainActiviy?)!!
                        globActivity.setAllMasterVals(url, usr, pwd, it, jwtValidUntil)

                        if (login)
                            getPortainerContainers(url, it)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Jwt?>, t: Throwable) {
                    btn?.let { changeBtnState(it, true) }
                    Toast.makeText(activity,
                        "Server not permitting communication!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    // todo:: add new showSnack method for displaying generic messages
    fun showResponseSnack(responseStatus: String, btn: Button?) {
        data class SnackStyle(val text: String, val textColor: Int, val bckColor: Int)
        // colors
        val cBlueMain = context?.let { ContextCompat.getColor(it, R.color.blue_main) }
        val cDiscordGray = context?.let { ContextCompat.getColor(it, R.color.dis4) }
        val cRed = context?.let { ContextCompat.getColor(it, R.color.red) }
        val cWhite = context?.let { ContextCompat.getColor(it, R.color.white) }
        val cOrange = context?.let { ContextCompat.getColor(it, R.color.orange_warning) }
        val cGreen = context?.let { ContextCompat.getColor(it, R.color.green_success) }

        val sbStyle = when (responseStatus) {
            "200" -> SnackStyle("nothing", cWhite!!, cGreen!!)
            "502", "404" -> {
                btn?.let { changeBtnState(it, true) }
                SnackStyle("Wrong URL or service is down", cWhite!!, cRed!!)
            }
            "422" -> {
                btn?.let { changeBtnState(it, true) }
                SnackStyle("Invalid Credentials", cWhite!!, cOrange!!)
            }
            else -> {
                btn?.let { changeBtnState(it, true) }
                SnackStyle("Server response: Unknown error", cBlueMain!!, cDiscordGray!!)
            }
        }

        val v: View? = activity?.findViewById(android.R.id.content)
        val snackbar = Snackbar.make(
            v!!,
            sbStyle.text,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView: View = snackbar.view
        val snackbarTextId: Int = R.id.snackbar_text

        val textView = snackbarView.findViewById<View>(snackbarTextId) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        //textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(sbStyle.textColor)

        snackbarView.setBackgroundColor(sbStyle.bckColor)

        if (responseStatus != "200")
            snackbar.show()
    }


    fun changeBtnState(btn: Button, enable: Boolean) {
        btn.isEnabled = enable
        if (enable)
            btn.text = "Login"
        else
            btn.text = "Logging in.."
    }


    private fun getPortainerContainers(
        url: String,
        jwt: String,
    ) {
        println("PRVOOOOOOOOOOOOOOOOOOOOOOOOO")
        val fullUrl = "${url.removeSuffix("/")}/api/endpoints/1/docker/containers/json"
        val header = "Bearer $jwt"

        println("EVE tukakaka")
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.listDockerContainers(header, fullUrl, 1)
            .enqueue(object : Callback<PContainersResponse?> {
                override fun onResponse(
                    call: Call<PContainersResponse?>,
                    response: Response<PContainersResponse?>,
                ) {


                    val pcResponse: PContainersResponse? = response.body()
                    println("eve bodi::::: $pcResponse")
                    println("EVE KOD:--- ${response.code()}")

                    pcResponse?.let {
                        println("INJUNUIN")

                        // remap from retrofit model to regular data class
                        val pcs = it.map { pcr ->
                            PContainer(pcr.Id, pcr.Names[0].drop(1),
                                pcr.Status,
                                pcr.State)
                        }

                        // go to the ListerFragment and transfer all found docker files
                        val transferContainer = PContainers(pcs)
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToDockerListerFragment(
                                transferContainer)
                        findNavController().navigate(action)
                    }
                }

                override fun onFailure(call: Call<PContainersResponse?>, t: Throwable) {
                    println("U EROROROOOOOOXOXOXOXOXOXOXOOXOX: ${t.message}")
                    Toast.makeText(activity,
                        "Server not permitting communication! Please Log in manually",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}