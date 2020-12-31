package com.dokeraj.androtainer

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.TextView
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.dis4)

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars = (globActivity.application as GlobalApp)

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
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(),
                                R.color.blue_main),
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
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(),
                                R.color.orange_warning),
                                PorterDuff.Mode.SRC_ATOP)
                        etUrl.background = background
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            changeBtnState(false)
            if (Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                authenticate(etUrl.text.toString(),
                    etUser.text.toString(),
                    etPass.text.toString(), globActivity)
            } else {
                changeBtnState(true)
                globActivity.showGenericSnack(requireContext(),
                    (getView())!!,
                    "Invalid URL!",
                    R.color.white,
                    R.color.orange_warning)
            }
        }

        if (globActivity.getLogoutMsg() != null) {
            globActivity.showGenericSnack(requireContext(),
                (getView())!!,
                globActivity.getLogoutMsg()!!,
                R.color.white,
                R.color.orange_warning)
            globActivity.setLogoutMsg(null)
        }

        globalVars.url?.let { etUrl.setText(it) }
        globalVars.user?.let { etUser.setText(it) }
        globalVars.pwd?.let { etPass.setText(it) }


        if (globActivity.hasJwt() && globActivity.isJwtValid()) {
            changeBtnState(false)
            getPortainerContainers(globalVars.url!!, globalVars.jwt!!, globActivity)
        } else if (globActivity.hasJwt() && !globActivity.isJwtValid()) {
            changeBtnState(false)
            authenticate(etUrl.text.toString(),
                etUser.text.toString(),
                etPass.text.toString(),
                globActivity)
        }
    }


    private fun authenticate(
        url: String,
        usr: String,
        pwd: String,
        mainActiviy: MainActiviy,
    ) {
        val cred = UserCredentials(usr, pwd)

        val fullPath = getString(R.string.authenticate).replace("{baseUrl}", url.removeSuffix("/"))
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.loginRequest(cred, fullPath)
            .enqueue(object : retrofit2.Callback<Jwt?> {
                override fun onResponse(
                    call: retrofit2.Call<Jwt?>,
                    response: retrofit2.Response<Jwt?>,
                ) {
                    val jwtResponse: String? = response.body()?.jwt
                    showResponseSnack(response.code().toString())

                    jwtResponse?.let {
                        val jwtValidUntil: Long =
                            ZonedDateTime.now(ZoneOffset.UTC).plusHours(7).plusMinutes(59)
                                .toInstant()
                                .toEpochMilli()

                        val globActivity: MainActiviy = (activity as MainActiviy?)!!
                        globActivity.setAllMasterVals(url, usr, pwd, it, jwtValidUntil)

                        getPortainerContainers(url, it, mainActiviy)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Jwt?>, t: Throwable) {
                    changeBtnState(true)
                    mainActiviy.showGenericSnack(requireContext(),
                        view!!,
                        "Server not permitting communication! Check URL.",
                        R.color.red,
                        R.color.white)
                }

            })
    }

    fun showResponseSnack(responseStatus: String) {
        data class SnackStyle(val text: String, val textColor: Int, val bckColor: Int)
        // colors
        val cBlueMain = context?.let { ContextCompat.getColor(it, R.color.blue_main) }
        val cDiscordGray = context?.let { ContextCompat.getColor(it, R.color.dis4) }
        val cRed = context?.let { ContextCompat.getColor(it, R.color.red) }
        val cWhite = context?.let { ContextCompat.getColor(it, R.color.white) }
        val cOrange = context?.let { ContextCompat.getColor(it, R.color.orange_warning) }

        val sbStyle: SnackStyle = when (responseStatus) {
            "200" -> SnackStyle("", cWhite!!, cRed!!)
            "502", "404" -> {
                changeBtnState(true)
                SnackStyle("Wrong URL or service is down", cWhite!!, cRed!!)
            }
            "422" -> {
                changeBtnState(true)
                SnackStyle("Invalid Credentials", cWhite!!, cOrange!!)
            }
            else -> {
                changeBtnState(true)
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
        textView.setTextColor(sbStyle.textColor)

        snackbarView.setBackgroundColor(sbStyle.bckColor)

        if (responseStatus != "200")
            snackbar.show()
    }

    fun changeBtnState(enable: Boolean) {
        if (enable != btnLogin.isEnabled) {
            btnLogin.isEnabled = enable
            if (enable)
                btnLogin.text = "Login"
            else
                btnLogin.text = "Logging in.."
        }
    }


    private fun getPortainerContainers(
        url: String,
        jwt: String,
        mainActiviy: MainActiviy,
    ) {
        val fullUrl =
            getString(R.string.getDockerContainers).replace("{baseUrl}", url.removeSuffix("/"))
        val header = "Bearer $jwt"

        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.listDockerContainers(header, fullUrl, 1)
            .enqueue(object : Callback<PContainersResponse?> {
                override fun onResponse(
                    call: Call<PContainersResponse?>,
                    response: Response<PContainersResponse?>,
                ) {
                    val pcResponse: PContainersResponse? = response.body()

                    pcResponse?.let {
                        // remap from retrofit model to regular data class
                        val pcs: List<PContainer> = it.mapNotNull { pcr ->
                            ContainerStateType.values().firstOrNull { xx -> xx.name == pcr.State }
                                ?.let { cst ->
                                    PContainer(pcr.Id, pcr.Names[0].drop(1).trim().capitalize(),
                                        pcr.Status, cst
                                    )
                                }
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
                    changeBtnState(false)
                    mainActiviy.showGenericSnack(requireContext(),
                        view!!,
                        "Server not permitting communication! Check URL.",
                        R.color.red,
                        R.color.white)
                }
            })
    }
}